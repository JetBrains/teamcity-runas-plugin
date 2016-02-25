package jetbrains.buildServer.runAs.agent;

import java.util.List;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineArgument;
import org.jetbrains.annotations.NotNull;

public class Settings {
  private final String myUser;
  private final String myPassword;
  private final List<CommandLineArgument> myAdditionalArgs;

  public Settings(
    @NotNull final String user,
    @NotNull final String password,
    @NotNull final List<CommandLineArgument> additionalArgs) {
    myUser = user;
    myPassword = password;
    myAdditionalArgs = additionalArgs;
  }

  @NotNull
  public String getUser() {
    return myUser;
  }

  @NotNull
  public String getPassword() {
    return myPassword;
  }

  @NotNull
  public List<CommandLineArgument> getAdditionalArgs() {
    return myAdditionalArgs;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final Settings settings = (Settings)o;

    if (!getUser().equals(settings.getUser())) return false;
    if (!getPassword().equals(settings.getPassword())) return false;
    return getAdditionalArgs().equals(settings.getAdditionalArgs());

  }

  @Override
  public int hashCode() {
    int result = getUser().hashCode();
    result = 31 * result + getPassword().hashCode();
    result = 31 * result + getAdditionalArgs().hashCode();
    return result;
  }
}
