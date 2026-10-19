# Demo Scenario — Customer Review Classification

## 1. Tujuan

Mendemonstrasikan aplikasi Android untuk mengklasifikasikan teks ulasan pelanggan menjadi sentimen positif atau negatif menggunakan model IndoBERT yang dijalankan secara lokal melalui ONNX Runtime.

## 2. Informasi Agenda

- Jadwal awal pada Issue #5: Sabtu, 17 Oktober 2026.
- Waktu yang tercatat: 13.00 WITA.
- Catatan: jadwal awal sudah lewat. Perbarui tanggal dan waktu jika demo dijadwalkan ulang.

## 3. Persiapan

Sebelum demonstrasi:

1. Buka aplikasi Review Buddy pada emulator atau perangkat Android.
2. Pastikan aplikasi dapat dibuka dan halaman input tersedia.
3. Pastikan model ONNX serta file tokenizer tersedia.
4. Siapkan contoh ulasan positif dan negatif.
5. Pastikan screenshot dokumentasi berada di `docs/screenshots/`.

## 4. Alur Demonstrasi

### Tahap 1 — Pembukaan (1 menit)

Jelaskan bahwa aplikasi mengklasifikasikan ulasan pelanggan ke dalam dua kelas sentimen, yaitu positif dan negatif.

### Tahap 2 — Pengenalan Aplikasi (1 menit)

Tunjukkan halaman aplikasi dan jelaskan cara memasukkan ulasan, menjalankan prediksi, serta membaca hasilnya.

### Tahap 3 — Pengujian Sentimen Positif (1–2 menit)

Masukkan ulasan berikut:

> Produk bagus, kualitas sangat baik dan pengiriman cepat. Saya puas dengan pembelian ini.

Jalankan prediksi. Amati hasil sentimen dan confidence score yang ditampilkan aplikasi.

### Tahap 4 — Pengujian Sentimen Negatif (1–2 menit)

Masukkan ulasan berikut:

> Kualitas produk buruk, barang rusak dan saya sangat kecewa dengan pembelian ini.

Jalankan prediksi. Amati hasil sentimen dan confidence score yang ditampilkan aplikasi.

### Tahap 5 — Penjelasan Machine Learning (2 menit)

Jelaskan alur pengembangan model:

1. Eksplorasi dan pemeriksaan dataset.
2. Pembuatan baseline menggunakan TF-IDF dan Logistic Regression.
3. Fine-tuning IndoBERT untuk klasifikasi sentimen.
4. Evaluasi model menggunakan metrik klasifikasi.
5. Ekspor model ke ONNX dan kuantisasi INT8.
6. Integrasi model dan tokenizer ke aplikasi Android.

Notebook mencatat hasil evaluasi model ONNX INT8 sebagai berikut:

| Metrik | Nilai |
| --- | ---: |
| Accuracy | 93,36% |
| Precision | 93,38% |
| Recall | 93,36% |
| F1-score | 93,36% |

Evaluasi menggunakan 1.160 sampel uji, terdiri dari 580 ulasan negatif dan 580 ulasan positif. Nilai tersebut merupakan hasil pengujian pada dataset uji, bukan jaminan bahwa setiap prediksi akan benar.

### Tahap 6 — Penutup dan Tanya Jawab (1 menit)

Rangkum fitur aplikasi, metode machine learning, proses inferensi lokal, dan hasil evaluasi model. Lanjutkan dengan sesi pertanyaan.

## 5. Checklist Keberhasilan

- [ ] Aplikasi dapat dibuka tanpa crash.
- [ ] Ulasan positif dapat diuji.
- [ ] Ulasan negatif dapat diuji.
- [ ] Hasil prediksi dapat dibaca.
- [ ] Confidence score dijelaskan jika ditampilkan oleh UI.
- [ ] Pipeline IndoBERT dan ONNX Runtime dapat dijelaskan.
- [ ] Metrik evaluasi disampaikan dengan konteks dataset uji.

## 6. Referensi

- [README utama](../README.md)
- [Dokumentasi training ML](../ml_training/README.md)
- [Screenshot hasil prediksi](screenshots/)
