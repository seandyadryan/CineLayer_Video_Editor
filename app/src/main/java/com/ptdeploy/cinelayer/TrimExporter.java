package com.ptdeploy.cinelayer;

import android.content.Context;
import android.media.*;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import java.nio.ByteBuffer;
import java.util.*;

/** Copies original compressed samples; starts at the preceding video sync frame. */
public final class TrimExporter {
  private TrimExporter() {}

  public static long export(Context context, Uri input, Uri output, long startMs, long endMs)
      throws Exception {
    MediaExtractor extractor = new MediaExtractor();
    MediaMuxer muxer = null;
    boolean started = false;
    try (ParcelFileDescriptor fd = context.getContentResolver().openFileDescriptor(output, "rw")) {
      if (fd == null) throw new IllegalStateException("Cannot open destination");
      extractor.setDataSource(context, input, null);
      Map<Integer, Integer> tracks = new HashMap<>();
      int videoTrack = -1, capacity = 4 * 1024 * 1024;
      muxer = new MediaMuxer(fd.getFileDescriptor(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
      for (int i = 0; i < extractor.getTrackCount(); i++) {
        MediaFormat format = extractor.getTrackFormat(i);
        String mime = format.getString(MediaFormat.KEY_MIME);
        if (mime != null && (mime.startsWith("video/") || mime.startsWith("audio/"))) {
          if (mime.startsWith("video/")) videoTrack = i;
          tracks.put(i, muxer.addTrack(format));
          if (format.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE))
            capacity = Math.max(capacity, format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE));
        }
      }
      if (videoTrack < 0)
        throw new IllegalArgumentException("Choose a video with a supported MP4 codec");
      extractor.selectTrack(videoTrack);
      extractor.seekTo(startMs * 1000, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
      long base = extractor.getSampleTime();
      if (base < 0 || base >= endMs * 1000)
        throw new IllegalArgumentException("No video samples in this range");
      // Reset the complete interleaved stream at the same sync boundary.
      for (int track : tracks.keySet()) extractor.selectTrack(track);
      extractor.seekTo(base, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
      MediaMetadataRetriever metadata = new MediaMetadataRetriever();
      try {
        metadata.setDataSource(context, input);
        String rotation =
            metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
        if (rotation != null) muxer.setOrientationHint(Integer.parseInt(rotation));
      } finally {
        metadata.release();
      }
      muxer.start();
      started = true;
      ByteBuffer data = ByteBuffer.allocateDirect(capacity);
      MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
      int count = 0;
      while (true) {
        long time = extractor.getSampleTime();
        int track = extractor.getSampleTrackIndex();
        if (time < 0 || time >= endMs * 1000) break;
        if (time >= base && tracks.containsKey(track)) {
          if (extractor.getSampleSize() > capacity)
            throw new IllegalStateException("Media sample exceeds export buffer");
          data.clear();
          int size = extractor.readSampleData(data, 0);
          if (size < 0) break;
          info.set(
              0,
              size,
              time - base,
              (extractor.getSampleFlags() & MediaExtractor.SAMPLE_FLAG_SYNC) != 0
                  ? MediaCodec.BUFFER_FLAG_KEY_FRAME
                  : 0);
          muxer.writeSampleData(tracks.get(track), data, info);
          count++;
        }
        extractor.advance();
      }
      if (count == 0) throw new IllegalStateException("No samples exported");
      muxer.stop();
      started = false;
      return base / 1000;
    } finally {
      extractor.release();
      if (muxer != null) {
        if (started)
          try {
            muxer.stop();
          } catch (Exception ignored) {
          }
        muxer.release();
      }
    }
  }
}
