# Verification record

Run date: 16 October 2026, Asia/Jakarta.

Commands and device checks are reproducible from README.md and the repository test sources.

## Completed checks

- Dependency-free JVM suite: **15 search contract checks passed**. This covers case/whitespace handling, literal punctuation, combined type/favorite/source filters, no-match state, ordering, source non-mutation, Turkish-default-locale stability, metadata matching and exact color-tag matching.
- `assembleDebug assembleDebugAndroidTest assembleRelease bundleRelease lint --offline --console=plain --max-workers=2`: successful. Android lint: **0 errors, 9 warnings** (full-bleed legacy launcher shape and English UI string-resource recommendations).
- Device runner: `adb shell am instrument -w com.ptdeploy.cinelayer.test/com.ptdeploy.cinelayer.SmokeInstrumentation` passed on Android 15 / API 35, Google APIs x86_64 emulator. It opened the actual Activity, loaded four bundled samples, verified reactive and superseding queries, searched aspect metadata, selected a clip, and remuxed a real MP4.
- Trim test: requested 2,100–6,500 ms, aligned the start to the preceding 2,000 ms keyframe, verified output duration approximately 4,500 ms, retained two audio/video tracks, normalized first video timestamp to zero, decoded a frame, and verified the original SHA-256 was unchanged. Temporary test output was removed.
- System document-provider UI integration: selected `Import_Check.mp4` from Downloads using ACTION_OPEN_DOCUMENT; library grew from four to five items and opened the imported entry. Chose Export MP4, selected Downloads through ACTION_CREATE_DOCUMENT, and received the completion dialog. The saved file was 5,193,698 bytes and FFmpeg decoded its audio/video without errors. This checks real content-URI input and output through Android's provider rather than only direct file URIs.
- Soft keyboard integration: tapped the actual search field and typed `coast` through Android input. The field had focus, two matching items were returned, and both result cards including filenames/favorites were visible above the keyboard. On phones the introductory heading and bottom navigation collapse while typing and restore when the keyboard closes; query state is retained.
- Release APK verified with SDK 36 `apksigner verify --print-certs`. Public signer SHA-256: `eec0a05f309c612009647bdd216c96a40fdefef7828ac45f172ea6d0917d54ef`.
- Release AAB verified with JDK `jarsigner -verify`; it reports a valid JAR signature. The self-signed certificate and lack of timestamp are normal for this local Android signing key.
- Asset validator passed **18 image assets**: opaque RGB 512×512 icon, 1024×500 feature graphic, and 16 PNG screenshots below 8 MB each. Listing copy is 69 / 80 and 2,286 / 4,000 characters, excluding trailing file newlines. `store/asset-manifest.json` records exact dimensions, sizes and SHA-256 values.

## Screenshot capture

Each category has four actual app states: library, search, trim and favorites. All use the app's original bundled samples. The capture runner is in `app/src/androidTest/`; it changes only test Activity state and writes raw display captures. `tools/capture-screenshots.ps1` reproduces captures after installing debug and test APKs.

| Category | Pixels | Emulator density |
|---|---|---|
| Phone | 1080×1920 | 360 dpi |
| Desktop-sized Android layout | 1920×1080 | 160 dpi |
| 7-inch tablet category | 1920×1080 | 240 dpi |
| 10-inch tablet category | 1920×1080 | 200 dpi |

All 16 screenshots were visually inspected in a contact sheet. Initial captures containing an emulator System UI ANR dialog were discarded and replaced after recovery; the delivered captures contain no such dialog. The first cold boot was slow under host memory pressure. No reference APK was executed.

## Boundaries

This is not physical-device, Chromebook, native desktop, exhaustive codec, large-library performance, accessibility or all-Android-version certification. Android 9 compatibility is checked statically by lint but was not device-tested. Two measured debug cold launches took 6,612 and 9,065 ms on this emulator; injecting input before the first focused window caused a no-focused-window input timeout and ANR dialog. This is an observed startup/performance limitation under the test host conditions, not a proven device-independent diagnosis. Later interactions were tested after waiting for the Activity window. Physical-device startup/performance remains unverified. Cloud document providers may have their own permissions, network behavior and seekable-file limitations. Long-running exports are local foreground-app work without a persistent background service. Video cuts are keyframe-aligned, not frame-accurate transcoding. The Play privacy URL, account declarations, paid-app configuration, submission and approval remain external publication steps.
