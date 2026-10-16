package com.ptdeploy.cinelayer;

import android.app.*;
import android.content.*;
import android.media.*;
import android.net.Uri;
import android.os.*;
import java.io.*;
import java.security.MessageDigest;
import java.util.*;

/** Dependency-free device smoke test; validates real Activity and MP4 output. */
public final class SmokeInstrumentation extends Instrumentation {
  String captureGroup;
  void check(boolean condition, String message) {
    if (!condition) throw new AssertionError(message);
  }

  @Override
  public void onCreate(Bundle args) {
    super.onCreate(args);
    captureGroup = args == null ? null : args.getString("capture");
    start();
  }

  byte[] digest(File f) throws Exception {
    MessageDigest d = MessageDigest.getInstance("SHA-256");
    try (InputStream in = new FileInputStream(f)) {
      byte[] b = new byte[8192];
      int n;
      while ((n = in.read(b)) > 0) d.update(b, 0, n);
    }
    return d.digest();
  }

  @Override
  public void onStart() {
    Bundle result = new Bundle();
    try {
      Context context = getTargetContext();
      Intent intent =
          new Intent(context, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      MainActivity activity = (MainActivity) startActivitySync(intent);
      waitForIdleSync();
      if (captureGroup != null) {
        captureScreens(activity);
        result.putString("stream", "PASS: captured four actual app screens for " + captureGroup + "\n");
        finish(Activity.RESULT_OK, result);
        return;
      }
      check(activity.library.size() >= 4, "bundled library did not load");
      runOnMainSync(() -> activity.search.setText("COAST"));
      Thread.sleep(350);
      waitForIdleSync();
      check(
          activity.count.getText().toString().startsWith("2 media"),
          "reactive search results mismatch");
      runOnMainSync(
          () -> {
            activity.search.setText("missing");
            activity.search.setText("studio");
          });
      Thread.sleep(350);
      waitForIdleSync();
      check(
          activity.count.getText().toString().startsWith("1 media"),
          "stale search result replaced latest query");
      runOnMainSync(
          () -> {
            activity.search.setText("16:9");
          });
      Thread.sleep(350);
      waitForIdleSync();
      check(
          activity.count.getText().toString().startsWith("3 media"),
          "metadata search not connected to UI");
      MediaItem clip =
          activity.library.stream().filter(x -> x.name.equals("Coast_01.mp4")).findFirst().get();
      runOnMainSync(
          () -> {
            activity.select(clip);
            activity.page = "Editor";
            activity.build();
          });
      waitForIdleSync();
      check(activity.selected == clip, "selection lost");
      File source = new File(Uri.parse(clip.uri).getPath()),
          output = new File(context.getFilesDir(), "smoke-export.mp4");
      byte[] original = digest(source);
      long actual =
          TrimExporter.export(context, Uri.fromFile(source), Uri.fromFile(output), 2100, 6500);
      check(actual >= 1900 && actual <= 2100, "export did not align to preceding keyframe");
      check(Arrays.equals(original, digest(source)), "original file was modified");
      MediaExtractor extractor = new MediaExtractor();
      try {
        extractor.setDataSource(output.toString());
        check(extractor.getTrackCount() == 2, "audio/video tracks not retained");
        int video = -1;
        for (int i = 0; i < extractor.getTrackCount(); i++)
          if (extractor.getTrackFormat(i).getString(MediaFormat.KEY_MIME).startsWith("video/"))
            video = i;
        check(video >= 0, "no video output");
        extractor.selectTrack(video);
        check(extractor.getSampleTime() == 0, "export timestamps not normalized");
      } finally {
        extractor.release();
      }
      MediaMetadataRetriever metadata = new MediaMetadataRetriever();
      try {
        metadata.setDataSource(output.toString());
        long duration =
            Long.parseLong(metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        check(duration >= 4300 && duration <= 4600, "unexpected trim duration " + duration);
        check(metadata.getFrameAtTime(0) != null, "export cannot decode");
      } finally {
        metadata.release();
      }
      check(output.delete(), "temporary export cleanup failed");
      runOnMainSync(
          () -> {
            activity.query = "";
            activity.page = "Library";
            activity.build();
          });
      result.putString(
          "stream",
          "PASS: Activity samples, reactive search, stale-query cancellation, metadata search,"
              + " selection, keyframe-aligned MP4 export, track retention, timestamp normalization,"
              + " decode, source integrity.\n");
      finish(Activity.RESULT_OK, result);
    } catch (Throwable e) {
      result.putString("stream", "FAIL: " + e + "\n" + android.util.Log.getStackTraceString(e));
      finish(Activity.RESULT_CANCELED, result);
    }
  }

  void screenshot(MainActivity activity, String name) throws Exception {
    waitForIdleSync();
    Thread.sleep(2200);
    android.graphics.Bitmap screenshot = getUiAutomation().takeScreenshot();
    check(screenshot != null, "cannot capture display");
    File directory = new File(activity.getExternalFilesDir(null), "screenshots/" + captureGroup);
    check(directory.exists() || directory.mkdirs(), "cannot create screenshot folder");
    try (OutputStream output = new FileOutputStream(new File(directory, name + ".png"))) {
      screenshot.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, output);
    }
    screenshot.recycle();
  }

  void captureScreens(MainActivity activity) throws Exception {
    MediaItem coast = activity.library.stream().filter(x -> x.name.equals("Coast_01.mp4")).findFirst().get();
    MediaItem motion = activity.library.stream().filter(x -> x.name.equals("Motion_Study.mp4")).findFirst().get();
    runOnMainSync(() -> {activity.query="";activity.type="All";activity.scope="All sources";activity.favorites=false;activity.page="Library";activity.select(coast);activity.build();});
    screenshot(activity,"01-library");
    runOnMainSync(() -> activity.search.setText("coast"));
    screenshot(activity,"02-search");
    runOnMainSync(() -> {activity.query="";activity.type="Video";activity.select(motion);activity.trimStart=1000;activity.trimEnd=6500;if(!activity.wide)activity.page="Editor";activity.build();});
    screenshot(activity,"03-trim");
    runOnMainSync(() -> {activity.query="";activity.type="All";activity.favorites=true;activity.page="Library";activity.select(coast);activity.build();});
    screenshot(activity,"04-favorites");
  }
}
