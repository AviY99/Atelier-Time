package com.ateliertime.android

import com.ateliertime.domain.*

/** UI text adapter, separate from Compose. Keep scheduler itself Android-independent. */
data class OpeningDraft(val id: Int, val count: String = "4", val minutes: String = "1")
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
) {
    fun toRules(): SessionRules {
        fun read(input: String, label: String): Int =
            input.trim().toIntOrNull()
                ?: throw IllegalArgumentException("Enter a whole number for " + label)
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
            totalSeconds = read(totalMinutes, "total minutes") * SECONDS_PER_MINUTE,
            summarySeconds = read(summaryMinutes, "summary minutes") * SECONDS_PER_MINUTE,
            openingGroups = opening.map {
                OpeningPoseGroup(
                    "opening_" + it.id,
                    read(it.count, "opening pose count"),
                    read(it.minutes, "opening pose minutes") * SECONDS_PER_MINUTE,
                )
            },
            poseSeconds = read(poseMinutes, "pose minutes") * SECONDS_PER_MINUTE,
            shortBreakSeconds =
                read(normalBreakMinutes, "normal break minutes") * SECONDS_PER_MINUTE,
            specialBreakSeconds =
                read(specialBreakMinutes, "special break minutes") * SECONDS_PER_MINUTE,
            specialBreakCount = count,
            cycleStart = cycleStart,
            placement = placement,
        )
    }
}
