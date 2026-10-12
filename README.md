# Student Companion

Student Companion is a comprehensive, offline-first Android application designed for students to manage subjects, track attendance, calculate bunk buffers, organize subject note vaults with audio and OCR search, perform spaced repetition study reviews, inspect upcoming exams, and run focus study sessions with emergency cram mode.

## Features

- **Attendance Engine & Bunk Buffer**: Real-time calculation of safe bunk buffer or mandatory classes needed to maintain target percentage.
- **3-Step Onboarding**: Custom appearance, default target attendance, and optional feature flags.
- **Material 3 Expressive Design System**: Supports Light, Dark, System, and OLED black themes with glassmorphism UI components.
- **Subject Vaults & Search**: Organize notes and attachments per subject with real-time text search. Dynamic subject creation prevents Room foreign-key constraint failures on clean installs.
- **Focus Guard & Emergency Cram**: Focus timer with retrospective session tagging and cram mode toggle.
- **Spaced Repetition Queue**: Leitner 5-box card algorithm with sticky unanswered card persistence.
- **Security & Full Database Backup/Restore**: Keystore-backed `SecurityManager` with safe fallback handling, AES-256-GCM + PBKDF2 password-derived encrypted backups across all entities, and offline-by-default optional Ask AI integration.
- **Centralized Debug Logger**: Safe file logging to `debug.log` with automatic redaction of keys, tokens, emails, and passwords.

## Architecture

- **Clean Architecture & MVVM**: `data` (Room, DAOs, Repositories, Security), `domain` (Models), and `ui` (Jetpack Compose Screens, Themes, Components).
- **Offline-First**: All core functionality operates strictly locally without cloud dependencies or tracking SDKs.
- **CI Build**: Configured GitHub Actions workflow (`.github/workflows/build.yml`) for automated debug APK compilation and artifact upload.

## Building and Testing

Run unit tests:
```bash
./gradlew test
```

Build Debug APK:
```bash
./gradlew assembleDebug
```
The APK will be generated at `app/build/outputs/apk/debug/app-debug.apk`.
