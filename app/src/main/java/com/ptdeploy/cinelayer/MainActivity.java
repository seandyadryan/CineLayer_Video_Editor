package com.ptdeploy.cinelayer;

import android.app.*;
import android.content.*;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.*;
import android.provider.*;
import android.text.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import org.json.*;

public class MainActivity extends Activity {
  static final int BG = Color.rgb(12, 16, 23),
      PANEL = Color.rgb(20, 27, 38),
      LINE = Color.rgb(38, 49, 65),
      WHITE = Color.rgb(238, 244, 248),
      MUTED = Color.rgb(143, 158, 178),
      MINT = Color.rgb(142, 230, 207);
  final ArrayList<MediaItem> library = new ArrayList<>();
  final Handler handler = new Handler(Looper.getMainLooper());
  final ExecutorService worker = Executors.newFixedThreadPool(2);
  final android.util.LruCache<String, Bitmap> thumbs =
      new android.util.LruCache<>(24 * 1024) {
        protected int sizeOf(String key, Bitmap value) {
          return value.getByteCount() / 1024;
        }
      };
  LinearLayout root, content, results, nav, previewPanel;
  TextView count, trimLabel;
  VideoView video;
  ImageView still;
  EditText search;
  MediaItem selected;
  String query = "", type = "All", sort = "Recent", page = "Library", scope = "All sources";
  boolean favorites = false, wide = false, exporting = false, playing = false, loading = false;
  long trimStart = 0, trimEnd = 0;
  Runnable pending;

  @Override
  public void onCreate(Bundle state) {
    super.onCreate(state);
    getWindow().setStatusBarColor(BG);
    getWindow().setNavigationBarColor(BG);
    loadLibrary();
    build();
  }

  int dp(float v) {
    return Math.round(v * getResources().getDisplayMetrics().density);
  }

  TextView text(String value, int size, int color) {
    TextView t = new TextView(this);
    t.setText(value);
    t.setTextSize(size);
    t.setTextColor(color);
    t.setGravity(Gravity.CENTER_VERTICAL);
    return t;
  }

  GradientDrawable shape(int color, int radius) {
    GradientDrawable d = new GradientDrawable();
    d.setColor(color);
    d.setCornerRadius(dp(radius));
    return d;
  }

  LinearLayout col() {
    LinearLayout l = new LinearLayout(this);
    l.setOrientation(LinearLayout.VERTICAL);
    return l;
  }

  LinearLayout row() {
    LinearLayout l = new LinearLayout(this);
    l.setGravity(Gravity.CENTER_VERTICAL);
    return l;
  }

  void pad(View v, int p) {
    v.setPadding(dp(p), dp(p), dp(p), dp(p));
  }

  TextView button(String label, boolean primary, Runnable action) {
    TextView b = text(label, 13, primary ? BG : WHITE);
    b.setTypeface(null, Typeface.BOLD);
    b.setGravity(Gravity.CENTER);
    b.setPadding(dp(15), dp(11), dp(15), dp(11));
    b.setBackground(shape(primary ? MINT : PANEL, 10));
    b.setOnClickListener(v -> action.run());
    return b;
  }

  void spacer(LinearLayout p, int height) {
    p.addView(new View(this), new LinearLayout.LayoutParams(1, dp(height)));
  }

  void grow(LinearLayout parent, View child) {
    parent.addView(child, new LinearLayout.LayoutParams(0, -2, 1));
  }

  void build() {
    stopPlayback();
    wide = getResources().getConfiguration().screenWidthDp >= 840;
    root = col();
    root.setBackgroundColor(BG);
    root.setPadding(dp(20), 0, dp(20), 0);
    root.setOnApplyWindowInsetsListener(
        (v, insets) -> {
          int left, right, top, bottom;
          if (Build.VERSION.SDK_INT >= 30) {
            android.graphics.Insets bars = insets.getInsets(WindowInsets.Type.systemBars());
            left = bars.left;
            right = bars.right;
            top = bars.top;
            bottom = Math.max(bars.bottom, insets.getInsets(WindowInsets.Type.ime()).bottom);
          } else {
            left = insets.getSystemWindowInsetLeft();
            right = insets.getSystemWindowInsetRight();
            top = insets.getSystemWindowInsetTop();
            bottom = insets.getSystemWindowInsetBottom();
          }
          root.setPadding(dp(20) + left, top + dp(12), dp(20) + right, bottom);
          return insets;
        });
    setContentView(root);
    LinearLayout header = row();
    TextView brand = text("◈  CineLayer", 25, WHITE);
    brand.setTypeface(null, Typeface.BOLD);
    grow(header, brand);
    header.addView(button("+ Import", true, this::importMedia));
    root.addView(header);
    spacer(root, 5);
    TextView tagline = text("A little order. More creative flow.", 12, MUTED);
    root.addView(tagline);
    spacer(root, 20);
    content = col();
    root.addView(content, new LinearLayout.LayoutParams(-1, 0, 1));
    if (page.equals("Settings")) settings();
    else if (page.equals("Editor") && !wide) editor(content);
    else libraryView();
    nav = row();
    nav.setPadding(0, dp(12), 0, dp(12));
    for (String tab : new String[] {"Library", "Editor", "Settings"}) {
      TextView b =
          button(
              (tab.equals("Library") ? "▦  " : tab.equals("Editor") ? "▷  " : "⚙  ") + tab,
              page.equals(tab),
              () -> {
                page = tab;
                build();
              });
      nav.addView(b, new LinearLayout.LayoutParams(0, dp(44), 1));
    }
    root.addView(nav);
  }

  void libraryView() {
    LinearLayout heading = row();
    TextView title = text("Your media, in focus.", wide ? 30 : 26, WHITE);
    title.setTypeface(null, Typeface.BOLD);
    grow(heading, title);
    content.addView(heading);
    spacer(content, 5);
    content.addView(text("Find the right shot. Keep the story moving.", 13, MUTED));
    spacer(content, 18);
    LinearLayout split = row();
    split.setGravity(Gravity.TOP);
    content.addView(split, new LinearLayout.LayoutParams(-1, 0, 1));
    LinearLayout browser = col();
    split.addView(browser, new LinearLayout.LayoutParams(0, -1, wide ? 1.15f : 1));
    LinearLayout searchRow = row();
    searchRow.setBackground(shape(PANEL, 12));
    TextView glass = text("⌕", 24, MINT);
    pad(glass, 10);
    searchRow.addView(glass);
    search = new EditText(this);
    search.setSingleLine(true);
    search.setTextColor(WHITE);
    search.setHintTextColor(MUTED);
    search.setHint("Search media, notes, tags…");
    search.setTextSize(15);
    search.setBackgroundColor(Color.TRANSPARENT);
    search.setText(query);
    search.setContentDescription("Search media filenames notes and tags");
    searchRow.addView(search, new LinearLayout.LayoutParams(0, dp(50), 1));
    TextView clear =
        button(
            "×",
            false,
            () -> {
              search.setText("");
            });
    clear.setContentDescription("Clear search");
    searchRow.addView(clear);
    browser.addView(searchRow);
    spacer(browser, 12);
    search.addTextChangedListener(
        new TextWatcher() {
          public void beforeTextChanged(CharSequence s, int st, int c, int a) {}

          public void onTextChanged(CharSequence s, int st, int before, int c) {
            query = s.toString();
            if (pending != null) handler.removeCallbacks(pending);
            pending = () -> renderResults();
            handler.postDelayed(pending, 120);
          }

          public void afterTextChanged(Editable e) {}
        });
    HorizontalScrollView filterScroll = new HorizontalScrollView(this);
    filterScroll.setHorizontalScrollBarEnabled(false);
    LinearLayout filters = row();
    for (String category : new String[] {"All", "Video", "Audio", "Image"}) {
      TextView b =
          button(
              category,
              category.equals(type),
              () -> {
                type = category;
                build();
              });
      LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(-2, dp(38));
      bp.setMargins(0, 0, dp(6), 0);
      filters.addView(b, bp);
    }
    filterScroll.addView(filters);
    browser.addView(filterScroll);
    spacer(browser, 14);
    LinearLayout tools = row();
    count = text("", 12, MUTED);
    grow(tools, count);
    browser.addView(
        button(
            "⌖  " + scope,
            false,
            () ->
                new AlertDialog.Builder(this)
                    .setTitle("Search in")
                    .setItems(
                        new String[] {"All sources", "Imported", "Samples"},
                        (d, i) -> {
                          scope = new String[] {"All sources", "Imported", "Samples"}[i];
                          build();
                        })
                    .show()));
    spacer(browser, 6);
    tools.addView(
        button(
            favorites ? "★ Saved" : "☆ Saved",
            favorites,
            () -> {
              favorites = !favorites;
              build();
            }));
    TextView sortButton =
        button(
            "↕ " + sort,
            false,
            () ->
                new AlertDialog.Builder(this)
                    .setTitle("Sort media")
                    .setItems(
                        new String[] {"Recent", "Name", "Duration"},
                        (d, i) -> {
                          sort = new String[] {"Recent", "Name", "Duration"}[i];
                          build();
                        })
                    .show());
    tools.addView(sortButton);
    browser.addView(tools);
    spacer(browser, 12);
    ScrollView scroll = new ScrollView(this);
    scroll.setClipToPadding(false);
    results = col();
    scroll.addView(results);
    browser.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
    renderResults();
    if (wide) {
      View gap = new View(this);
      split.addView(gap, new LinearLayout.LayoutParams(dp(24), 1));
      LinearLayout right = col();
      split.addView(right, new LinearLayout.LayoutParams(0, -1, 1));
      editor(right);
    }
  }

  void renderResults() {
    if (results == null || count == null) return;
    results.removeAllViews();
    List<MediaItem> list = SearchEngine.filter(library, query, type, favorites, sort, scope);
    count.setText(
        list.size()
            + " media  ·  "
            + (query.trim().isEmpty() ? "Local library" : "Search results"));
    if (list.isEmpty()) {
      LinearLayout empty = col();
      pad(empty, 26);
      empty.setBackground(shape(PANEL, 14));
      empty.addView(text("⌕", 42, MINT));
      empty.addView(text("No matching media", 20, WHITE));
      empty.addView(text("Try another term or clear your filters.", 13, MUTED));
      spacer(empty, 18);
      empty.addView(
          button(
              "Reset search",
              true,
              () -> {
                query = "";
                type = "All";
                favorites = false;
                scope = "All sources";
                build();
              }));
      results.addView(empty);
      return;
    }
    int columns = wide ? 3 : 2;
    for (int i = 0; i < list.size(); i += columns) {
      LinearLayout r = row();
      r.setGravity(Gravity.TOP);
      for (int j = 0; j < columns; j++) {
        if (i + j >= list.size()) {
          r.addView(new View(this), new LinearLayout.LayoutParams(0, 1, 1));
          continue;
        }
        MediaItem item = list.get(i + j);
        LinearLayout card = col();
        card.setBackground(shape(PANEL, 12));
        pad(card, 7);
        FrameLayout frame = new FrameLayout(this);
        ImageView thumb = new ImageView(this);
        thumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
        thumb.setBackground(shape(LINE, 8));
        frame.addView(thumb, new FrameLayout.LayoutParams(-1, -1));
        loadThumb(item, thumb);
        TextView badge =
            text(
                item.type.equals("Video")
                    ? "▶ " + time(item.duration)
                    : item.type.equals("Audio") ? "♫ " + time(item.duration) : "▧ Image",
                10,
                WHITE);
        badge.setPadding(dp(6), dp(3), dp(6), dp(3));
        badge.setBackground(shape(0xCF0C1017, 5));
        FrameLayout.LayoutParams bp =
            new FrameLayout.LayoutParams(-2, dp(24), Gravity.BOTTOM | Gravity.START);
        bp.setMargins(dp(5), 0, 0, dp(5));
        frame.addView(badge, bp);
        card.addView(frame, new LinearLayout.LayoutParams(-1, dp(wide ? 104 : 96)));
        spacer(card, 5);
        TextView name = text(item.name, 12, WHITE);
        name.setMaxLines(1);
        name.setEllipsize(TextUtils.TruncateAt.END);
        card.addView(name);
        LinearLayout meta = row();
        grow(
            meta,
            text(item.sample ? "Sample · " + item.type : "Imported · " + item.type, 10, MUTED));
        TextView star = text(item.favorite ? "★" : "☆", 21, item.favorite ? MINT : MUTED);
        star.setContentDescription("Favorite " + item.name);
        star.setPadding(dp(8), 0, dp(5), 0);
        star.setOnClickListener(
            v -> {
              item.favorite = !item.favorite;
              saveLibrary();
              renderResults();
            });
        meta.addView(star);
        card.addView(meta);
        card.setContentDescription("Open " + item.name);
        card.setOnClickListener(
            v -> {
              select(item);
              if (!wide) page = "Editor";
              build();
            });
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(0, -2, 1);
        cp.setMargins(0, 0, j < columns - 1 ? dp(10) : 0, dp(10));
        r.addView(card, cp);
      }
      results.addView(r);
    }
  }

  void select(MediaItem item) {
    selected = item;
    trimStart = 0;
    trimEnd = item.duration;
    loadTrim();
  }

  void editor(LinearLayout host) {
    ScrollView editorScroll = new ScrollView(this);
    previewPanel = col();
    editorScroll.addView(previewPanel, new ScrollView.LayoutParams(-1, -2));
    host.addView(editorScroll, new LinearLayout.LayoutParams(-1, -1));
    if (selected == null) {
      LinearLayout empty = col();
      pad(empty, 24);
      empty.setBackground(shape(PANEL, 14));
      empty.addView(text("THE PREVIEW ROOM", 11, MINT));
      spacer(empty, 14);
      empty.addView(text("Every story starts\nwith a single clip.", 26, WHITE));
      spacer(empty, 12);
      empty.addView(
          text(
              "Select media from your library to preview,\n"
                  + "trim a video, and export your next idea.",
              13,
              MUTED));
      spacer(empty, 20);
      empty.addView(
          button(
              "Browse library",
              true,
              () -> {
                page = "Library";
                build();
              }));
      previewPanel.addView(empty);
      return;
    }
    LinearLayout title = row();
    grow(title, text("PREVIEW ROOM", 11, MINT));
    title.addView(button("•••", false, this::mediaMenu));
    previewPanel.addView(title);
    spacer(previewPanel, 10);
    FrameLayout preview = new FrameLayout(this);
    preview.setBackground(shape(Color.BLACK, 14));
    if (selected.type.equals("Image")) {
      still = new ImageView(this);
      still.setScaleType(ImageView.ScaleType.FIT_CENTER);
      preview.addView(still, new FrameLayout.LayoutParams(-1, -1));
      loadThumb(selected, still);
    } else {
      video = new VideoView(this);
      FrameLayout.LayoutParams vp = new FrameLayout.LayoutParams(-1, -1, Gravity.CENTER);
      preview.addView(video, vp);
      video.setVideoURI(Uri.parse(selected.uri));
      video.setOnPreparedListener(
          mp -> {
            mp.setOnSeekCompleteListener(p -> {});
            video.seekTo((int) trimStart);
            mp.setOnCompletionListener(
                m -> {
                  playing = false;
                });
          });
      video.setOnErrorListener(
          (mp, w, e) -> {
            toast("This media cannot be played. Try another format or import it again.");
            return true;
          });
      if (selected.type.equals("Audio")) {
        TextView art = text("♫", 80, MINT);
        art.setGravity(Gravity.CENTER);
        preview.addView(art, new FrameLayout.LayoutParams(-1, -1));
      }
    }
    int previewHeight =
        (int)
            Math.min(
                wide ? 340 : 190,
                (getResources().getConfiguration().screenWidthDp - (wide ? 85 : 40))
                    * (wide ? .43 : .5625));
    previewPanel.addView(preview, new LinearLayout.LayoutParams(-1, dp(previewHeight)));
    spacer(previewPanel, 10);
    TextView filename = text(selected.name, 17, WHITE);
    filename.setMaxLines(1);
    filename.setEllipsize(TextUtils.TruncateAt.END);
    filename.setTypeface(null, Typeface.BOLD);
    previewPanel.addView(filename);
    spacer(previewPanel, 4);
    previewPanel.addView(
        text(
            selected.type + "  /  " + (selected.sample ? "Included sample" : "On your device"),
            11,
            MUTED));
    spacer(previewPanel, 12);
    if (!selected.type.equals("Image")) {
      LinearLayout transport = row();
      TextView play =
          button(
              "▶  Play",
              true,
              () -> {
                if (video == null) return;
                if (video.isPlaying()) {
                  video.pause();
                  playing = false;
                } else {
                  if (video.getCurrentPosition() >= trimEnd) video.seekTo((int) trimStart);
                  video.start();
                  playing = true;
                  watchPlayback();
                }
              });
      grow(transport, play);
      transport.addView(
          button(
              "↺  Start",
              false,
              () -> {
                if (video != null) {
                  video.pause();
                  video.seekTo((int) trimStart);
                  playing = false;
                }
              }));
      previewPanel.addView(transport);
      spacer(previewPanel, 12);
    }
    if (selected.type.equals("Video") && selected.duration > 0) {
      trimLabel = text("", 12, MUTED);
      updateTrimLabel();
      previewPanel.addView(trimLabel);
      spacer(previewPanel, 7);
      previewPanel.addView(new Timeline(this), new LinearLayout.LayoutParams(-1, dp(45)));
      spacer(previewPanel, 8);
      LinearLayout trim = row();
      grow(trim, text("TRIM RANGE", 10, MINT));
      trim.addView(
          button(
              "Reset",
              false,
              () -> {
                trimStart = 0;
                trimEnd = selected.duration;
                saveTrim();
                build();
              }));
      previewPanel.addView(trim);
      previewPanel.addView(range("In", true));
      previewPanel.addView(range("Out", false));
      previewPanel.addView(text("Fast MP4 export · starts at the previous keyframe", 10, MUTED));
      spacer(previewPanel, 8);
      previewPanel.addView(
          button(exporting ? "Exporting…" : "↗  Export MP4", true, this::chooseExport));
    }
  }

  View range(String label, boolean start) {
    LinearLayout r = row();
    r.addView(text(label, 12, MUTED), new LinearLayout.LayoutParams(dp(30), -2));
    SeekBar seek = new SeekBar(this);
    seek.setMax(1000);
    seek.setProgress((int) ((start ? trimStart : trimEnd) * 1000 / Math.max(1, selected.duration)));
    seek.setContentDescription(start ? "Trim start" : "Trim end");
    seek.setProgressTintList(android.content.res.ColorStateList.valueOf(MINT));
    r.addView(seek, new LinearLayout.LayoutParams(0, dp(34), 1));
    seek.setOnSeekBarChangeListener(
        new SeekBar.OnSeekBarChangeListener() {
          public void onProgressChanged(SeekBar s, int p, boolean user) {
            if (!user) return;
            long value = selected.duration * p / 1000;
            if (start) trimStart = Math.min(value, Math.max(0, trimEnd - 100));
            else trimEnd = Math.max(value, trimStart + 100);
            trimEnd = Math.min(trimEnd, selected.duration);
            updateTrimLabel();
          }

          public void onStartTrackingTouch(SeekBar s) {
            if (video != null) video.pause();
          }

          public void onStopTrackingTouch(SeekBar s) {
            saveTrim();
            s.setProgress((int) ((start ? trimStart : trimEnd) * 1000 / selected.duration));
            if (video != null) video.seekTo((int) trimStart);
          }
        });
    return r;
  }

  void updateTrimLabel() {
    if (trimLabel != null)
      trimLabel.setText(
          time(trimStart)
              + "  →  "
              + time(trimEnd)
              + "     /     "
              + time(Math.max(0, trimEnd - trimStart))
              + " selected");
  }

  void watchPlayback() {
    handler.postDelayed(
        () -> {
          if (video == null || !playing) return;
          if (video.getCurrentPosition() >= trimEnd && trimEnd > 0) {
            video.pause();
            playing = false;
          } else watchPlayback();
        },
        100);
  }

  void stopPlayback() {
    playing = false;
    if (video != null) {
      video.stopPlayback();
      video = null;
    }
    results = null;
    count = null;
  }

  void importMedia() {
    Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
    i.setType("*/*");
    i.putExtra(Intent.EXTRA_MIME_TYPES, new String[] {"video/*", "audio/*", "image/*"});
    i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
    i.addCategory(Intent.CATEGORY_OPENABLE);
    i.addFlags(
        Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
    startActivityForResult(i, 101);
  }

  void chooseExport() {
    if (exporting) {
      toast("Please wait for the current export.");
      return;
    }
    if (selected == null || !selected.type.equals("Video")) return;
    Intent i = new Intent(Intent.ACTION_CREATE_DOCUMENT);
    i.setType("video/mp4");
    i.addCategory(Intent.CATEGORY_OPENABLE);
    i.putExtra(Intent.EXTRA_TITLE, selected.name.replaceFirst("\\.[^.]+$", "") + "_CineLayer.mp4");
    startActivityForResult(i, 102);
  }

  @Override
  protected void onActivityResult(int request, int result, Intent data) {
    super.onActivityResult(request, result, data);
    if (result != RESULT_OK || data == null) return;
    if (request == 101) {
      ArrayList<Uri> uris = new ArrayList<>();
      if (data.getClipData() != null)
        for (int n = 0; n < data.getClipData().getItemCount(); n++)
          uris.add(data.getClipData().getItemAt(n).getUri());
      else if (data.getData() != null) uris.add(data.getData());
      for (Uri uri : uris)
        try {
          getContentResolver()
              .takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (Exception ignored) {
        }
      toast("Reading selected media…");
      worker.execute(
          () -> {
            ArrayList<MediaItem> imported = new ArrayList<>();
            for (Uri uri : uris) {
              MediaItem item = readMedia(uri);
              if (item != null) {
                probeVideoMetadata(item);
                imported.add(item);
              }
            }
            handler.post(
                () -> {
                  for (MediaItem item : imported) {
                    boolean exists = false;
                    for (MediaItem old : library) if (old.uri.equals(item.uri)) exists = true;
                    if (!exists) library.add(item);
                  }
                  saveLibrary();
                  query = "";
                  type = "All";
                  favorites = false;
                  page = "Library";
                  build();
                  toast(imported.size() + " media read");
                });
          });
    }
    if (request == 102 && data.getData() != null) {
      Uri output = data.getData(), input = Uri.parse(selected.uri);
      long start = trimStart, end = trimEnd;
      exporting = true;
      build();
      toast("Exporting video…");
      worker.execute(
          () -> {
            String message;
            try {
              long actual = TrimExporter.export(this, input, output, start, end);
              message = "Export complete. Video starts at " + time(actual) + " (keyframe).";
            } catch (Exception e) {
              try {
                DocumentsContract.deleteDocument(getContentResolver(), output);
              } catch (Exception ignored) {
              }
              message =
                  "Export failed: "
                      + (e.getMessage() == null ? "unsupported format" : e.getMessage());
            }
            String finalMessage = message;
            handler.post(
                () -> {
                  exporting = false;
                  if (!isDestroyed()) {
                    build();
                    new AlertDialog.Builder(this)
                        .setTitle("Video export")
                        .setMessage(finalMessage)
                        .setPositiveButton("OK", null)
                        .show();
                  }
                });
          });
    }
  }

  MediaItem readMedia(Uri uri) {
    String mime = getContentResolver().getType(uri);
    if (mime == null
        || !(mime.startsWith("video/") || mime.startsWith("audio/") || mime.startsWith("image/")))
      return null;
    String name = "Imported media";
    try (Cursor c =
        getContentResolver()
            .query(uri, new String[] {OpenableColumns.DISPLAY_NAME}, null, null, null)) {
      if (c != null && c.moveToFirst()) name = c.getString(0);
    } catch (Exception ignored) {
    }
    long duration = 0;
    if (!mime.startsWith("image/")) {
      MediaMetadataRetriever m = new MediaMetadataRetriever();
      try {
        m.setDataSource(this, uri);
        String d = m.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        if (d != null) duration = Long.parseLong(d);
      } catch (Exception ignored) {
      } finally {
        try {
          m.release();
        } catch (Exception ignored) {
        }
      }
    }
    return new MediaItem(
        uri.toString(),
        name,
        mime.startsWith("video/") ? "Video" : mime.startsWith("audio/") ? "Audio" : "Image",
        duration,
        System.currentTimeMillis(),
        false,
        false);
  }

  void mediaMenu() {
    new AlertDialog.Builder(this)
        .setTitle(selected.name)
        .setItems(
            new String[] {
              "Edit notes & color tag",
              selected.favorite ? "Remove favorite" : "Add favorite",
              "Remove from library"
            },
            (d, i) -> {
              if (i == 0) editMetadata();
              else if (i == 1) {
                selected.favorite = !selected.favorite;
                saveLibrary();
                build();
              } else
                new AlertDialog.Builder(this)
                    .setTitle("Remove from library?")
                    .setMessage("Your original file stays on your device.")
                    .setPositiveButton(
                        "Remove",
                        (dialog, which) -> {
                          library.remove(selected);
                          getPreferences(0)
                              .edit()
                              .remove("in:" + selected.uri)
                              .remove("out:" + selected.uri)
                              .apply();
                          if (!selected.sample)
                            try {
                              getContentResolver()
                                  .releasePersistableUriPermission(
                                      Uri.parse(selected.uri),
                                      Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            } catch (Exception ignored) {
                            }
                          selected = null;
                          saveLibrary();
                          page = "Library";
                          build();
                        })
                    .setNegativeButton("Cancel", null)
                    .show();
            })
        .show();
  }

  void editMetadata() {
    LinearLayout form = col();
    pad(form, 20);
    EditText notes = new EditText(this);
    notes.setHint("Notes to help you find this clip");
    notes.setText(selected.notes);
    form.addView(notes);
    Spinner tag = new Spinner(this);
    String[] colors = {"None", "Red", "Orange", "Yellow", "Green", "Blue", "Purple"};
    tag.setAdapter(
        new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, colors));
    for (int i = 0; i < colors.length; i++)
      if (colors[i].equals(selected.colorTag)) tag.setSelection(i);
    form.addView(text("Color tag (search by its full name)", 12, MUTED));
    form.addView(tag);
    new AlertDialog.Builder(this)
        .setTitle("Notes & color tag")
        .setView(form)
        .setPositiveButton(
            "Save",
            (d, i) -> {
              selected.notes = notes.getText().toString();
              selected.colorTag =
                  tag.getSelectedItemPosition() == 0 ? "" : colors[tag.getSelectedItemPosition()];
              saveLibrary();
              build();
            })
        .setNegativeButton("Cancel", null)
        .show();
  }

  static byte[] readBytes(InputStream input) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    copy(input, out);
    return out.toByteArray();
  }

  static void copy(InputStream input, OutputStream output) throws IOException {
    byte[] buffer = new byte[16384];
    int n;
    while ((n = input.read(buffer)) != -1) output.write(buffer, 0, n);
  }

  void applyMetadata(MediaItem item, JSONObject data) {
    item.notes = data.optString("notes");
    item.colorTag = data.optString("colorTag");
    item.frameRate = data.optString("frameRate");
    item.aspectRatio = data.optString("aspectRatio");
  }

  void writeMetadata(MediaItem item, JSONObject data) throws JSONException {
    data.put("notes", item.notes);
    data.put("colorTag", item.colorTag);
    data.put("frameRate", item.frameRate);
    data.put("aspectRatio", item.aspectRatio);
  }

  void probeVideoMetadata(MediaItem item) {
    if (!item.type.equals("Video")) return;
    MediaMetadataRetriever m = new MediaMetadataRetriever();
    try {
      m.setDataSource(this, Uri.parse(item.uri));
      String rate = m.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE);
      item.frameRate = rate == null ? "" : rate;
      String width = m.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH),
          height = m.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
      if (width != null && height != null) {
        int w = Integer.parseInt(width), h = Integer.parseInt(height), a = w, b = h;
        while (b != 0) {
          int t = a % b;
          a = b;
          b = t;
        }
        if (a > 0) item.aspectRatio = (w / a) + ":" + (h / a);
      }
    } catch (Exception ignored) {
    } finally {
      try {
        m.release();
      } catch (Exception ignored) {
      }
    }
  }

  void settings() {
    ScrollView scroll = new ScrollView(this);
    LinearLayout body = col();
    scroll.addView(body);
    content.addView(scroll);
    body.addView(text("Made for your next idea.", 26, WHITE));
    spacer(body, 18);
    body.addView(text("CineLayer 1.0.0\nPT Deploy Dulu Pulang Nanti", 15, MUTED));
    spacer(body, 22);
    body.addView(
        button(
            "Privacy policy",
            false,
            () -> {
              try (InputStream in = getAssets().open("privacy.html")) {
                String html = new String(readBytes(in), java.nio.charset.StandardCharsets.UTF_8);
                android.webkit.WebView web = new android.webkit.WebView(this);
                web.getSettings().setJavaScriptEnabled(false);
                web.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
                new AlertDialog.Builder(this)
                    .setTitle("Privacy policy")
                    .setView(web)
                    .setPositiveButton("Close", null)
                    .show();
              } catch (Exception e) {
                toast("Policy unavailable");
              }
            }));
    spacer(body, 10);
    body.addView(
        button(
            "Email support",
            false,
            () -> {
              try {
                startActivity(
                    new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:seandyadryan2@gmail.com")));
              } catch (Exception e) {
                toast("Contact: seandyadryan2@gmail.com");
              }
            }));
    spacer(body, 22);
    body.addView(
        text(
            "Your library stays on this device.\n"
                + "No accounts. No ads. No analytics.\n\n"
                + "Import with the system file picker. Search filenames,\n"
                + "filter media types, and keep favorites close.\n\n"
                + "Video trimming preserves original audio and video.\n"
                + "Exports snap back to the previous video keyframe.\n"
                + "Not all source codecs can be saved in MP4.",
            14,
            MUTED));
    spacer(body, 20);
    body.addView(
        button(
            "Restore included samples",
            false,
            () -> {
              worker.execute(
                  () -> {
                    ArrayList<MediaItem> samples = sampleItems();
                    handler.post(
                        () -> {
                          for (MediaItem s : samples) {
                            boolean exists = false;
                            for (MediaItem old : library) if (old.uri.equals(s.uri)) exists = true;
                            if (!exists) library.add(s);
                          }
                          saveLibrary();
                          toast("Included samples restored");
                        });
                  });
            }));
  }

  void loadThumb(MediaItem item, ImageView view) {
    Bitmap cached = thumbs.get(item.uri);
    if (cached != null) {
      view.setImageBitmap(cached);
      return;
    }
    if (item.type.equals("Audio")) {
      view.setImageDrawable(new AudioArt());
      return;
    }
    view.setTag(item.uri);
    worker.execute(
        () -> {
          Bitmap bitmap = null;
          try {
            if (item.type.equals("Image")) {
              try (InputStream in = getContentResolver().openInputStream(Uri.parse(item.uri))) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2;
                bitmap = BitmapFactory.decodeStream(in, null, options);
              }
            } else {
              MediaMetadataRetriever m = new MediaMetadataRetriever();
              try {
                m.setDataSource(this, Uri.parse(item.uri));
                bitmap =
                    m.getScaledFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC, 640, 360);
              } finally {
                m.release();
              }
            }
          } catch (Exception ignored) {
          }
          if (bitmap != null) {
            thumbs.put(item.uri, bitmap);
            Bitmap result = bitmap;
            handler.post(
                () -> {
                  if (item.uri.equals(view.getTag())) view.setImageBitmap(result);
                });
          }
        });
  }

  void loadLibrary() {
    String data = getPreferences(0).getString("library", null);
    if (data == null) {
      library.addAll(sampleItems());
      saveLibrary();
    } else
      try {
        JSONArray array = new JSONArray(data);
        for (int i = 0; i < array.length(); i++) {
          JSONObject o = array.getJSONObject(i);
          MediaItem item =
              new MediaItem(
                  o.getString("uri"),
                  o.getString("name"),
                  o.getString("type"),
                  o.getLong("duration"),
                  o.getLong("added"),
                  o.optBoolean("favorite"),
                  o.optBoolean("sample"));
          applyMetadata(item, o);
          library.add(item);
        }
      } catch (Exception ignored) {
      }
  }

  ArrayList<MediaItem> sampleItems() {
    ArrayList<MediaItem> items = new ArrayList<>();
    String[] files = {"Coast_01.mp4", "Coast_Still.jpg", "Motion_Study.mp4", "Studio_Ambience.wav"};
    for (int i = 0; i < files.length; i++)
      try {
        File f = new File(getFilesDir(), files[i]);
        if (!f.exists())
          try (InputStream in = getAssets().open("samples/" + files[i]);
              OutputStream out = new FileOutputStream(f)) {
            copy(in, out);
          }
        MediaItem item =
            new MediaItem(
                Uri.fromFile(f).toString(),
                files[i],
                i == 1 ? "Image" : i == 3 ? "Audio" : "Video",
                i == 1 ? 0 : 8000,
                1700000000000L - i * 1000,
                i == 0,
                true);
        item.frameRate = item.type.equals("Video") ? "24" : "";
        item.aspectRatio = item.type.equals("Audio") ? "" : "16:9";
        item.notes =
            i < 2
                ? "Mediterranean coast, warm evening light"
                : i == 2 ? "Abstract motion and color" : "Soft studio tone";
        item.colorTag = i < 2 ? "Green" : i == 2 ? "Purple" : "Blue";
        items.add(item);
      } catch (Exception ignored) {
      }
    return items;
  }

  void saveLibrary() {
    JSONArray array = new JSONArray();
    for (MediaItem item : library)
      try {
        JSONObject o = new JSONObject();
        o.put("uri", item.uri);
        o.put("name", item.name);
        o.put("type", item.type);
        o.put("duration", item.duration);
        o.put("added", item.added);
        o.put("favorite", item.favorite);
        o.put("sample", item.sample);
        writeMetadata(item, o);
        array.put(o);
      } catch (Exception ignored) {
      }
    getPreferences(0).edit().putString("library", array.toString()).apply();
  }

  void saveTrim() {
    if (selected != null)
      getPreferences(0)
          .edit()
          .putLong("in:" + selected.uri, trimStart)
          .putLong("out:" + selected.uri, trimEnd)
          .apply();
  }

  void loadTrim() {
    trimStart = getPreferences(0).getLong("in:" + selected.uri, 0);
    trimEnd = getPreferences(0).getLong("out:" + selected.uri, selected.duration);
  }

  String time(long millis) {
    long s = millis / 1000;
    return String.format(Locale.ROOT, "%02d:%02d", s / 60, s % 60);
  }

  void toast(String message) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
  }

  @Override
  public void onConfigurationChanged(Configuration c) {
    super.onConfigurationChanged(c);
    build();
  }

  @Override
  protected void onPause() {
    super.onPause();
    if (video != null) video.pause();
    playing = false;
  }

  @Override
  protected void onDestroy() {
    stopPlayback();
    handler.removeCallbacksAndMessages(null);
    worker.shutdown();
    super.onDestroy();
  }

  class Timeline extends View {
    Paint p = new Paint(3);
    Rect tile = new Rect();

    Timeline(Context c) {
      super(c);
    }

    protected void onDraw(Canvas c) {
      int w = getWidth(), h = getHeight();
      p.setColor(LINE);
      c.drawRoundRect(0, 0, w, h, dp(6), dp(6), p);
      Bitmap frame = selected == null ? null : thumbs.get(selected.uri);
      if (frame != null)
        for (int x = 0; x < w; x += dp(72)) {
          tile.set(x, 0, x + dp(70), h);
          c.drawBitmap(frame, null, tile, p);
        }
      p.setColor(MINT);
      c.drawRect(0, 0, dp(3), h, p);
      c.drawRect(w - dp(3), 0, w, h, p);
    }
  }

  class AudioArt extends android.graphics.drawable.Drawable {
    Paint p = new Paint(3);

    public void draw(Canvas c) {
      android.graphics.Rect b = getBounds();
      p.setShader(
          new LinearGradient(
              0,
              0,
              b.width(),
              b.height(),
              new int[] {0xFF294456, 0xFF272342},
              null,
              Shader.TileMode.CLAMP));
      c.drawRect(b, p);
      p.setShader(null);
      p.setColor(MINT);
      p.setStrokeWidth(dp(2));
      for (int i = 0; i < 32; i++) {
        float x = b.width() * (i + 2) / 36f,
            v = (float) (Math.sin(i * 1.7) * Math.sin(i * .4) * .3 + .1) * b.height();
        c.drawLine(x, b.height() / 2f - v, x, b.height() / 2f + v, p);
      }
    }

    public void setAlpha(int a) {}

    public void setColorFilter(ColorFilter f) {}

    public int getOpacity() {
      return android.graphics.PixelFormat.OPAQUE;
    }
  }
}
