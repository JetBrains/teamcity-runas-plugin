package jetbrains.buildServer.runAs.agent;

import org.jetbrains.annotations.NotNull;

public class RunAsCmdSettings {
  private final String myCommandLine;

  public RunAsCmdSettings(
    @NotNull final String commandLine) {
    myCommandLine = commandLine;
  }

  @NotNull
  public String getCommandLine() {
    return myCommandLine;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final RunAsCmdSettings that = (RunAsCmdSettings)o;

    return getCommandLine().equals(that.getCommandLine());

  }

  @Override
  public int hashCode() {
    return getCommandLine().hashCode();
  }
}
