package com.trio.customerreview

import android.content.Context
import android.content.res.Configuration
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

data class HistoryItem(
    val review: String,
    val label: String,
    val confidence: Float,
    val timestamp: Long
)

object HistoryStore {

    const val PREFS_NAME = "review_buddy_preferences"
    const val KEY_LANGUAGE = "language"
    const val EXTRA_SELECTED_REVIEW = "selected_review"
    const val MAX_HISTORY = 100

    private const val KEY_HISTORY = "prediction_history"

    private fun preferences(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun currentLanguage(context: Context): String {
        return preferences(context).getString(KEY_LANGUAGE, "id") ?: "id"
    }

    fun setLanguage(context: Context, language: String) {
        val selectedLanguage = if (language == "en") "en" else "id"
        preferences(context).edit().putString(KEY_LANGUAGE, selectedLanguage).apply()
    }

    fun localizedContext(base: Context): Context {
        val language = currentLanguage(base)
        val locale = Locale.forLanguageTag(if (language == "en") "en-US" else "id-ID")
        Locale.setDefault(locale)
        val configuration = Configuration(base.resources.configuration)
        configuration.setLocale(locale)
        return base.createConfigurationContext(configuration)
    }

    fun load(context: Context): List<HistoryItem> {
        val raw = preferences(context).getString(KEY_HISTORY, "[]") ?: "[]"
        return try {
            val array = JSONArray(raw)
            val items = ArrayList<HistoryItem>()
            for (index in 0 until array.length()) {
                val obj = array.optJSONObject(index) ?: continue
                items.add(
                    HistoryItem(
                        review = obj.optString("review", ""),
                        label = obj.optString("label", ""),
                        confidence = obj.optDouble("confidence", 0.0).toFloat(),
                        timestamp = obj.optLong("timestamp", 0L)
                    )
                )
            }
            items.sortedByDescending { it.timestamp }.take(MAX_HISTORY)
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun add(context: Context, item: HistoryItem) {
        save(context, (listOf(item) + load(context)).take(MAX_HISTORY))
    }

    fun remove(context: Context, item: HistoryItem) {
        val updated = load(context).filterNot {
            it.timestamp == item.timestamp && it.review == item.review && it.label == item.label
        }
        save(context, updated)
    }

    private fun save(context: Context, items: List<HistoryItem>) {
        val array = JSONArray()
        items.take(MAX_HISTORY).forEach { item ->
            array.put(
                JSONObject()
                    .put("review", item.review)
                    .put("label", item.label)
                    .put("confidence", item.confidence.toDouble())
                    .put("timestamp", item.timestamp)
            )
        }
        preferences(context).edit().putString(KEY_HISTORY, array.toString()).apply()
    }

    fun clear(context: Context) {
        preferences(context).edit().remove(KEY_HISTORY).apply()
    }
}
