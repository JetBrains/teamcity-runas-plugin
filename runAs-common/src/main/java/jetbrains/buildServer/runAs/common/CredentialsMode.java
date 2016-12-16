package jetbrains.buildServer.runAs.common;

public enum CredentialsMode {
  Prohibited("prohibited", "Prohibited"),
  Enforced("enforced", "Enforced"),
  Allowed("allowed", "Allowed"),
  Disabled("disabled", "Disabled");

  private final String myValue;
  private final String myDescription;

  private CredentialsMode(String value, final String description) {
    myValue = value;
    myDescription = description;
  }

  public String getValue() {
    return myValue;
  }

  public String getDescription() {
    return myDescription;
  }

  public static CredentialsMode tryParse(String value) {
    for (CredentialsMode v : values()) {
      if (v.getValue().equals(value)) return v;
    }

    return Allowed;
  }
}