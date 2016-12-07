package jetbrains.buildServer.runAs.common;

public enum WindowsIntegrityLevel {
  Auto("auto", "Default"),
  Untrusted("untrusted", "Untrusted"),
  Low ("low", "Low"),
  Medium ("medium", "Medium"),
  MediumPlus ("medium_plus", "Medium Plus"),
  High("high", "High");

  private final String myValue;
  private final String myDescription;

  private WindowsIntegrityLevel(String value, final String description) {
    myValue = value;
    myDescription = description;
  }

  public String getValue() {
    return myValue;
  }

  public String getDescription() {
    return myDescription;
  }

  public static WindowsIntegrityLevel tryParse(String value) {
    for (WindowsIntegrityLevel v : values()) {
      if (v.getValue().equals(value)) return v;
    }

    return Auto;
  }
}