package com.ptdeploy.cinelayer;

import java.util.*;

/** Local filename search. No network calls, tokenization, regex, or SQL interpolation. */
public final class SearchEngine {
  private SearchEngine() {}

  public static List<MediaItem> filter(
      List<MediaItem> source, String query, String type, boolean favorites, String sort) {
    return filter(source, query, type, favorites, sort, "All sources");
  }

  public static List<MediaItem> filter(
      List<MediaItem> source,
      String query,
      String type,
      boolean favorites,
      String sort,
      String scope) {
    String needle = (query == null ? "" : query.trim()).toLowerCase(Locale.ROOT);
    List<MediaItem> result = new ArrayList<>();
    for (MediaItem item : source) {
      if ("Imported".equals(scope) && item.sample) continue;
      if ("Samples".equals(scope) && !item.sample) continue;
      if (!"All".equals(type) && !type.equals(item.type)) continue;
      if (favorites && !item.favorite) continue;
      if (!(contains(item.name, needle)
          || contains(item.notes, needle)
          || contains(item.frameRate, needle)
          || contains(item.aspectRatio, needle)
          || item.colorTag.equalsIgnoreCase(needle))) continue;
      result.add(item);
    }
    Comparator<MediaItem> comparator =
        "Name".equals(sort)
            ? Comparator.comparing(x -> x.name.toLowerCase(Locale.ROOT))
            : "Duration".equals(sort)
                ? Comparator.comparingLong(x -> x.duration)
                : Comparator.comparingLong((MediaItem x) -> x.added).reversed();
    result.sort(comparator.thenComparing(x -> x.uri));
    return result;
  }

  static boolean contains(String value, String needle) {
    return value.toLowerCase(Locale.ROOT).contains(needle);
  }
}
