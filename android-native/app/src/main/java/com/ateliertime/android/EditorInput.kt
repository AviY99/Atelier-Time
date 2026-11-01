package com.ateliertime.android

import com.ateliertime.domain.*

/** UI text adapter, separate from Compose. Keep scheduler itself Android-independent. */
data class OpeningDraft(
    val id: Int,
    val count: String = "4",
    val minutes: String = "1",
    // Null is reserved for untouched legacy presets; all new groups set a value.
    val breakAfterMinutes: String? = null,
    // Preserve the exact immutable approved group identifier on Edit.
    val originalGroupId: String? = null,
)
/** D-143: one source of truth for the editor's original fresh-start values. */
internal fun freshEditorInput() = EditorInput(
    name = "Atelier class",
    totalMinutes = "180",
    summaryMinutes = "15",
    poseMinutes = "25",
    normalBreakMinutes = "5",
    specialBreakMinutes = "15",
    specialCount = "1",
    opening = emptyList(),
    cycleStart = CycleStart.POSE_FIRST,
    automaticPlacement = true,
    manualOrdinals = "3",
    tailExtensionMinutes = 0,
)

data class EditorInput(
    val name: String,
    val totalMinutes: String,
    val summaryMinutes: String,
    val poseMinutes: String,
    val normalBreakMinutes: String,
    val specialBreakMinutes: String,
    val specialCount: String,
    val opening: List<OpeningDraft>,
    val cycleStart: CycleStart,
    val automaticPlacement: Boolean,
    val manualOrdinals: String,
    val tailExtensionMinutes: Int = 0,
) {
    fun toRules(): SessionRules {
        fun read(input: String, label: String): Int =
            input.trim().toIntOrNull()
                ?: throw IllegalArgumentException("Enter a whole number for " + label)
        fun seconds(input: String, label: String): Int {
            val minutes = read(input, label)
            val result = minutes.toLong() * SECONDS_PER_MINUTE
            require(result in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()) {
                "Duration for " + label + " exceeds the supported range."
            }
            return result.toInt()
        }
        if (name.isBlank()) throw IllegalArgumentException("Enter a session name.")
        val count = read(specialCount, "special break count")
        val placement = if (!automaticPlacement && count > 0) {
            val ordinals = if (manualOrdinals.isBlank()) emptyList() else
                manualOrdinals.split(",").map { read(it, "manual break ordinal") }
            SpecialBreakPlacement.manual(ordinals)
        } else SpecialBreakPlacement()
        return SessionRules(
            id = "editor-preview",
            name = name.trim(),
            totalSeconds = seconds(totalMinutes, "total minutes"),
            summarySeconds = seconds(summaryMinutes, "summary minutes"),
            openingGroups = opening.map {
                OpeningPoseGroup(
                    it.originalGroupId ?: ("opening_" + it.id),
                    read(it.count, "opening pose count"),
                    seconds(it.minutes, "opening pose minutes"),
                    it.breakAfterMinutes?.let { input ->
                        seconds(input, "break after opening group")
                    },
                )
            },
            poseSeconds = seconds(poseMinutes, "pose minutes"),
            shortBreakSeconds =
                seconds(normalBreakMinutes, "normal break minutes"),
            specialBreakSeconds =
                seconds(specialBreakMinutes, "special break minutes"),
            specialBreakCount = count,
            cycleStart = cycleStart,
            placement = placement,
            tailPoseExtensionSeconds = run {
                val value = tailExtensionMinutes.toLong() * SECONDS_PER_MINUTE
                require(value in 0L..Int.MAX_VALUE.toLong()) {
                    "Final Session extension is outside the supported range."
                }
                value.toInt()
            },
        )
    }
}
