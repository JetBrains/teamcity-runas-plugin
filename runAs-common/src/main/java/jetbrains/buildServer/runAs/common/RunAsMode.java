package jetbrains.buildServer.runAs.common;

public enum RunAsMode {
  CustomCredentials("custom", "Custom Credentials"),
  PredefinedCredentials("predefined", "Predefined Credentials"),
  Enabled("enabled", "Enabled"),
  Disabled("disabled", "Disabled");

  private final String myValue;
  private final String myDescription;

  private RunAsMode(String value, final String description) {
    myValue = value;
    myDescription = description;
  }

  public String getValue() {
    return myValue;
  }

  public String getDescription() {
    return myDescription;
  }

  public static RunAsMode tryParse(String value) {
    for (RunAsMode v : values()) {
      if (v.getValue().equals(value)) return v;
    }

    return Enabled;
  }
}