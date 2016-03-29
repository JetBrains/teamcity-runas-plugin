package jetbrains.buildServer.runAs.agent;

import java.util.List;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineArgument;
import org.jetbrains.annotations.NotNull;

public class Settings {
  private final String myUser;
  private final List<CommandLineArgument> myAdditionalArgs;

  public Settings(
    @NotNull final String user,
    @NotNull final List<CommandLineArgument> additionalArgs) {
    myUser = user;
    myAdditionalArgs = additionalArgs;
  }

  @NotNull
  public String getUser() {
    return myUser;
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
    return getAdditionalArgs().equals(settings.getAdditionalArgs());

  }

  @Override
  public int hashCode() {
    int result = getUser().hashCode();
    result = 31 * result + getAdditionalArgs().hashCode();
    return result;
  }
}
