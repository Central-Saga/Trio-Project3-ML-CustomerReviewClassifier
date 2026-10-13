package com.trio.customerreview

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class HistoryActivity : AppCompatActivity() {

    private lateinit var countText: TextView
    private lateinit var historyContainer: LinearLayout
    private lateinit var emptyText: TextView

    private val backgroundColor = Color.rgb(16, 21, 31)
    private val cardColor = Color.rgb(25, 36, 48)
    private val accentColor = Color.rgb(101, 244, 229)
    private val primaryTextColor = Color.rgb(239, 246, 250)
    private val secondaryTextColor = Color.rgb(172, 190, 201)

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(HistoryStore.localizedContext(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val english = HistoryStore.currentLanguage(this) == "en"

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(16), dp(20), dp(20))
            setBackgroundColor(backgroundColor)
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val backButton = Button(this).apply {
            text = getString(R.string.back_button)
            setTextColor(accentColor)
            isAllCaps = false
            setOnClickListener { finish() }
        }

        val title = TextView(this).apply {
            text = if (english) "Analysis History" else "Riwayat Analisis"
            textSize = 22f
            setTextColor(primaryTextColor)
            setTypeface(null, Typeface.BOLD)
            setPadding(dp(8), 0, 0, 0)
        }

        header.addView(
            backButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        header.addView(
            title,
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        )

        root.addView(header)

        countText = TextView(this).apply {
            textSize = 14f
            setTextColor(secondaryTextColor)
            setPadding(0, dp(14), 0, dp(4))
        }

        root.addView(countText)

        val latestText = TextView(this).apply {
            text = getString(R.string.history_latest_first)
            textSize = 12f
            setTextColor(secondaryTextColor)
            setPadding(0, 0, 0, dp(12))
        }

        root.addView(latestText)

        val scrollView = ScrollView(this)

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        emptyText = TextView(this).apply {
            text = if (english) {
                "No analysis history yet. Your predictions will appear here."
            } else {
                "Belum ada riwayat analisis. Hasil prediksi akan muncul di sini."
            }
            textSize = 14f
            setTextColor(secondaryTextColor)
            gravity = Gravity.CENTER
            setPadding(dp(20), dp(32), dp(20), dp(32))
        }

        content.addView(emptyText)

        historyContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        content.addView(historyContainer)
        scrollView.addView(content)

        root.addView(
            scrollView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        val clearButton = Button(this).apply {
            text = getString(R.string.history_clear_all)
            isAllCaps = false
            setTextColor(accentColor)
            setOnClickListener { confirmClearHistory() }
        }

        root.addView(
            clearButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        val footer = TextView(this).apply {
            text = getString(R.string.history_footer)
            textSize = 11f
            gravity = Gravity.CENTER
            setTextColor(secondaryTextColor)
            setPadding(0, dp(10), 0, 0)
        }

        root.addView(footer)

        setContentView(root)
        renderHistory()
    }

    private fun renderHistory() {
        val items = HistoryStore.load(this)
        val english = HistoryStore.currentLanguage(this) == "en"

        countText.text = getString(R.string.history_count_format, items.size)
        emptyText.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        historyContainer.removeAllViews()

        items.forEach { item ->
            val confidencePercent =
                (item.confidence * 100f).roundToInt().coerceIn(0, 100)

            val dateText = SimpleDateFormat(
                "dd MMM yyyy, HH:mm",
                Locale.getDefault()
            ).format(Date(item.timestamp))

            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(16), dp(14), dp(16), dp(14))

                background = GradientDrawable().apply {
                    setColor(cardColor)
                    cornerRadius = dp(16).toFloat()
                    setStroke(dp(1), Color.rgb(52, 75, 87))
                }

                isClickable = true
                isFocusable = true
                setOnClickListener { showDetail(item) }
            }

            val sentiment = TextView(this).apply {
                text = "${item.label} • $confidencePercent%"
                textSize = 15f
                setTextColor(accentColor)
                setTypeface(null, Typeface.BOLD)
            }

            val reviewPreview = if (item.review.length > 140) {
                item.review.take(140) + "..."
            } else {
                item.review
            }

            val review = TextView(this).apply {
                text = reviewPreview
                textSize = 14f
                setTextColor(primaryTextColor)
                setPadding(0, dp(8), 0, dp(8))
            }

            val date = TextView(this).apply {
                text = dateText
                textSize = 11f
                setTextColor(secondaryTextColor)
            }

            card.addView(sentiment)
            card.addView(review)
            card.addView(date)

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            params.bottomMargin = dp(12)
            historyContainer.addView(card, params)
        }
    }

    private fun showDetail(item: HistoryItem) {
        val english = HistoryStore.currentLanguage(this) == "en"
        val confidencePercent =
            (item.confidence * 100f).roundToInt().coerceIn(0, 100)

        val message = buildString {
            append(item.review)
            append("\n\n")
            append(if (english) "Sentiment: " else "Sentimen: ")
            append(item.label)
            append("\n")
            append(if (english) "Confidence: " else "Tingkat keyakinan: ")
            append("$confidencePercent%")
            append("\n")
            append(
                SimpleDateFormat(
                    "dd MMM yyyy, HH:mm",
                    Locale.getDefault()
                ).format(Date(item.timestamp))
            )
        }

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.history_detail_title))
            .setMessage(message)
            .setPositiveButton(getString(R.string.history_use_review)) { _, _ ->
                useReview(item)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun useReview(item: HistoryItem) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra(HistoryStore.EXTRA_SELECTED_REVIEW, item.review)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        startActivity(intent)
        finish()
    }

    private fun confirmClearHistory() {
        val english = HistoryStore.currentLanguage(this) == "en"

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.history_clear_all))
            .setMessage(
                if (english) {
                    "Are you sure you want to delete all analysis history?"
                } else {
                    "Yakin ingin menghapus semua riwayat analisis?"
                }
            )
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(if (english) "Delete" else "Hapus") { _, _ ->
                HistoryStore.clear(this)
                renderHistory()
            }
            .show()
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
