# Customer Review Classification

An Android application for classifying Indonesian customer reviews into positive and negative sentiment using IndoBERT and ONNX Runtime.

The application performs model inference locally on the Android device after the application and model have been installed.

## Features

- Classifies customer reviews into positive or negative sentiment.
- Displays the predicted sentiment and confidence score supported by the application UI.
- Uses an IndoBERT model exported to ONNX and quantized to INT8.
- Runs sentiment inference locally without requiring a FastAPI prediction API.

## Application Screenshots

### Positive Sentiment Prediction

![Positive sentiment prediction](docs/screenshots/02-positive-prediction.png)

### Negative Sentiment Prediction

![Negative sentiment prediction](docs/screenshots/03-negative-prediction.png)

## Technology Stack

- Kotlin and Android SDK
- Android Studio
- Gradle Kotlin DSL
- IndoBERT
- ONNX Runtime for Android
- Git LFS for large model files

## Project Structure

```text
CustomerReviewClassifier/
+-- android/
¦   +-- app/
¦   ¦   +-- src/main/
¦   ¦       +-- assets/
¦   ¦       ¦   +-- model.onnx
¦   ¦       ¦   +-- vocab.txt
¦   ¦       ¦   +-- tokenizer.json
¦   ¦       ¦   +-- ...
¦   ¦       +-- java/com/trio/customerreview/
¦   ¦           +-- MainActivity.kt
¦   ¦           +-- OnnxPredictor.kt
¦   ¦           +-- TextTokenizer.kt
¦   +-- gradle/
¦   +-- build.gradle.kts
¦   +-- settings.gradle.kts
+-- docs/
¦   +-- demo-scenario.md
¦   +-- screenshots/
¦       +-- 02-positive-prediction.png
¦       +-- 03-negative-prediction.png
+-- ml_training/
¦   +-- notebooks/
¦   ¦   +-- customer_review_classification_ml_pipeline.ipynb
¦   +-- README.md
+-- .gitattributes
+-- .gitignore
+-- README.md
```

## Machine Learning Pipeline

The training notebook documents the following workflow:

1. Exploratory Data Analysis (EDA).
2. Baseline text classification using TF-IDF and Logistic Regression.
3. Fine-tuning `indobenchmark/indobert-base-p1`.
4. Model evaluation using classification metrics.
5. Exporting the model to ONNX and quantizing it to INT8.
6. Preparing the model and tokenizer for Android deployment.

### Dataset

The training workflow uses `labeledReview.datasetFix.json`, a labeled customer review dataset.

The EDA notebook reports 11,606 rows. See the training notebook for dataset preparation and experiment details.

### Sentiment Labels

| Label | Sentiment |
| --- | --- |
| 0 | Negative |
| 1 | Positive |

### ONNX INT8 Evaluation Results

The training notebook reports the following evaluation results for the ONNX INT8 model:

| Metric | Result |
| --- | ---: |
| Accuracy | 93.36% |
| Precision | 93.38% |
| Recall | 93.36% |
| F1-score | 93.36% |

The reported evaluation used 1,160 test samples: 580 negative reviews and 580 positive reviews.

These metrics describe evaluation on the test dataset. They do not guarantee that every individual prediction made by the application will be correct.

For training and experiment details, see [ML Training Documentation](ml_training/README.md).

## Android Application

### Requirements

- Android Studio
- Android SDK compatible with the project configuration
- Git
- Git LFS for downloading large model files

### Clone the Repository

```bash
git clone https://github.com/Central-Saga/Trio-Project3-ML-CustomerReviewClassifier.git
cd Trio-Project3-ML-CustomerReviewClassifier
git lfs install
git lfs pull
```

### Run Using Android Studio

1. Open Android Studio.
2. Open the `android/` directory.
3. Wait for Gradle Sync to finish.
4. Start an Android emulator or connect a compatible Android device.
5. Select the `app` run configuration.
6. Click **Run**.

### Build Using PowerShell

From the repository root, enter the Android project directory:

```powershell
Set-Location android
.\gradlew.bat assembleDebug
```

To install the debug build on a connected device or running emulator:

```powershell
.\gradlew.bat installDebug
```

The debug APK is normally generated at:

```text
android/app/build/outputs/apk/debug/app-debug.apk
```

### How Inference Works

```text
Customer Review
      |
      v
TextTokenizer.kt
      |
      v
input_ids, attention_mask, token_type_ids
      |
      v
OnnxPredictor.kt
      |
      v
ONNX INT8 Model
      |
      v
Positive / Negative Sentiment
      |
      v
Prediction Result and Confidence, if displayed
```

The tokenizer prepares the text input, and `OnnxPredictor.kt` uses ONNX Runtime to run model inference.

The Android application does not require FastAPI for sentiment prediction.

## Git LFS

The ONNX model is stored using Git LFS because of its file size.

After cloning the repository, install Git LFS and download the tracked model files:

```bash
git lfs install
git lfs pull
```

Confirm that the model files are available before running the application.

## Demo Scenario

See [Demo Scenario](docs/demo-scenario.md) for the demonstration flow, example reviews, machine learning overview, and demo checklist.

## Repository

[Central-Saga/Trio-Project3-ML-CustomerReviewClassifier](https://github.com/Central-Saga/Trio-Project3-ML-CustomerReviewClassifier)