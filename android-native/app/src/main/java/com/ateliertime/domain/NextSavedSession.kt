package com.ateliertime.domain

/**
 * Select the next persisted, approved session in library order.
 * Wrap around after the last one; never select the only current preset again.
 * An unsaved current session starts with the first saved preset.
 */
object NextSavedSession {
    fun nextId(ids: List<String>, currentId: String?): String? {
        if (ids.isEmpty()) return null
        val index = ids.indexOf(currentId)
        if (index < 0) return ids.first()
        if (ids.size < 2) return null
        return ids[(index + 1) % ids.size]
    }
}
