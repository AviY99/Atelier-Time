package com.ateliertime.domain

/**
 * Pure Kotlin session library. Android is used only by the PresetStore adapter.
 * A saved entry is an immutable, APPROVED scheduling revision, not a raw draft.
 */
data class StoredPreset(
    val id: String,
    val title: String,
    val rules: SessionRules,
    val ending: FinalPoseChoice?,
    val acknowledgedRestWarnings: Boolean,
    val timelineSignature: String,
)

data class PresetCollection(
    val entries: List<StoredPreset> = emptyList(),
    val activeId: String? = null,
)

interface PresetStore {
    fun read(): PresetCollection
    /** Must be an atomic transaction; throw if the complete state cannot be written. */
    fun write(value: PresetCollection)
}

class PresetCatalog(
    private val store: PresetStore,
    private val engine: ScheduleEngine = ScheduleEngine(),
    private val newId: () -> String,
) {
    fun state(): PresetCollection {
        val collection = store.read()
        check(collection.entries.map { it.id }.distinct().size == collection.entries.size) {
            "Saved sessions have duplicate IDs. Storage was not modified."
        }
        collection.entries.forEach { checkedSession(it) }
        check(collection.activeId == null ||
            collection.entries.any { it.id == collection.activeId }) {
            "Saved active session is missing. Storage was not modified."
        }
        return collection
    }

    fun open(id: String): ApprovedSession = checkedSession(find(state(), id))

    fun save(
        approved: ApprovedSession,
        title: String,
        overwriteId: String? = null,
    ): StoredPreset {
        val clean = title.trim()
        require(clean.isNotEmpty()) { "Enter a preset name." }
        val old = state()
        if (overwriteId != null) find(old, overwriteId) // never silently create instead
        val id = overwriteId ?: newId()
        require(id.isNotBlank()) { "Preset ID cannot be blank." }
        check(overwriteId != null || old.entries.none { it.id == id }) {
            "Generated preset ID is already used."
        }
        val rules = approved.rules.copy(id = id, name = clean)
        val candidate = engine.generate(rules, approved.selectedChoice)
        require(!candidate.hasBlockingIssues && candidate.canApprove) {
            "Cannot save an invalid or unresolved schedule."
        }
        val acknowledged = approved.acknowledgedWarnings.isNotEmpty()
        val checked = engine.approve(candidate, acknowledgeWarnings = acknowledged)
        require(checked.phases == approved.phases &&
            checked.acknowledgedWarnings == approved.acknowledgedWarnings) {
            "Approved schedule has changed; review and approve it again."
        }
        val record = StoredPreset(
            id = id, title = clean, rules = rules,
            ending = approved.selectedChoice,
            acknowledgedRestWarnings = acknowledged,
            timelineSignature = signature(checked),
        )
        val entries = if (overwriteId == null) old.entries + record else
            old.entries.map { if (it.id == overwriteId) record else it }
        store.write(old.copy(entries = entries))
        return record
    }

    fun duplicate(id: String, newTitle: String): StoredPreset =
        save(open(id), newTitle)

    fun rename(id: String, newTitle: String): StoredPreset =
        save(open(id), newTitle, overwriteId = id)

    fun delete(id: String) {
        val old = state()
        find(old, id)
        store.write(old.copy(
            entries = old.entries.filterNot { it.id == id },
            activeId = old.activeId.takeUnless { it == id },
        ))
    }

    /** Select the exact approved plan; does NOT start a running timer. */
    fun activate(id: String): ApprovedSession {
        val old = state()
        val session = checkedSession(find(old, id))
        store.write(old.copy(activeId = id))
        return session
    }

    private fun find(collection: PresetCollection, id: String): StoredPreset =
        collection.entries.firstOrNull { it.id == id }
            ?: throw IllegalArgumentException("Saved session does not exist.")

    private fun checkedSession(preset: StoredPreset): ApprovedSession {
        check(preset.id.isNotBlank() && preset.title.isNotBlank() &&
            preset.rules.id == preset.id && preset.rules.name == preset.title) {
            "Saved session metadata is damaged; no data was overwritten."
        }
        val candidate = engine.generate(preset.rules, preset.ending)
        check(candidate.canApprove &&
            !candidate.hasBlockingIssues &&
            (!candidate.needsSafetyAcknowledgement ||
                preset.acknowledgedRestWarnings)) {
            "A saved session no longer satisfies the approved rules."
        }
        val approved = engine.approve(
            candidate,
            acknowledgeWarnings = preset.acknowledgedRestWarnings,
        )
        check(signature(approved) == preset.timelineSignature) {
            "Saved session differs from the originally approved timeline."
        }
        return approved
    }

    companion object {
        /**
         * Non-cryptographic consistency signature covering boundaries, ids,
         * sources, break ordinals and warning offsets. Corruption detection,
         * NOT a security/authentication mechanism.
         */
        fun signature(session: ApprovedSession): String =
            session.phases.joinToString("|") {
                listOf(
                    it.id, it.kind.name, it.startSeconds, it.endSeconds,
                    it.poseNumber, it.poseSource?.name, it.openingGroupId,
                    it.breakOrdinal,
                ).joinToString(",")
            } + "#" + session.acknowledgedWarnings.joinToString("|") {
                listOf(it.code, it.offsetSeconds).joinToString(",")
            }
    }
}
