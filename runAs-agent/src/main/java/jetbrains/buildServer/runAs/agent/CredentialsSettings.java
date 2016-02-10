package jetbrains.buildServer.runAs.agent;

import org.jetbrains.annotations.NotNull;

public class CredentialsSettings {
  private final String myUser;
  private final String myPassword;

  public CredentialsSettings(
    @NotNull final String user,
    @NotNull final String password) {
    myUser = user;
    myPassword = password;
  }

  @NotNull
  public String getUser() {
    return myUser;
  }

  @NotNull
  public String getPassword() {
    return myPassword;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final CredentialsSettings that = (CredentialsSettings)o;

    if (!getUser().equals(that.getUser())) return false;
    return getPassword().equals(that.getPassword());

  }

  @Override
  public int hashCode() {
    int result = getUser().hashCode();
    result = 31 * result + getPassword().hashCode();
    return result;
  }
}
