# Green Banana Myanmar — Offline Native Android APK

This repository is intended for the **Green Banana Myanmar 100-Acre Offline Native Android APK**.

## Required Android source
Upload the **extracted contents** of the project ZIP (not the ZIP file itself) to the repository root, including:

- `app/build.gradle`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/asia/aaii/greenbanana/MainActivity.java`
- `app/src/main/java/asia/aaii/greenbanana/Site3DView.java`
- `app/src/main/assets/site_model.bin`
- `app/src/main/assets/guides/Green_Banana_Myanmar_Innovation.pdf`
- all other `app/src/main/assets/` and `app/src/main/res/` files
- `build.gradle`, `settings.gradle`, `gradle.properties`, `check_project.py`

To start the Android build: open **Actions → Build Green Banana Offline Android APK → Run workflow** after committing the complete source to `main`.

The GitHub Actions build checks the offline assets, builds a debug Android APK, verifies its signature and packaged files, and uploads it as a build artifact.

**Do not claim the APK is built until GitHub Actions reports success.** The repository is **public**: any source files you upload will become publicly accessible. Do not upload credentials, keystores, or private materials without authorization.

Build workflow and project setup prepared for the user-provided source.