package pl.poznan.put.notation;

import java.util.Arrays;

/**
 * Base-ribose notation. Zirbel, C. L., et al (2009). Classification and energetics of the
 * base-phosphate interactions in RNA. Nucleic Acids Research, 37(15), 4898–4918.
 * http://doi.org/10.1093/nar/gkp468
 */
public enum BR {
  _0("0BR", "n0BR", "0RB", "n0RB"),
  _1("1BR", "n1BR", "1RB", "n1RB"),
  _2("2BR", "n2BR", "2RB", "n2RB"),
  _3("3BR", "n3BR", "3RB", "n3RB"),
  _4("4BR", "n4BR", "4RB", "n4RB"),
  _5("5BR", "n5BR", "5RB", "n5RB"),
  _6("6BR", "n6BR", "6RB", "n6RB"),
  _7("7BR", "n7BR", "7RB", "n7RB"),
  _8("8BR", "n8BR", "8RB", "n8RB"),
  _9("9BR", "n9BR", "9RB", "n9RB"),
  UNKNOWN("UNKNOWN");

  private final String[] displayNames;

  BR(final String... displayNames) {
    this.displayNames = displayNames;
  }

  public static BR fromString(final String candidate) {
      return Arrays.stream(BR.values()).filter(br -> Arrays.asList(br.displayNames).contains(candidate)).findFirst().orElse(BR.UNKNOWN);
  }

  public String getDisplayName() {
    return displayNames[0];
  }
}
