# 🌿 Green Banana Myanmar — Offline Native APK

## Single-ZIP APK build

**Upload just one file** to this public repository: `Green_Banana_Myanmar_APK_Build_Ready_V1_2.zip` (20.1 MiB).

1. Download the project ZIP from the ChatGPT conversation.
2. Open [GitHub Upload](https://github.com/aiacademyanmar/greenbanana/upload/main).
3. Drag the **ZIP itself** (no extraction required) into the upload area.
4. Commit directly to the `main` branch.
5. Open [GitHub Actions](https://github.com/aiacademyanmar/greenbanana/actions/workflows/build-android-apk.yml) to follow the build.
6. When the run succeeds, open its **Artifacts** section and download `Green_Banana_Myanmar_Offline_APK_V1_2`; extract the ZIP artifact to obtain the installable `-debug.apk`.

The workflow will unpack the original source ZIP, verify the offline 3D model and PDF, compile Java with Android Gradle Plugin and Java 17, verify the debug APK's signature, then upload the APK.

**Build is not considered successful until Actions reports a green check.** If compilation fails, see job logs.

**Privacy warning:** This repository is **public**. Anyone can download the uploaded original ZIP, 3D models and app assets. If you do not wish to publish the project, change the repository visibility to Private before uploading.

The project is a concept demonstration; demo production data is not verified actual monthly output.
