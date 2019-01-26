package org.camunda.bpm.spring.boot.example.webapp.delegate;

import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class Strings {

  private static final Pattern DEFAULT_INTERPOLATE_PATTERN = Pattern.compile("\\$\\{(.+?)\\}");
  private static final Pattern CSV = Pattern.compile(",");

  private Strings() {}

  public static boolean isNullOrEmpty(String s) {
    return s == null || s.isEmpty();
  }

  public static boolean isNotEmpty(String s) {
    return s != null && !s.isEmpty();
  }

  public static String[] splitAndTrim(String s) {
    return Stream.of(s.split(",")).map(String::trim).toArray(String[]::new);
  }

  /**
   * Inject data property values into a string.
   *
   * <p>Examples:
   *
   * <pre>{@code
   * import static org.assertj.core.api.Assertions.assertThat;
   * import java.util.*;
   *
   * String s = "${foo} Hello ${world}";
   * Map<String, Object> data = new HashMap<>();
   * data.put("world", "Welt");
   * data.put("foo", 42);
   * assertThat(Strings.interpolate(s, data)).isEqualTo("42 Hello Welt");
   * }</pre>
   */
  public static String interpolate(String s, Map<String, ? extends Object> data) {
    return interpolate(s, DEFAULT_INTERPOLATE_PATTERN, data);
  }

  /**
   * Inject data property values into a string.
   *
   * <p>Examples:
   *
   * <pre>{@code
   * import static org.assertj.core.api.Assertions.assertThat;
   * import java.util.regex.*;
   * import java.util.*;
   *
   * String s = "Hello #{world}";
   * Pattern pattern = Pattern.compile("#\\{(.+?)\\}");
   * Map<String, Object> data = new HashMap<>();
   * data.put("world", "Welt");
   * assertThat(Strings.interpolate(s, pattern, data)).isEqualTo("Hello Welt");
   * }</pre>
   */
  public static String interpolate(String s, Pattern pattern, Map<String, ? extends Object> data) {
    Matcher matcher = pattern.matcher(s);

    Map<String, Object> caseInsensitiveData =
        new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
    caseInsensitiveData.putAll(data);

    StringBuffer buffer = new StringBuffer();
    while (matcher.find()) {
      String replacement = "" + caseInsensitiveData.getOrDefault(matcher.group(1), "");
      matcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement));
    }
    matcher.appendTail(buffer);
    return buffer.toString();
  }

  /**
   * Streams values of a CSV string.
   *
   * <p>Examples:
   *
   * <pre>{@code
   * import static org.assertj.core.api.Assertions.assertThat;
   *
   * assertThat(Strings.csv("a,,b, c ,,,,d")).contains("a", "b", "c", "d");
   * }</pre>
   */
  public static Stream<String> csv(String str) {
    return str == null
        ? Stream.empty()
        : CSV.splitAsStream(str).map(String::trim).filter(s -> !s.isEmpty());
  }
}
