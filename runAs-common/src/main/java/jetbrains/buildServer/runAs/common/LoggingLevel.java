package jetbrains.buildServer.runAs.common;

public enum LoggingLevel {
  Off("off", "Off"),
  Errors("errors", "Errors"),
  Normal("normal", "Normal"),
  Debug("debug", "Debug");

  private final String myValue;
  private final String myDescription;

  private LoggingLevel(String value, final String description) {
    myValue = value;
    myDescription = description;
  }

  public String getValue() {
    return myValue;
  }

  public String getDescription() {
    return myDescription;
  }

  public static LoggingLevel tryParse(String value) {
    for (LoggingLevel v : values()) {
      if (v.getValue().equals(value)) return v;
    }

    return Off;
  }
}