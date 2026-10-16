# Original artwork and screenshot provenance

Created 16 October 2026 using the built-in imagegen tool. No artwork, branding, code, footage or services were extracted from the reference APK for inclusion in this app.

Generated source masters are retained under `store/icon/icon-master.png` and `store/source-art/`. The app icon is packaged as opaque 512×512 PNG for Google Play and resized to Android launcher density buckets. The feature graphic is normalized to exactly 1024×500. Normalization changes output pixel dimensions and encoding only. The original generated masters remain unchanged.

## Prompt set

**Icon — logo-brand:** A polished minimal professional square icon for CineLayer, a video library and clip trimming app. Full-bleed dark midnight navy. A geometric ribbon forms a forward play triangle from layered film strips, mint turquoise and icy blue with a restrained violet accent. Crisp dimensional creative-software brand, recognizable at small sizes. No text, existing logos, rounded exterior corners, phone or border. Output: `store/icon/icon-master.png`.

**Sample photo — photorealistic-natural:** An original cinematic aerial travel photograph of a secluded Mediterranean cove: aquamarine sea, white waves, pale limestone cliffs, dark green coastal pines, winding coast road and distant sailboat in warm late-afternoon sunlight. Wide 16:9, realistic editorial travel photography. No text, watermarks, UI or logos. Output: `store/source-art/coast-master.png`.

**Feature graphic — ads-marketing, icon reference:** Premium CineLayer Video Editor Play feature graphic, target 1024×500. Reuse the generated mint/blue/violet film ribbon brand identity on the right. Midnight navy, subtle layered film panels and cinematic light. Exact English copy: “CineLayer”, “Find your shot. Shape your story.” and “SEARCH • PREVIEW • TRIM”. No invented screens, store badges, awards, prices or additional text. Output: `store/source-art/feature-master.png`.

## Bundled samples

`tools/prepare_samples.py` packages the generated coastal photograph, encodes an eight-second 24 fps H.264/AAC example video, creates an original eight-second animated gradient video with FFmpeg, and synthesizes an eight-second soft audio tone. Samples are explicitly marked **Sample** in the app and are optional; removing them from the library does not remove imported originals. These demonstrate app workflows and are not user projects or advertised stock-media access.

## Screenshots

All delivered screenshots are direct, unretouched Android emulator captures from the app through `SmokeInstrumentation`'s `capture` mode. They use the included original samples. No screenshot UI was generated with imagegen. Four states per device category: library, filename search, video trimming and favorites.

The desktop folder captures the responsive Android app at a desktop-sized viewport; it does not represent a separate desktop executable or a verified Chromebook runtime. Tablet categories use different emulator density settings. A physical 7-inch/10-inch device was not used. Refer to `docs/verification.md` for capture sizes and device details.
