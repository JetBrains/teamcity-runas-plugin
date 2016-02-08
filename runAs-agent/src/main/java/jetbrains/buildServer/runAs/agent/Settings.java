package jetbrains.buildServer.runAs.agent;

import org.jetbrains.annotations.NotNull;

public class Settings {
  private final String myUser;
  private final String myPassword;
  private final String myWorkingDirectory;

  public Settings(
    @NotNull final String user,
    @NotNull final String password,
    @NotNull final String workingDirectory) {
    myUser = user;
    myPassword = password;
    myWorkingDirectory = workingDirectory;
  }

  public String getUser() {
    return myUser;
  }

  public String getPassword() {
    return myPassword;
  }

  public String getWorkingDirectory() {
    return myWorkingDirectory;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final Settings settings = (Settings)o;

    if (!myUser.equals(settings.myUser)) return false;
    if (!myPassword.equals(settings.myPassword)) return false;
    return myWorkingDirectory.equals(settings.myWorkingDirectory);

  }

  @Override
  public int hashCode() {
    int result = myUser.hashCode();
    result = 31 * result + myPassword.hashCode();
    result = 31 * result + myWorkingDirectory.hashCode();
    return result;
  }
}
