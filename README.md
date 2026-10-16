# CineLayer Video Editor

An original, offline Android media browser with metadata search, previews, favorites, and single-video keyframe trimming. Android 9 or newer; Java 17, Android SDK 36, Gradle 8.14, Android Gradle Plugin 8.11.1. No third-party runtime dependencies, network permission, ads, analytics, accounts or subscriptions.

## Use the app

1. Explore the four included **Sample** items, or tap **Import** to select images, audio and videos through Android's document picker. Imported originals stay in their original location.
2. Search filenames, notes, available frame-rate/aspect-ratio metadata, or a full color tag such as `Purple`. Choose All sources, Imported or Samples, then optionally apply media-type or favorite filters. Queries use literal case-insensitive substring matching; color tags use exact case-insensitive matching.
3. Open an item for preview. The overflow menu edits notes/color tags, toggles favorites, or removes the library entry without deleting the original.
4. For video, adjust the In/Out range and choose **Export MP4**. Choose a writable destination in Android's file picker. Export preserves supported original compressed video/audio tracks and rotation metadata. The start snaps back to the preceding keyframe, so it can be earlier than the chosen In point. The app does not transcode incompatible codecs.
5. Settings includes the complete bundled privacy policy and support email.

This is a media-search and single-clip trimming release. Multi-track composition, transitions, text overlays, markers, cloud stock libraries and transcoding are not implemented. It does not reproduce LumaFusion's complete editor or UI.

## Evidence and original implementation

[APK search investigation](analysis/search-evidence.md) identifies the supplied artifact by SHA-256 and links retained REA evidence and readable snippets. The reference APK was inspected statically, never executed. Decompiled control-flow limitations are explicit. The app contains original code and original sample/brand artwork; no reference APK classes, resources, assets or services are used.

The raw REA bundle and decompiled snippets are retained locally and excluded from Git. The committed report includes the artifact digest, Evidence IDs, short excerpts and conclusions; its full-evidence links resolve in this original workspace. This avoids distributing recovered commercial application source with the original CineLayer project.

| Reference evidence | CineLayer adaptation |
|---|---|
| Text-change listener, cancel earlier delayed work, approximately 1,000 ms delay | TextWatcher with a cancellable 120 ms trailing delay; local in-memory filtering |
| All / Current / StoryblockFolder scopes | All sources / Imported / Samples; source change retains and reruns query |
| Title, notes, frame rate, aspect ratio substring; exact color tags | Filename/notes/available technical metadata substring and exact color tags |
| Clear/restoration and explicit not-found state | Clear button, Reset search, result count and empty-state guidance |

CineLayer additionally trims surrounding query whitespace, uses `Locale.ROOT` for stable casing, and provides media-type/favorite filters and Recent/Name/Duration sorting. These are product choices, not recovered reference behavior. It has no fuzzy matching, token parser or remote search. Video frame rate is searchable only when Android exposes the capture frame-rate metadata; it is not estimated from samples.

## Build

Set `JAVA_HOME` to a JDK 17+ (tested with Android Studio's JBR 21). Install SDK platform 36 and build tools. Set `ANDROID_HOME`, or create an ignored `local.properties` with your escaped SDK path:

```properties
sdk.dir=C\:/Users/YOU/AppData/Local/Android/Sdk
```

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat assembleRelease bundleRelease lint
```

Release signing reads ignored `keystore.properties`:

```properties
storeFile=D:/KEYSTORE/CineLayer_Video_Editor.jks
storePassword=YOUR_LOCAL_PASSWORD
keyAlias=cinelayer
keyPassword=YOUR_LOCAL_PASSWORD
```

The requested JKS was created at that path. Keep its password and backups outside version control. Release outputs are `app/build/outputs/apk/release/app-release.apk` and `app/build/outputs/bundle/release/app-release.aab`. The application ID is `com.ptdeploy.cinelayer`; confirm its availability before first Play publication. Version is 1.0.0 (code 1).

## Verification

```powershell
New-Item -ItemType Directory -Force .local/tests
javac -d .local/tests app/src/main/java/com/ptdeploy/cinelayer/MediaItem.java app/src/main/java/com/ptdeploy/cinelayer/SearchEngine.java tests/SearchEngineTest.java
java -cp .local/tests SearchEngineTest
.\gradlew.bat connectedDebugAndroidTest
```

The dependency-free JVM checks cover combined query/type/favorite/source filters, literal punctuation, numeric sorting, non-mutation, stable casing, notes, technical metadata and exact versus partial color tags. Device instrumentation checks reactive search, replacing stale queries, real Activity selection, sample loading, original-file integrity and a decodable MP4 trim retaining audio/video tracks with normalized timestamps. See [verification record](docs/verification.md) for actual run results and boundaries.

## Play Store materials

- `store/listing/short-description.txt` and `full-description.txt`: English copy within 80/4,000 characters.
- `store/icon/icon-512.png`: 512 × 512 opaque PNG.
- `store/feature-graphic.png`: 1024 × 500 PNG.
- `store/screenshots/phone/`: phone screenshots, 1080 × 1920.
- `store/screenshots/desktop/`: Android large-screen layout captures, 1920 × 1080. These are not evidence of a native Windows/macOS build.
- `store/screenshots/tablet-7/` and `tablet-10/`: independently captured Android tablet layouts, 1920 × 1080.
- `privacy/index.html`: public-hosting-ready English policy, also bundled in the app.

The screenshots are captured from the built Android app using original included samples; they are not generated mock interfaces. [Asset provenance](store/artwork-provenance.md) documents generated artwork and packaging. [Compliance notes](store/compliance-notes.md) document remaining Play Console steps. The privacy file must be hosted at a public URL before publication. Preparing files does not publish the app, create paid-app settings, or submit a Play listing.

`tools/package_delivery.py` copies signed release outputs and packages `deliverables/CineLayer-Play-Store-Assets.zip` and `CineLayer-Search-Evidence.zip` locally, with SHA-256 checksums. Delivery archives and binaries are excluded from Git. The English [upload guide](store/UPLOAD.md) maps every Play Console asset category to its files.
