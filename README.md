# 📞 Contacts Pro — Android Contact Management & Caller App

A modern, fast, and feature-rich Android Contact Manager and Quick Caller built with Kotlin, Material Design 3, and SQLite.

![Android](https://img.shields.io/badge/Android-7.0%2B%20(API%2024%2B)-3DDC84?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?logo=kotlin&logoColor=white)
![Build](https://img.shields.io/badge/Build-Passing-brightgreen)
![License](https://img.shields.io/badge/License-MIT-blue)

---

## ✨ Features

- **📇 Comprehensive Contact Storage:**
  - Full Name, Primary Phone, Secondary Phone, Email Address, Group / Category (Family, Friends, Work, Other), and Notes.
  - ⭐ Mark / Unmark Favorites with a single tap.
  - Initials-based color avatars generated automatically.

- **📞 Direct Calling & Fallback:**
  - One-tap calling to any contact using `Intent.ACTION_CALL`.
  - Automatic safe fallback to system dialer (`Intent.ACTION_DIAL`) if permission is denied or on non-telephony devices (tablets).

- **🔢 In-App Quick Dial Pad:**
  - Interactive dial pad (keys 0–9, *, #, and '+' long-press on 0).
  - Live display, single-character backspace, and long-press clear.
  - Direct call button and quick "Save to Contacts" shortcut.

- **💬 SMS & Communication:**
  - Direct SMS shortcut to launch messaging app prefilled with recipient.
  - Email sending and one-tap Contact Sharing via standard Android share sheet.

- **🔍 Live Search & Filter Chips:**
  - Instant real-time filtering as you type (searches across name, phone numbers, and email).
  - Quick filter chips: `All`, `⭐ Favorites`, `Family`, `Friends`, and `Work`.

- **💾 Robust Local SQLite Persistence:**
  - Full offline storage powered by `SQLiteOpenHelper`.
  - Comes pre-seeded with realistic sample contacts on fresh install.

- **🎨 Modern Dark UI:**
  - Sleek Material Design 3 dark surfaces (`#0B0F19`, `#1E293B`).
  - High-contrast typography and emerald green / royal blue call action buttons.

---

## 📱 Screenshots & Previews

The app is packaged with an ready-to-install debug/release APK located in the [`apk/`](apk/) directory:
- **`apk/ContactsPro-v1.0.apk`** (~5.8 MB)

---

## 🛠️ Tech Stack & Architecture

- **Language:** Kotlin
- **Compile SDK / Target SDK:** 36 (Android 15+)
- **Min SDK:** 24 (Android 7.0 Nougat+) — Compatible across modern phones & tablets
- **UI Architecture:** ViewBinding with custom MaterialCard & RecyclerView
- **Database:** SQLite with full CRUD and indexed search filtering
- **Testing:** JUnit 4 unit tests covering initials logic, phone formatting, and data mutations

---

## 🚀 Building & Running

### Prerequisites
- Android Studio Ladybug / Meerkat or Android SDK command-line tools
- JDK 17 or JDK 21 (bundled JBR in Android Studio)

### Build via Gradle
```bash
# Clone the repository
git clone https://github.com/rajeevksofficial/ContactsApp.git
cd ContactsApp

# Run unit tests
./gradlew testDebugUnitTest

# Assemble Debug APK
./gradlew assembleDebug
```

### Install APK to Connected Device or Emulator
```bash
adb install -r apk/ContactsPro-v1.0.apk
```
