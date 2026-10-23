package com.ateliertime.android

import android.content.Context
import com.ateliertime.domain.*
import org.json.JSONArray
import org.json.JSONObject

/**
 * Entire library is written via ONE SharedPreferences commit.
 * Versioned JSON is local/private and usable offline. Never overwrite
 * unreadable or future-schema data with an empty collection.
 */
class AndroidPresetStore(
    context: Context,
    storageName: String = "atelier_time_presets",
) : PresetStore {
    private val preferences = context.applicationContext.getSharedPreferences(
        storageName, Context.MODE_PRIVATE,
    )

    override fun read(): PresetCollection {
        val source = preferences.getString(KEY, null) ?: return PresetCollection()
        try {
            val root = JSONObject(source)
            check(root.getInt("version") == VERSION) {
                "Unsupported saved-session version. Data was not changed."
            }
            val array = root.getJSONArray("presets")
            val entries = (0 until array.length()).map { decodePreset(array.getJSONObject(it)) }
            return PresetCollection(
                entries = entries,
                activeId = if (root.isNull("activeId")) null else root.getString("activeId"),
            )
        } catch (e: Exception) {
            throw IllegalStateException(
                "Saved sessions cannot be read. The original local data was preserved.", e,
            )
        }
    }

    override fun write(value: PresetCollection) {
        val root = JSONObject()
            .put("version", VERSION)
            .put("activeId", value.activeId ?: JSONObject.NULL)
            .put("presets", JSONArray().apply {
                value.entries.forEach { put(encodePreset(it)) }
            })
        check(preferences.edit().putString(KEY, root.toString()).commit()) {
            "Could not save sessions to phone storage. Previous data was preserved."
        }
    }

    private fun encodePreset(preset: StoredPreset): JSONObject = JSONObject()
        .put("id", preset.id)
        .put("title", preset.title)
        .put("rules", encodeRules(preset.rules))
        .put("ending", when (preset.ending?.mode) {
            FinalPoseMode.REMAINDER -> JSONObject().put("mode", "REMAINDER")
            FinalPoseMode.EVEN_SPLIT -> JSONObject().put("mode", "EVEN_SPLIT")
            FinalPoseMode.CUSTOM_SPLIT -> JSONObject()
                .put("mode", "CUSTOM_SPLIT")
                .put("firstPoseSeconds", preset.ending.firstPoseSeconds)
            null -> JSONObject.NULL
        })
        .put("acknowledgedRestWarnings", preset.acknowledgedRestWarnings)
        .put("timelineSignature", preset.timelineSignature)

    private fun decodePreset(json: JSONObject): StoredPreset {
        val ending = when {
            json.isNull("ending") -> null
            else -> {
                val data = json.getJSONObject("ending")
                when (data.getString("mode")) {
                    "REMAINDER" -> FinalPoseChoice.remainder()
                    "EVEN_SPLIT" -> FinalPoseChoice.even()
                    "CUSTOM_SPLIT" -> FinalPoseChoice.custom(data.getInt("firstPoseSeconds"))
                    else -> error("Unknown saved ending choice.")
                }
            }
        }
        return StoredPreset(
            id = json.getString("id"),
            title = json.getString("title"),
            rules = decodeRules(json.getJSONObject("rules")),
            ending = ending,
            acknowledgedRestWarnings = json.getBoolean("acknowledgedRestWarnings"),
            timelineSignature = json.getString("timelineSignature"),
        )
    }

    private fun encodeRules(r: SessionRules): JSONObject = JSONObject()
        .put("id", r.id)
        .put("name", r.name)
        .put("totalSeconds", r.totalSeconds)
        .put("summarySeconds", r.summarySeconds)
        .put("poseSeconds", r.poseSeconds)
        .put("shortBreakSeconds", r.shortBreakSeconds)
        .put("specialBreakSeconds", r.specialBreakSeconds)
        .put("specialBreakCount", r.specialBreakCount)
        .put("cycleStart", r.cycleStart.name)
        .put("placementAutomatic", r.placement.automatic)
        .put("placementOrdinals", JSONArray().apply {
            r.placement.breakOrdinals.forEach { put(it) }
        })
        .put("openingGroups", JSONArray().apply {
            r.openingGroups.forEach {
                put(JSONObject().put("id", it.id).put("count", it.count)
                    .put("poseSeconds", it.poseSeconds))
            }
        })

    private fun decodeRules(data: JSONObject): SessionRules {
        val opening = data.getJSONArray("openingGroups")
        val ordinals = data.getJSONArray("placementOrdinals")
        val replacement = (0 until ordinals.length()).map { ordinals.getInt(it) }
        val placement = if (data.getBoolean("placementAutomatic")) {
            check(replacement.isEmpty()) {
                "An automatic saved preset contains unexpected manual break ordinals."
            }
            SpecialBreakPlacement()
        } else {
            SpecialBreakPlacement.manual(replacement)
        }
        return SessionRules(
            id = data.getString("id"),
            name = data.getString("name"),
            totalSeconds = data.getInt("totalSeconds"),
            summarySeconds = data.getInt("summarySeconds"),
            openingGroups = (0 until opening.length()).map {
                val group = opening.getJSONObject(it)
                OpeningPoseGroup(
                    id = group.getString("id"),
                    count = group.getInt("count"),
                    poseSeconds = group.getInt("poseSeconds"),
                )
            },
            poseSeconds = data.getInt("poseSeconds"),
            shortBreakSeconds = data.getInt("shortBreakSeconds"),
            specialBreakSeconds = data.getInt("specialBreakSeconds"),
            specialBreakCount = data.getInt("specialBreakCount"),
            cycleStart = CycleStart.valueOf(data.getString("cycleStart")),
            placement = placement,
        )
    }

    private companion object {
        const val KEY = "library_v1"
        const val VERSION = 1
    }
}
