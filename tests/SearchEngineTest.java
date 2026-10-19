import com.deploydulupulangnanti.cinelayer.*;
import java.util.*;

public final class SearchEngineTest {
  static void check(boolean ok, String message) {
    if (!ok) throw new AssertionError(message);
  }

  public static void main(String[] args) {
    List<MediaItem> items =
        List.of(
            new MediaItem("1", "Coast_01.MP4", "Video", 4000, 1, true, false),
            new MediaItem("2", "COAST-photo.jpg", "Image", 0, 2, false, false),
            new MediaItem("3", "City.mp4", "Video", 2000, 3, false, false),
            new MediaItem("4", "100%_literal.wav", "Audio", 5000, 4, false, false));
    check(
        SearchEngine.filter(items, "  CoAsT ", "All", false, "Name").size() == 2,
        "case and whitespace");
    check(
        SearchEngine.filter(items, "coast", "Video", true, "Recent").size() == 1,
        "combined filters");
    check(SearchEngine.filter(items, "missing", "All", false, "Recent").isEmpty(), "empty results");
    check(
        SearchEngine.filter(items, "%_", "All", false, "Name").size() == 1,
        "literal wildcard characters");
    check(
        SearchEngine.filter(items, null, "All", false, "Recent").get(0).uri.equals("4"),
        "recent sorting and null query");
    check(
        SearchEngine.filter(items, "", "Video", false, "Duration").get(0).uri.equals("3"),
        "numeric duration sort");
    check(items.get(0).uri.equals("1"), "source remains unchanged");
    items.get(2).notes = "Night skyline";
    items.get(2).colorTag = "Purple";
    items.get(2).frameRate = "24";
    items.get(2).aspectRatio = "16:9";
    check(
        SearchEngine.filter(items, "SKYLINE", "All", false, "Recent").size() == 1,
        "notes matching");
    check(
        SearchEngine.filter(items, "purple", "All", false, "Recent").size() == 1,
        "exact case insensitive tag");
    check(
        SearchEngine.filter(items, "purp", "All", false, "Recent").isEmpty(),
        "partial color is not a tag match");
    check(
        SearchEngine.filter(items, "16:9", "Video", false, "Recent").size() == 1,
        "aspect metadata");
    check(
        SearchEngine.filter(items, "24", "Video", false, "Recent").size() == 1,
        "frame rate metadata");
    List<MediaItem> sample =
        List.of(new MediaItem("s", "Sample.mp4", "Video", 1000, 1, false, true));
    check(
        SearchEngine.filter(sample, "", "All", false, "Recent", "Imported").isEmpty(),
        "imported scope");
    check(
        SearchEngine.filter(sample, "", "All", false, "Recent", "Samples").size() == 1,
        "sample scope");
    Locale previous = Locale.getDefault();
    Locale.setDefault(Locale.forLanguageTag("tr-TR"));
    check(
        SearchEngine.filter(items, "CITY", "All", false, "Name").size() == 1,
        "stable casing under Turkish locale");
    Locale.setDefault(previous);
    System.out.println("PASS: 15 search contract checks");
  }
}
