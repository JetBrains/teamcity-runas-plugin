package jetbrains.buildServer.runAs.agent;

import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineSetup;
import org.jetbrains.annotations.NotNull;

public class Settings {
  private final CommandLineSetup mySetup;
  private final String myUser;
  private final String myPassword;
  private final String myWorkingDirectory;

  public Settings(
    @NotNull final CommandLineSetup setup,
    @NotNull final String user,
    @NotNull final String password,
    @NotNull final String workingDirectory) {
    mySetup = setup;
    myUser = user;
    myPassword = password;
    myWorkingDirectory = workingDirectory;
  }

  public CommandLineSetup getSetup() {
    return mySetup;
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

    if (!mySetup.equals(settings.mySetup)) return false;
    if (!myUser.equals(settings.myUser)) return false;
    if (!myPassword.equals(settings.myPassword)) return false;
    return myWorkingDirectory.equals(settings.myWorkingDirectory);

  }

  @Override
  public int hashCode() {
    int result = mySetup.hashCode();
    result = 31 * result + myUser.hashCode();
    result = 31 * result + myPassword.hashCode();
    result = 31 * result + myWorkingDirectory.hashCode();
    return result;
  }
}
