# Search behavior recovered from the supplied APK

Analysis date: 16 October 2026. Method: static REA/JADX inspection only. The patched APK was **never installed or executed**. The implementation in CineLayer must be original code; the snippets below are retained evidence, not reusable application source.

## Artifact identity and reproducibility

| Item | Observed value |
|---|---|
| Supplied file | `LumaFusion v1.0.68.4 (Patched)_Fix.apk` |
| SHA-256 | `12cc0b80519c0b8fc0bf838f88cf5ec1ca41dec3f19e1b35a930a8d45f79c6ba` |
| Package declaration | `com.luma_touch.lumafusion` |
| Version declaration | `1.0.68.4`, version code `94` |
| SDK declarations | minimum 29, target 34 |
| Parsed inventory | 12,732 classes; 2,216 resources |
| Engine | REA Android bridge; `jadx-headless-mcp` 0.7.1, Java 21.0.9, one worker |
| Engine JAR SHA-256 | `6e5eacf500b64292bfb73c49797c1958f6ee44646e43e868039ae7feb573ff75` |

Identity source: package summary `ev_858c6443764afdb18a8ba1353192cd78d5de2faa10797b2a2d07bf35dc838d6a`, retained full package record `ev_78aec4a84bad0b3a599cd03d6b3b89f9604c8aa0c0e74607c7f94004f007fe36`. The digest was independently recomputed using PowerShell `Get-FileHash`. The filename and manifest do not verify publisher authenticity; APK signatures were not verified.

The [complete REA evidence bundle](rea-search-evidence.json) preserves exact returned records, raw tool results, engine identity and limitations. [evidence-index.json](evidence-index.json) maps method records to readable files. Run `./analysis/extract-evidence.ps1` from PowerShell to reproduce readable snippets from the bundle. Each snippet starts with its full Evidence ID, artifact digest, DEX prototype, representation and coverage status. Source line numbers in these files are local evidence-file lines, not recovered original source positions.

## What the static evidence establishes

| Question | Finding and evidence | Confidence / boundary |
|---|---|---|
| How does typing enter search? | `GalleryFragment.o0` attaches `androidx.appcompat.widget.p2` as a TextWatcher to `binding.etSearchMedia`. Its `onTextChanged`, case 5, stores the query, closes the previous not-found panel, and calls `ti.vb.O0` for nonempty input while search is visible/enabled. [Listener attachment](snippets/com.solbegsoft.luma.ui.screens.gallery.GalleryFragment.o0.smali.txt), [watcher](snippets/androidx.appcompat.widget.p2.onTextChanged.java.txt). | Observed static call path. The fragment required smali fallback; watcher Java contains decompiler warnings. |
| Is search delayed? | `O0` cancels the previous job in `R`, then starts `ti.xa`. `xa.invokeSuspend` calls `yk.k.B(1000L, this)` before dispatch. `yk.k.B` schedules a coroutine continuation after the supplied duration. [O0](snippets/ti.vb.O0.java.txt), [worker](snippets/ti.xa.invokeSuspend.java.txt), [delay helper](snippets/yk.k.B.java.txt). | Strong inference: trailing debounce of approximately 1,000 ms, supported by cancellation plus suspension. No runtime timing measurement. |
| What are the scopes? | `SearchMediaLibrary` has `Current`, `All`, `StoryblockFolder`. The enum-switch map explicitly maps those to cases 1, 2 and 3. Worker calls `vb.y`, `vb.x`, `vb.z` respectively. [Switch map](snippets/ti.wa._clinit_.smali.txt), [worker](snippets/ti.xa.invokeSuspend.java.txt). | Observed declarations and dispatch. Scope names alone do not prove exact folder traversal. |
| What does Current include? | `vb.y` branches on active main source: Files, Imported, Media, Transition, Storyblocks or Titles, then calls the corresponding search routine. [Dispatcher](snippets/ti.vb.y.java.txt). | Observed static branches; source-specific traversal and permission outcomes were not exhaustively traced. |
| What does All do? | `vb.x` obtains Storyblocks candidates (`oa.z`), then runs `oa.j0`, which launches six independent candidate jobs and flattens their lists. [Dispatcher](snippets/ti.vb.x.java.txt), [aggregator](snippets/oa.j0.invokeSuspend.java.txt). | Observed aggregation. Each of the six jobs and all remote/provider behavior are outside this bounded trace. All must not be described as a verified device-wide index. |
| Does changing scope rerun the query? | `R0(String, SearchMediaLibrary)` changes scope/tooltip state, reads the existing query, and invokes `O0` if its length is greater than zero. [R0](snippets/ti.vb.R0.java.txt). | Observed static code. |
| What does Enter do? | The gallery branch of `ag.a.onKey` handles key code 66 and invokes a UI helper for the edit field on key-down, consuming Enter key-down/up. It does not directly invoke the search routine. [Key handler](snippets/ag.a.onKey.java.txt). | Observed absence of search dispatch in this handler; keyboard-dismiss purpose is an inference until that UI helper is traced. |
| What happens on clear? | The watcher calls `Q0()` for an empty query. `Q0` cancels `R`, compares the current list with the gallery stack, and schedules `y6` when restoration is needed. `F0` resets the query to empty, closes the not-found panel, uses the media-source icon, and selects single-selection mode. [Watcher](snippets/androidx.appcompat.widget.p2.onTextChanged.java.txt), [Q0](snippets/ti.vb.Q0.java.txt), [F0](snippets/ti.vb.F0.java.txt). | Observed reset path; final rendered restoration was not observed. |
| How is no result represented? | Current-source and All dispatchers test `list.isEmpty()` and use `NotFoundPanelState.Show` with resource `search_did_not_find_any_matches`; nonempty lists are routed to gallery rendering. [Current](snippets/ti.vb.y.java.txt), [All](snippets/ti.vb.x.java.txt). | Observed named resource and state transitions. Some decompiled All control flow is inconsistent, so do not treat the decompiled Java as executable source. |

## Matching semantics

The common local-media filter is `oa.m0.c(String, List)`. Files, Imported and Media paths eventually call it: [Files](snippets/oa.m0.h.java.txt), [Imported](snippets/oa.m0.j.java.txt), [Media](snippets/oa.m0.l.java.txt).

| Field | Matching operation observed | Evidence |
|---|---|---|
| Media title | Case-insensitive substring | [oa.m0.c](snippets/oa.m0.c.java.txt), `mn.o.l2(title, query, true)` |
| Media notes | Case-insensitive substring | [oa.m0.c](snippets/oa.m0.c.java.txt), `mn.o.l2(notes, query, true)` |
| Video frame rate | Case-insensitive substring against string form of frame rate | [oa.m0.c](snippets/oa.m0.c.java.txt), `String.valueOf(getFrameRate())` |
| Video aspect ratio | Case-insensitive substring against localized short-name resource | [oa.m0.c](snippets/oa.m0.c.java.txt), `getAspectRatioType().getShortNameRes()` |
| Media color tag | Exact case-insensitive equality against color-tag class simple name | [oa.m0.c](snippets/oa.m0.c.java.txt), [equality helper](snippets/mn.o.p2.java.txt) |
| Marker names and marker color names | Case-insensitive substring calls exist for video/audio marker names and lowercased marker color class names | [oa.m0.c](snippets/oa.m0.c.java.txt). **The recovered marker boolean control flow is inconsistent; these calls establish intended searchable fields, but their complete predicate is only partially recovered.** |
| Title presets | Name/notes substring or exact color-tag equality | [oa.m0.e](snippets/oa.m0.e.java.txt) |
| Transitions | Group-name, effect-name and stored notes substring calls | [oa.m0.d](snippets/oa.m0.d.java.txt). Group inclusion control flow has decompiler ambiguity. |

The main predicate combines its checks with logical OR. An item can match one supported field. There is no tokenization, stemming, regex, fuzzy ranking or Boolean query parser in the traced local filter. That statement is bounded to this routine, not every provider.

`mn.o.l2` is the substring/contains helper: it succeeds when an index lookup is nonnegative. It routes through `u2` → `r2` → `s2` for the case-insensitive path. `mn.o.p2` directly uses `String.equalsIgnoreCase` when passed `true`. [Contains](snippets/mn.o.l2.java.txt), [index wrapper](snippets/mn.o.u2.java.txt), [index implementation](snippets/mn.o.r2.java.txt), [scan implementation](snippets/mn.o.s2.java.txt), [exact equality](snippets/mn.o.p2.java.txt).

At the UI boundary, `c5.a.s1` does only `null ? "" : toString()`. No trimming, token splitting or minimum length beyond nonempty input appears on that path. Whitespace is therefore passed through by this entry point. This does not prove every downstream provider preserves it. [Input conversion](snippets/c5.a.s1.java.txt), [watcher](snippets/androidx.appcompat.widget.p2.onTextChanged.java.txt).

Short evidence excerpts, with full surrounding context retained in the linked files:

```java
// ti.vb.O0: cancel previous work, retain the selected scope.
if (v1Var != null) { v1Var.a(null); }
this.R = fl.e0.j1(..., new xa(this, e5Var.f22388n, str, null), 2);

// ti.xa.invokeSuspend: wait before dispatching the query.
if (yk.k.B(1000L, this) == aVar) { return aVar; }

// oa.m0.c: representative field matching.
boolean zL4 = mn.o.l2(galleryMediaModel.getTitle(), str, true);
boolean zL5 = mn.o.l2(galleryMediaModel.getNotes(), str, true);
```

## Feature specification for an original CineLayer implementation

Carry forward the evidenced interaction: a media-library search field, search in a selected scope, query retained when the scope changes, cancellable delayed search, metadata matching, clear/reset, and an explicit empty state. Keep a local query over imported media; do not reuse LumaFusion classes, resources, icons, branding, data, network credentials or services.

Use an original `MediaItem` model with title, notes, folder, type, color, frame-rate/aspect metadata and markers. Match title/notes/marker names and video metadata by case-insensitive substring, and color by case-insensitive equality. Make the marker OR behavior explicit and test it, since the reference decompilation is ambiguous there. Filter scope first, then match the query; cancel stale pending results when input, scope or filters change.

The following are **product design choices**, not claims about the APK: All media versus Current folder wording, media-type chips, result count, accent-insensitive matching, explicit quoted phrases, relevance sorting, a shorter debounce, and a persistent recent-query list. If any are implemented, document them as CineLayer behavior. In particular, scope Current in the APK dispatches by source; equating it with Current folder is a useful adaptation, not a proven exact clone.

## Remaining limits and question checklist

- Answered: artifact identity; search input listener; cancellation and 1,000 ms delay; declared scopes and dispatch; common local searchable fields; substring/equality distinction; scope change; empty-query and empty-result paths.
- Partially answered: complete marker/group predicates because Java output contains inconsistent boolean flow; complete meaning/traversal of each provider scope; All aggregation's individual jobs; Storyblocks folder/remote search.
- Unresolved: runtime appearance, animation, actual timing and keyboard behavior, performance on large libraries, runtime permission effects, Unicode/locale edge cases, exact backend ranking/pagination/error handling, authenticity and differences introduced by the patch, native/reflection/dynamic-code behavior.

No live backend was called and no runtime trace was collected. Decompiled Java is a recovered representation and may contain wrong local names or control flow. The retained smali fallback is explicitly marked where used. A reported source status of complete means the returned method text was not truncated; it does not mean decompilation is semantically perfect.
