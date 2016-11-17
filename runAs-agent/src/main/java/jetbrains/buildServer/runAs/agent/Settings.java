package jetbrains.buildServer.runAs.agent;

import java.util.List;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineArgument;
import org.jetbrains.annotations.NotNull;

public class Settings {
  private final String myUser;
  private final String myPassword;
  private final List<CommandLineArgument> myAdditionalArgs;

  Settings(
    @NotNull final String user,
    @NotNull final String password,
    @NotNull final List<CommandLineArgument> additionalArgs) {
    myUser = user;
    myPassword = password;
    myAdditionalArgs = additionalArgs;
  }

  @NotNull
  String getUser() {
    return myUser;
  }

  @NotNull
  String getPassword() {
    return myPassword;
  }

  @NotNull
  List<CommandLineArgument> getAdditionalArgs() {
    return myAdditionalArgs;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final Settings settings = (Settings)o;

    if (!myUser.equals(settings.myUser)) return false;
    if (!myPassword.equals(settings.myPassword)) return false;
    return myAdditionalArgs.equals(settings.myAdditionalArgs);

  }

  @Override
  public int hashCode() {
    int result = myUser.hashCode();
    result = 31 * result + myPassword.hashCode();
    result = 31 * result + myAdditionalArgs.hashCode();
    return result;
  }
}
