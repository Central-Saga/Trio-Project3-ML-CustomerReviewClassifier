package com.trio.customerreview

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HistoryStoreInstrumentedTest {

    private lateinit var context: Context
    private var originalHistory: List<HistoryItem> = emptyList()

    @Before
    fun setUp() {
        context = InstrumentationRegistry
            .getInstrumentation()
            .targetContext

        // Simpan riwayat asli agar bisa dipulihkan.
        originalHistory = HistoryStore.load(context)
        HistoryStore.clear(context)
    }

    @After
    fun tearDown() {
        // Pulihkan riwayat asli setelah tes selesai.
        HistoryStore.clear(context)
        originalHistory.asReversed().forEach { item ->
            HistoryStore.add(context, item)
        }
    }

    @Test
    fun addAndLoad_preservesAllPredictionFields() {
        val expected = HistoryItem(
            review = "Produk bagus dan pengiriman cepat",
            label = "POSITIF",
            confidence = 0.998f,
            timestamp = 1_700_000_000_000L
        )

        HistoryStore.add(context, expected)

        val actual = HistoryStore.load(context).firstOrNull()

        assertTrue("Riwayat harus berisi satu item", actual != null)
        assertEquals(expected.review, actual!!.review)
        assertEquals(expected.label, actual.label)
        assertEquals(expected.confidence, actual.confidence, 0.0001f)
        assertEquals(expected.timestamp, actual.timestamp)
    }

    @Test
    fun load_returnsNewestEntriesFirst() {
        val oldest = HistoryItem(
            review = "Ulasan lama",
            label = "NEGATIF",
            confidence = 0.80f,
            timestamp = 100L
        )

        val newest = HistoryItem(
            review = "Ulasan terbaru",
            label = "POSITIF",
            confidence = 0.95f,
            timestamp = 300L
        )

        val middle = HistoryItem(
            review = "Ulasan tengah",
            label = "POSITIF",
            confidence = 0.90f,
            timestamp = 200L
        )

        HistoryStore.add(context, oldest)
        HistoryStore.add(context, newest)
        HistoryStore.add(context, middle)

        val result = HistoryStore.load(context)

        assertEquals(3, result.size)
        assertEquals(300L, result[0].timestamp)
        assertEquals(200L, result[1].timestamp)
        assertEquals(100L, result[2].timestamp)
    }

    @Test
    fun remove_deletesOnlyMatchingEntry() {
        val first = HistoryItem(
            review = "Produk bagus",
            label = "POSITIF",
            confidence = 0.95f,
            timestamp = 100L
        )

        val second = HistoryItem(
            review = "Produk rusak",
            label = "NEGATIF",
            confidence = 0.98f,
            timestamp = 200L
        )

        HistoryStore.add(context, first)
        HistoryStore.add(context, second)
        HistoryStore.remove(context, second)

        val result = HistoryStore.load(context)

        assertEquals(1, result.size)
        assertEquals(first.review, result[0].review)
        assertEquals(first.label, result[0].label)
        assertEquals(first.timestamp, result[0].timestamp)
    }

    @Test
    fun clear_removesAllEntries() {
        HistoryStore.add(
            context,
            HistoryItem(
                review = "Ulasan positif",
                label = "POSITIF",
                confidence = 0.95f,
                timestamp = 100L
            )
        )

        HistoryStore.add(
            context,
            HistoryItem(
                review = "Ulasan negatif",
                label = "NEGATIF",
                confidence = 0.97f,
                timestamp = 200L
            )
        )

        HistoryStore.clear(context)

        assertTrue(HistoryStore.load(context).isEmpty())
    }

    @Test
    fun add_keepsAtMost100NewestEntries() {
        for (index in 1..110) {
            HistoryStore.add(
                context,
                HistoryItem(
                    review = "Ulasan ke-$index",
                    label = if (index % 2 == 0) {
                        "POSITIF"
                    } else {
                        "NEGATIF"
                    },
                    confidence = 0.90f,
                    timestamp = index.toLong()
                )
            )
        }

        val result = HistoryStore.load(context)

        assertEquals(100, result.size)
        assertEquals(110L, result.first().timestamp)
        assertEquals(11L, result.last().timestamp)
    }
}