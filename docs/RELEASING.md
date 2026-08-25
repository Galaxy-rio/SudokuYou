# Release guide

This guide records the release identity and signing expectations for Sudoku You.

## Stable application identity

- Display name: `Sudoku You`
- Android application ID: `com.galaxyrio.sudokusolver`
- First stable version: `2.0.0` (`versionCode` 2)
- Stable Git tag format: exact version name, for example `2.0.0`

Do not change the application ID or release signing key for a normal update. Either change would prevent existing installations from updating in place.

## Versioning

For every release:

1. Increase `versionCode` to a value greater than every previously published APK.
2. Set the user-visible `versionName`.
3. Update `CHANGELOG.md` and each locale's `fastlane/metadata/android/<locale>/changelogs/<versionCode>.txt`.
4. Commit and test the final source.
5. Create and push a Git tag exactly matching `versionName`.

## Signing identity

The APKs published for the alpha releases were audited on 2026-08-24. They all use the same valid release certificate:

```text
Subject: CN=galaxyrio
Algorithm: RSA 2048
Certificate SHA-256:
2afeef946b759ec1ed620c9b98166a0dc0d2589a6ddc398cef0d745e04bbddaf
```

Keep the original keystore, key alias, and passwords backed up securely in more than one offline location. Never commit them to Git or place credentials directly in Gradle files.

Verify a signed APK before publishing:

```shell
apksigner verify --verbose --print-certs app-release.apk
```

Confirm that verification succeeds and that the certificate SHA-256 fingerprint exactly matches the value above.

### Reproducibility audit

The signed GitHub APK for tag `1.1.0-alpha` (commit
`944135a476214d44984139361524e158d12152ab`) was reproduced from a clean
checkout on 2026-08-24. `apksigcopier compare --unsigned` accepted the clean
build, and copying the release APK's v2 signing block onto that build produced
an APK byte-for-byte identical to the GitHub asset:

```text
GitHub/reconstructed APK SHA-256:
e72a5154f0fc6ece790ea44b626c764ede63b7a676acfdeabb67601e162126a8
```

This demonstrates reproducibility for that tag on Windows with JDK 21 and
Gradle 9.6.1. It does not replace a final check of the `2.0.0` tag in F-Droid's
Linux build environment. Repeat the comparison for every developer-signed
stable release before requesting reproducible-build publication.

## Build checks

From a clean checkout, run:

```shell
./gradlew test lint assembleRelease
```

The repository's release build is intentionally unsigned. Sign the resulting APK only through the protected release process, then verify the final APK again.

## GitHub repository rename

After renaming the GitHub repository to `SudokuYou`, update the local remote:

```shell
git remote set-url origin https://github.com/Galaxy-rio/SudokuYou.git
git remote -v
```

GitHub normally redirects the old URL, but F-Droid metadata should use the final canonical URL.

## F-Droid signing choice

The root `.fdroid.yml` describes the source build and stable tag. It does not currently request publishing the developer-signed GitHub APK because no signed `2.0.0` asset exists yet.

- With default F-Droid signing, F-Droid builds and signs its own APK. This is the simplest path, but GitHub and F-Droid APKs cannot update each other in place.
- With a verified reproducible build, F-Droid can publish the developer-signed APK after rebuilding and comparing it. The historical `1.1.0-alpha` audit passed, but only add `Binaries` and `AllowedAPKSigningKeys` after the final signed `2.0.0` asset has also passed on the intended F-Droid build environment.

Never send the private signing key to F-Droid or any other third party.
