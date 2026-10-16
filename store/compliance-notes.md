# Store listing and privacy release notes

Prepared October 16, 2026 for CineLayer Video Editor, developer **PT Deploy Dulu Pulang Nanti**, support **seandyadryan2@gmail.com**.

## Scope represented by the copy

The English listing describes selected-file import; local searches across filenames, notes, exact color-tag names, frame rates, and aspect ratios; source scopes (All sources / Imported / Samples); media-type filters; favorites; date sorting; editable notes and color tags in the preview menu; video/audio/image previews; and an individual video trim exported as MP4. It identifies bundled sample media, including the AI-generated coastal image, and the Android 9 minimum. The copy states the keyframe limitation and does not imply a multitrack editor, transitions, effects, compositing, or cloud service.

The privacy policy describes selected media access, persistable file references, locally processed search and metadata, editable notes and color tags, private local preferences, included samples, exports, provider behavior, retention, deletion, security, and optional support email. Before release, compare these statements to the final manifest, dependencies, app behavior, and backup rules. Support correspondence handling must match the developer's actual process.

## Play Console preparation

- Publish `privacy/index.html` at an active public HTTPS URL accessible without sign-in or geographic restrictions. A local file is not a hosted privacy URL. Enter the resulting URL in Play Console and keep privacy text or a working policy link accessible in the app.
- Complete the Data safety form against the final build, including any SDKs. Based on the described offline build, no data is sent off-device by the app to the developer. Google's definition generally excludes on-device-only processing; the developer must still check the final implementation and any user-selected provider behavior before submitting answers.
- The app has no accounts. Do not claim that it provides account deletion or a remote deletion service. Local app data and exported files have different deletion paths, described in the policy.
- Review the selected content rating, intended age audience, distribution countries, pricing, and all applicable Play Console declarations separately. This repository does not constitute a Play submission or guarantee approval.
- Keep screenshots faithful to the shipped interface and supported features. Do not label mockups or promotional artwork as captured app screenshots. Use English text throughout the listing graphics.
- Keep the app's brand distinct and use original code and artwork. Do not market it as an official LumaFusion product or imply affiliation.

## Official references checked

1. [Google Play User Data policy](https://support.google.com/googleplay/android-developer/answer/10144311?hl=en) requires a policy covering data handling, contact, retention/deletion and security. A policy must be accessible in the app and at a public, active, non-geofenced URL, with the app or developer identified. All apps need a policy, including apps without collection.
2. [Google Play Data safety guidance](https://support.google.com/googleplay/android-developer/answer/10787469?hl=en) defines collection as transmission off-device, explains on-device processing exclusions, and requires developers to account for third-party SDK behavior. Keep declarations consistent with the privacy policy.
3. [Android: access documents and other files](https://developer.android.com/training/data-storage/shared/documents-files) describes user-selected access through the Storage Access Framework, persistable URI permissions, and creating a document through the system picker. Access may fail if a selected document is moved or deleted.
4. [Google Play store listing setup](https://support.google.com/googleplay/android-developer/answer/9859152?hl=en) specifies maximum lengths of 30 characters for the app name, 80 for the short description, and 4,000 for the full description.
5. [Google Play preview asset guidance](https://support.google.com/googleplay/android-developer/answer/9866151?hl=en) documents listing-image formats and dimensions, including a 1024 × 500 feature graphic and device screenshot requirements. Validate output dimensions and file sizes before upload.

These are implementation and submission notes, not a finding that every jurisdiction's legal requirements have been met. Additional developer contact details or local disclosures may be needed for the countries in which the developer distributes the app.
