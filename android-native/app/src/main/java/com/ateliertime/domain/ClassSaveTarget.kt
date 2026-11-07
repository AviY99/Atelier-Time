package com.ateliertime.domain

/** Class names identify saved plans in the editor; an existing name requires confirmation. */
object ClassSaveTarget {
    fun existingId(entries: List<StoredPreset>, proposedTitle: String): String? {
        val clean = proposedTitle.trim()
        require(clean.isNotEmpty()) { "Enter a class name." }
        return entries.firstOrNull {
            it.title.equals(clean, ignoreCase = true)
        }?.id
    }
}
