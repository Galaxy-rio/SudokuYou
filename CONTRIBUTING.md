# Contributing to Sudoku You

Thank you for helping improve Sudoku You.

## Development setup

1. Install JDK 17 and an Android SDK containing Platform 37.
2. Clone the repository.
3. Create `local.properties` with your local `sdk.dir` if Android Studio has not generated it.
4. Run the checks:

```shell
./gradlew test
./gradlew lint
./gradlew assembleDebug
```

On Windows, use `gradlew.bat` instead of `./gradlew`.

## Contributions

- Keep changes focused and add tests for behavior changes where practical.
- Preserve the offline-first design and do not add proprietary SDKs, advertising, analytics, or tracking dependencies.
- Ensure new source code and assets are compatible with GPL-3.0-or-later, and document third-party assets in `NOTICE`.
- Update both English and Simplified Chinese resources when changing user-visible text.
- Do not commit signing keys, credentials, `local.properties`, generated APKs, or other secrets.

## Release checklist

- Increase `versionCode` monotonically and update `versionName`.
- Update `CHANGELOG.md` and both Fastlane changelog files.
- Run unit tests, lint, and a release build from a clean checkout.
- Create a Git tag prefixed with `v` after the release commit is final, for example `v2.0.0`.
- Verify the release APK signature and preserve the same release key for future updates.
