package com.trio.customerreview

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var etReview: EditText
    private lateinit var btnPredict: Button
    private lateinit var tvSentiment: TextView
    private lateinit var tvConfidence: TextView
    private lateinit var tvHistoryEmpty: TextView
    private lateinit var tvHistoryCount: TextView
    private lateinit var pbConfidence: ProgressBar
    private lateinit var historyContainer: LinearLayout

    private lateinit var screenHome: View
    private lateinit var screenAnalysis: View
    private lateinit var screenHistory: View
    private lateinit var screenSettings: View
    private lateinit var screenDetail: View
    private lateinit var bottomNav: View

    private lateinit var navHome: LinearLayout
    private lateinit var navAnalysis: LinearLayout
    private lateinit var navHistory: LinearLayout
    private lateinit var navSettings: LinearLayout
    private lateinit var navHomeIcon: ImageView
    private lateinit var navAnalysisIcon: ImageView
    private lateinit var navHistoryIcon: ImageView
    private lateinit var navSettingsIcon: ImageView
    private lateinit var navHomeLabel: TextView
    private lateinit var navAnalysisLabel: TextView
    private lateinit var navHistoryLabel: TextView
    private lateinit var navSettingsLabel: TextView

    private lateinit var tvLatestSentiment: TextView
    private lateinit var tvLatestConfidence: TextView
    private lateinit var tvLatestTime: TextView
    private lateinit var tvStatTotal: TextView
    private lateinit var tvStatPositive: TextView
    private lateinit var tvStatNegative: TextView

    private lateinit var btnLanguageId: TextView
    private lateinit var btnLanguageEn: TextView
    private lateinit var tvDetailSentiment: TextView
    private lateinit var tvDetailConfidence: TextView
    private lateinit var tvDetailDate: TextView
    private lateinit var tvDetailReview: TextView

    private lateinit var tokenizer: TextTokenizer
    private var tokenizerReady = false
    private var onnxPredictor: OnnxPredictor? = null
    private var selectedHistoryItem: HistoryItem? = null
    private var detailReturnScreen: String = SCREEN_HISTORY
    private var currentScreen: String = SCREEN_HOME
    private var screenTransitionToken = 0
    private var hasShownInitialScreen = false

    private val executor: ExecutorService = Executors.newSingleThreadExecutor()

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(HistoryStore.localizedContext(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        applySystemBarInsets()
        bindViews()
        bindActions()
        resetResult()
        updateLanguageButtons()

        try {
            tokenizer = TextTokenizer(
                context = this,
                vocabFileName = "vocab.txt",
                maxLength = 128
            )
            tokenizerReady = true
        } catch (e: Exception) {
            tokenizerReady = false
            btnPredict.isEnabled = false
            tvSentiment.text = getString(R.string.error_label)
            tvConfidence.text = getString(R.string.tokenizer_failed)
            android.util.Log.e(TAG, "Failed to initialize tokenizer", e)
        }

        showScreen(savedInstanceState?.getString(STATE_SCREEN) ?: SCREEN_HOME)
        handleSelectedReview(intent)
        renderHomeSummary()
        renderHistory()
    }

    private fun applySystemBarInsets() {
        val root = findViewById<View>(R.id.appRoot)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
    }

    @Suppress("DEPRECATION")
    private fun updateStatusBarIconAppearance(darkBackground: Boolean) {
        val currentFlags = window.decorView.systemUiVisibility
        window.decorView.systemUiVisibility = if (darkBackground) {
            currentFlags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        } else {
            currentFlags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }

    private fun bindViews() {
        screenHome = findViewById(R.id.screenHome)
        screenAnalysis = findViewById(R.id.screenAnalysis)
        screenHistory = findViewById(R.id.screenHistory)
        screenSettings = findViewById(R.id.screenSettings)
        screenDetail = findViewById(R.id.screenDetail)
        bottomNav = findViewById(R.id.bottomNav)

        etReview = findViewById(R.id.etReview)
        btnPredict = findViewById(R.id.btnPredict)
        tvSentiment = findViewById(R.id.tvSentiment)
        tvConfidence = findViewById(R.id.tvConfidence)
        pbConfidence = findViewById(R.id.pbConfidence)
        historyContainer = findViewById(R.id.historyContainer)
        tvHistoryEmpty = findViewById(R.id.tvHistoryEmpty)
        tvHistoryCount = findViewById(R.id.tvHistoryCount)

        navHome = findViewById(R.id.navHome)
        navAnalysis = findViewById(R.id.navAnalysis)
        navHistory = findViewById(R.id.navHistory)
        navSettings = findViewById(R.id.navSettings)
        navHomeIcon = findViewById(R.id.navHomeIcon)
        navAnalysisIcon = findViewById(R.id.navAnalysisIcon)
        navHistoryIcon = findViewById(R.id.navHistoryIcon)
        navSettingsIcon = findViewById(R.id.navSettingsIcon)
        navHomeLabel = findViewById(R.id.navHomeLabel)
        navAnalysisLabel = findViewById(R.id.navAnalysisLabel)
        navHistoryLabel = findViewById(R.id.navHistoryLabel)
        navSettingsLabel = findViewById(R.id.navSettingsLabel)

        tvLatestSentiment = findViewById(R.id.tvLatestSentiment)
        tvLatestConfidence = findViewById(R.id.tvLatestConfidence)
        tvLatestTime = findViewById(R.id.tvLatestTime)
        tvStatTotal = findViewById(R.id.tvStatTotal)
        tvStatPositive = findViewById(R.id.tvStatPositive)
        tvStatNegative = findViewById(R.id.tvStatNegative)

        btnLanguageId = findViewById(R.id.btnLanguageId)
        btnLanguageEn = findViewById(R.id.btnLanguageEn)
        tvDetailSentiment = findViewById(R.id.tvDetailSentiment)
        tvDetailConfidence = findViewById(R.id.tvDetailConfidence)
        tvDetailDate = findViewById(R.id.tvDetailDate)
        tvDetailReview = findViewById(R.id.tvDetailReview)
    }

    private fun bindActions() {
        navHome.setOnClickListener { showScreen(SCREEN_HOME) }
        navAnalysis.setOnClickListener { showScreen(SCREEN_ANALYSIS) }
        navHistory.setOnClickListener { showScreen(SCREEN_HISTORY) }
        navSettings.setOnClickListener { showScreen(SCREEN_SETTINGS) }

        findViewById<View>(R.id.quickAnalyze).setOnClickListener {
            showScreen(SCREEN_ANALYSIS)
            etReview.requestFocus()
        }
        findViewById<View>(R.id.quickHistory).setOnClickListener {
            showScreen(SCREEN_HISTORY)
        }
        findViewById<View>(R.id.quickLanguage).setOnClickListener {
            showScreen(SCREEN_SETTINGS)
        }
        findViewById<View>(R.id.quickHowItWorks).setOnClickListener {
            showHowItWorks()
        }
        findViewById<View>(R.id.btnNotifications).setOnClickListener {
            showHowItWorks()
        }
        findViewById<View>(R.id.btnLatestDetail).setOnClickListener {
            val latest = HistoryStore.load(this).firstOrNull()
            if (latest != null) openDetail(latest, SCREEN_HOME)
            else showScreen(SCREEN_ANALYSIS)
        }
        findViewById<View>(R.id.btnAnalysisBack).setOnClickListener {
            showScreen(SCREEN_HOME)
        }
        findViewById<View>(R.id.btnDetailBack).setOnClickListener {
            showScreen(detailReturnScreen)
        }
        findViewById<View>(R.id.btnDetailShare).setOnClickListener {
            shareSelectedHistory()
        }
        findViewById<View>(R.id.btnReuseReview).setOnClickListener {
            reuseSelectedReview()
        }
        findViewById<View>(R.id.btnDeleteReview).setOnClickListener {
            confirmDeleteSelectedHistory()
        }
        findViewById<View>(R.id.btnClearHistory).setOnClickListener {
            confirmClearHistory()
        }

        btnPredict.setOnClickListener { predictReview() }
        btnLanguageId.setOnClickListener { changeLanguage("id") }
        btnLanguageEn.setOnClickListener { changeLanguage("en") }
    }

    private fun changeLanguage(language: String) {
        HistoryStore.setLanguage(this, language)
        recreate()
    }

    private fun updateLanguageButtons() {
        val selectedLanguage = HistoryStore.currentLanguage(this)
        val idSelected = selectedLanguage == "id"
        btnLanguageId.setBackgroundResource(
            if (idSelected) R.drawable.bg_language_selected else R.drawable.bg_language_unselected
        )
        btnLanguageEn.setBackgroundResource(
            if (!idSelected) R.drawable.bg_language_selected else R.drawable.bg_language_unselected
        )
        btnLanguageId.setTextColor(if (idSelected) Color.WHITE else getColor(R.color.text_primary))
        btnLanguageEn.setTextColor(if (!idSelected) Color.WHITE else getColor(R.color.text_primary))
    }

    private fun resetResult() {
        tvSentiment.text = getString(R.string.sentiment_placeholder)
        tvSentiment.setTextColor(getColor(R.color.text_primary))
        tvConfidence.text = getString(R.string.confidence_placeholder)
        pbConfidence.progress = 0
        pbConfidence.visibility = View.GONE
    }

    private fun showScreen(screen: String) {
        val previousScreen = currentScreen
        val transitionId = ++screenTransitionToken

        val allScreens = listOf(screenHome, screenAnalysis, screenHistory, screenSettings, screenDetail)
        val previousView = when (previousScreen) {
            SCREEN_HOME -> screenHome
            SCREEN_ANALYSIS -> screenAnalysis
            SCREEN_HISTORY -> screenHistory
            SCREEN_SETTINGS -> screenSettings
            SCREEN_DETAIL -> screenDetail
            else -> screenHome
        }
        val targetView = when (screen) {
            SCREEN_HOME -> screenHome
            SCREEN_ANALYSIS -> screenAnalysis
            SCREEN_HISTORY -> screenHistory
            SCREEN_SETTINGS -> screenSettings
            SCREEN_DETAIL -> screenDetail
            else -> screenHome
        }

        currentScreen = screen
        val navyHeader = screen == SCREEN_ANALYSIS || screen == SCREEN_DETAIL
        window.statusBarColor = getColor(if (navyHeader) R.color.navy else R.color.background_soft)
        updateStatusBarIconAppearance(navyHeader)

        allScreens.forEach { it.animate().cancel() }

        if (!hasShownInitialScreen || previousScreen == screen) {
            allScreens.forEach { view ->
                view.visibility = if (view == targetView) View.VISIBLE else View.GONE
                view.alpha = 1f
                view.translationX = 0f
            }
        } else {
            allScreens.filter { it != targetView && it != previousView }.forEach { view ->
                view.visibility = View.GONE
                view.alpha = 1f
                view.translationX = 0f
            }

            previousView.visibility = View.VISIBLE
            previousView.alpha = 1f
            previousView.translationX = 0f

            targetView.visibility = View.VISIBLE
            targetView.alpha = 0f
            targetView.translationX = dp(16).toFloat()

            previousView.animate()
                .alpha(0f)
                .translationX(-dp(6).toFloat())
                .setDuration(135L)
                .setInterpolator(android.view.animation.DecelerateInterpolator(1.3f))
                .withEndAction {
                    if (transitionId == screenTransitionToken && currentScreen == screen) {
                        previousView.visibility = View.GONE
                        previousView.alpha = 1f
                        previousView.translationX = 0f
                    }
                }
                .start()

            targetView.animate()
                .alpha(1f)
                .translationX(0f)
                .setDuration(230L)
                .setInterpolator(android.view.animation.DecelerateInterpolator(1.5f))
                .start()
        }
        hasShownInitialScreen = true

        // Fade the floating navbar when entering/leaving the detail screen.
        bottomNav.animate().cancel()
        if (screen == SCREEN_DETAIL) {
            if (bottomNav.visibility == View.VISIBLE) {
                bottomNav.animate()
                    .alpha(0f)
                    .translationY(dp(8).toFloat())
                    .setDuration(140L)
                    .setInterpolator(android.view.animation.DecelerateInterpolator(1.4f))
                    .withEndAction {
                        if (transitionId == screenTransitionToken && currentScreen == SCREEN_DETAIL) {
                            bottomNav.visibility = View.GONE
                            bottomNav.alpha = 1f
                            bottomNav.translationY = 0f
                        }
                    }
                    .start()
            } else {
                bottomNav.visibility = View.GONE
            }
        } else {
            if (bottomNav.visibility != View.VISIBLE) {
                bottomNav.visibility = View.VISIBLE
                bottomNav.alpha = 0f
                bottomNav.translationY = dp(8).toFloat()
            }
            bottomNav.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(190L)
                .setInterpolator(android.view.animation.DecelerateInterpolator(1.4f))
                .start()
        }

        updateNavigationSelection(screen)
        if (screen == SCREEN_HOME) renderHomeSummary()
        if (screen == SCREEN_HISTORY) renderHistory()
        if (screen == SCREEN_SETTINGS) updateLanguageButtons()
    }

    private fun updateNavigationSelection(screen: String) {
        updateNavItem(navHome, navHomeIcon, navHomeLabel, screen == SCREEN_HOME)
        updateNavItem(navAnalysis, navAnalysisIcon, navAnalysisLabel, screen == SCREEN_ANALYSIS)
        updateNavItem(navHistory, navHistoryIcon, navHistoryLabel, screen == SCREEN_HISTORY)
        updateNavItem(navSettings, navSettingsIcon, navSettingsLabel, screen == SCREEN_SETTINGS)
    }

    private fun updateNavItem(
        item: LinearLayout,
        icon: ImageView,
        label: TextView,
        selected: Boolean
    ) {
        val previousSelected = item.tag as? Boolean
        item.tag = selected
        if (previousSelected != null && previousSelected != selected) {
            item.animate().cancel()
            item.scaleX = 0.96f
            item.scaleY = 0.96f
            item.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(180L)
                .setInterpolator(android.view.animation.DecelerateInterpolator(1.5f))
                .start()
        }

        item.setBackgroundResource(
            if (selected) R.drawable.bg_nav_item_active else R.drawable.bg_nav_item_inactive
        )
        (item.layoutParams as? LinearLayout.LayoutParams)?.let { params ->
            params.weight = if (selected) 1.35f else 0.88f
            item.layoutParams = params
        }
        icon.imageTintList = ColorStateList.valueOf(
            if (selected) Color.WHITE else getColor(R.color.icon_muted)
        )
        label.visibility = if (selected) View.VISIBLE else View.GONE
        label.setTextColor(Color.WHITE)
    }

    /**
     * Validate input before checking model readiness so an empty review always gets feedback.
     * All prediction work runs on the background executor; UI state is updated on the main thread.
     */
    private fun predictReview() {
        val review = etReview.text?.toString()?.trim().orEmpty()

        if (review.isBlank()) {
            etReview.error = getString(R.string.review_empty_error)
            etReview.requestFocus()
            android.widget.Toast.makeText(
                this,
                getString(R.string.review_empty_error),
                android.widget.Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Clear the previous validation hint once the user has supplied text.
        etReview.error = null

        if (!tokenizerReady) {
            showPredictionFailure(
                message = getString(R.string.tokenizer_failed),
                error = IllegalStateException("Tokenizer is not ready")
            )
            return
        }

        btnPredict.isEnabled = false
        btnPredict.text = getString(R.string.processing_button)
        tvSentiment.text = getString(R.string.processing_title)
        tvSentiment.setTextColor(getColor(R.color.text_primary))
        tvConfidence.text = getString(R.string.processing_message)
        pbConfidence.progress = 0
        pbConfidence.visibility = View.VISIBLE

        executor.execute {
            try {
                val predictor = onnxPredictor ?: synchronized(this) {
                    onnxPredictor ?: OnnxPredictor(context = this).also { onnxPredictor = it }
                }
                val encoded = tokenizer.encode(review)
                val result = predictor.predict(
                    inputIds = encoded.inputIds,
                    attentionMask = encoded.attentionMask,
                    tokenTypeIds = encoded.tokenTypeIds
                )

                runOnUiThread {
                    if (isFinishing || isDestroyed) return@runOnUiThread

                    val negative = isNegativeLabel(result.label)
                    tvSentiment.text = localizedSentiment(result.label)
                    val resultColor = getColor(
                        if (negative) R.color.sentiment_negative else R.color.sentiment_positive
                    )
                    tvSentiment.setTextColor(resultColor)
                    pbConfidence.progressTintList = ColorStateList.valueOf(resultColor)

                    val percent = (result.confidence * 100f).coerceIn(0f, 100f)
                    tvConfidence.text = getString(R.string.confidence_format, percent)
                    pbConfidence.progress = percent.toInt()
                    pbConfidence.visibility = View.VISIBLE

                    HistoryStore.add(
                        this,
                        HistoryItem(
                            review = review,
                            label = result.label,
                            confidence = result.confidence,
                            timestamp = System.currentTimeMillis()
                        )
                    )

                    btnPredict.isEnabled = true
                    btnPredict.text = getString(R.string.analyze_button)
                    renderHomeSummary()
                    renderHistory()
                }
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Prediction failed", e)
                runOnUiThread {
                    if (isFinishing || isDestroyed) return@runOnUiThread
                    showPredictionFailure(
                        message = getString(R.string.prediction_failed),
                        error = e
                    )
                }
            }
        }
    }

    /** Show a user-friendly error and always restore the prediction button/loading state. */
    private fun showPredictionFailure(message: String, error: Throwable? = null) {
        error?.let { android.util.Log.e(TAG, message, it) }
        tvSentiment.text = getString(R.string.error_label)
        tvSentiment.setTextColor(getColor(R.color.sentiment_negative))
        tvConfidence.text = message
        pbConfidence.visibility = View.GONE
        btnPredict.isEnabled = true
        btnPredict.text = getString(R.string.analyze_button)
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_LONG).show()
    }

    private fun isNegativeLabel(label: String): Boolean {
        val normalized = label.lowercase(Locale.ROOT)
        return normalized.contains("negatif") || normalized.contains("negative")
    }

    private fun localizedSentiment(label: String): String {
        return if (isNegativeLabel(label)) {
            getString(R.string.sentiment_negative)
        } else {
            getString(R.string.sentiment_positive)
        }
    }

    private fun renderHomeSummary() {
        val entries = HistoryStore.load(this)
        val latest = entries.firstOrNull()
        if (latest == null) {
            tvLatestSentiment.text = getString(R.string.latest_empty_title)
            tvLatestSentiment.setTextColor(Color.WHITE)
            tvLatestConfidence.text = getString(R.string.latest_empty_subtitle)
            tvLatestTime.text = getString(R.string.latest_empty_action)
        } else {
            val negative = isNegativeLabel(latest.label)
            tvLatestSentiment.text = localizedSentiment(latest.label)
            tvLatestSentiment.setTextColor(
                if (negative) Color.parseColor("#FFCCD2") else Color.parseColor("#C9F4DE")
            )
            tvLatestConfidence.text = getString(
                R.string.latest_confidence_format,
                latest.confidence * 100f
            )
            tvLatestTime.text = formatDate(latest.timestamp)
        }

        val calendar = Calendar.getInstance(currentLocale()).apply {
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfWeek = calendar.timeInMillis
        val weekEntries = entries.filter { it.timestamp >= startOfWeek }
        tvStatTotal.text = weekEntries.size.toString()
        tvStatPositive.text = weekEntries.count { !isNegativeLabel(it.label) }.toString()
        tvStatNegative.text = weekEntries.count { isNegativeLabel(it.label) }.toString()
    }

    private fun renderHistory() {
        val entries = HistoryStore.load(this)
        tvHistoryCount.text = getString(R.string.history_count_format, entries.size)
        tvHistoryEmpty.visibility = if (entries.isEmpty()) View.VISIBLE else View.GONE
        historyContainer.removeAllViews()

        entries.forEach { entry ->
            historyContainer.addView(createHistoryCard(entry))
        }

        findViewById<View>(R.id.btnClearHistory).apply {
            isEnabled = entries.isNotEmpty()
            alpha = if (entries.isEmpty()) 0.45f else 1f
        }
    }

    private fun createHistoryCard(item: HistoryItem): View {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(15), dp(16), dp(15))
            setBackgroundResource(R.drawable.bg_card_white)
            elevation = dp(2).toFloat()
            isClickable = true
            isFocusable = true
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val negative = isNegativeLabel(item.label)
        val color = getColor(
            if (negative) R.color.sentiment_negative else R.color.sentiment_positive
        )

        val sentimentView = TextView(this).apply {
            text = localizedSentiment(item.label)
            textSize = 14f
            setTextColor(color)
            setTypeface(typeface, Typeface.BOLD)
        }
        header.addView(sentimentView, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

        val confidenceView = TextView(this).apply {
            text = getString(R.string.history_confidence_format, item.confidence * 100f)
            textSize = 12f
            setTextColor(getColor(R.color.text_secondary))
            gravity = Gravity.END
        }
        header.addView(confidenceView)
        card.addView(header)

        val reviewView = TextView(this).apply {
            text = item.review
            textSize = 13f
            setTextColor(getColor(R.color.text_primary))
            maxLines = 2
            ellipsize = TextUtils.TruncateAt.END
            setPadding(0, dp(9), 0, 0)
        }
        card.addView(reviewView)

        val dateView = TextView(this).apply {
            text = formatDate(item.timestamp)
            textSize = 11f
            setTextColor(getColor(R.color.text_muted))
            setPadding(0, dp(9), 0, 0)
        }
        card.addView(dateView)

        card.setOnClickListener { openDetail(item, SCREEN_HISTORY) }
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = dp(11) }
        card.layoutParams = params
        return card
    }

    private fun openDetail(item: HistoryItem, returnScreen: String) {
        selectedHistoryItem = item
        detailReturnScreen = returnScreen
        val negative = isNegativeLabel(item.label)
        tvDetailSentiment.text = localizedSentiment(item.label)
        tvDetailSentiment.setTextColor(
            getColor(if (negative) R.color.sentiment_negative else R.color.sentiment_positive)
        )
        tvDetailConfidence.text = getString(
            R.string.history_confidence_format,
            item.confidence * 100f
        )
        tvDetailDate.text = formatDate(item.timestamp)
        tvDetailReview.text = item.review
        showScreen(SCREEN_DETAIL)
    }

    private fun reuseSelectedReview() {
        val item = selectedHistoryItem ?: return
        etReview.setText(item.review)
        etReview.setSelection(etReview.text.length)
        showScreen(SCREEN_ANALYSIS)
    }

    private fun shareSelectedHistory() {
        val item = selectedHistoryItem ?: return
        val shareText = buildString {
            append(getString(R.string.detail_title))
            append("\n")
            append(getString(R.string.detail_sentiment_label))
            append(": ")
            append(localizedSentiment(item.label))
            append("\n")
            append(getString(R.string.detail_confidence_label))
            append(": ")
            append(getString(R.string.history_confidence_format, item.confidence * 100f))
            append("\n\n")
            append(item.review)
        }
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        startActivity(Intent.createChooser(sendIntent, getString(R.string.share_result)))
    }

    private fun confirmDeleteSelectedHistory() {
        val item = selectedHistoryItem ?: return
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_history_item_title)
            .setMessage(R.string.delete_history_item_message)
            .setNegativeButton(R.string.cancel_button, null)
            .setPositiveButton(R.string.delete_button) { _, _ ->
                HistoryStore.remove(this, item)
                selectedHistoryItem = null
                renderHistory()
                renderHomeSummary()
                showScreen(if (detailReturnScreen == SCREEN_HOME) SCREEN_HOME else SCREEN_HISTORY)
            }
            .show()
    }

    private fun confirmClearHistory() {
        AlertDialog.Builder(this)
            .setTitle(R.string.clear_history_title)
            .setMessage(R.string.clear_history_message)
            .setNegativeButton(R.string.cancel_button, null)
            .setPositiveButton(R.string.delete_button) { _, _ ->
                HistoryStore.clear(this)
                selectedHistoryItem = null
                renderHistory()
                renderHomeSummary()
            }
            .show()
    }

    private fun showHowItWorks() {
        AlertDialog.Builder(this)
            .setTitle(R.string.how_it_works)
            .setMessage(R.string.how_it_works_message)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun formatDate(timestamp: Long): String {
        return SimpleDateFormat(getString(R.string.history_date_pattern), currentLocale())
            .format(Date(timestamp))
    }

    private fun currentLocale(): Locale {
        return Locale.forLanguageTag(
            if (HistoryStore.currentLanguage(this) == "en") "en-US" else "id-ID"
        )
    }

    private fun handleSelectedReview(intent: Intent?) {
        val selected = intent?.getStringExtra(HistoryStore.EXTRA_SELECTED_REVIEW) ?: return
        etReview.setText(selected)
        etReview.setSelection(etReview.text.length)
        showScreen(SCREEN_ANALYSIS)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleSelectedReview(intent)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(STATE_SCREEN, currentScreen)
        super.onSaveInstanceState(outState)
    }

    @Deprecated("Use the in-app navigation to return between screens")
    override fun onBackPressed() {
        when (currentScreen) {
            SCREEN_DETAIL -> showScreen(detailReturnScreen)
            SCREEN_ANALYSIS, SCREEN_HISTORY, SCREEN_SETTINGS -> showScreen(SCREEN_HOME)
            else -> super.onBackPressed()
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    override fun onDestroy() {
        executor.shutdownNow()
        onnxPredictor?.close()
        onnxPredictor = null
        super.onDestroy()
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val SCREEN_HOME = "home"
        private const val SCREEN_ANALYSIS = "analysis"
        private const val SCREEN_HISTORY = "history"
        private const val SCREEN_SETTINGS = "settings"
        private const val SCREEN_DETAIL = "detail"
        private const val STATE_SCREEN = "review_buddy_current_screen"
    }
}
