package jetbrains.buildServer.runAs.agent;

import org.jetbrains.annotations.NotNull;

public class RunAsArgsSettings {
  private final String myCommandLine;
  private final String myWorkingDirectory;

  public RunAsArgsSettings(
    @NotNull final String commandLine,
    @NotNull final String workingDirectory) {
    myCommandLine = commandLine;
    myWorkingDirectory = workingDirectory;
  }

  @NotNull
  public String getCommandLine() {
    return myCommandLine;
  }

  @NotNull
  public String getWorkingDirectory() {
    return myWorkingDirectory;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final RunAsArgsSettings that = (RunAsArgsSettings)o;

    if (!getCommandLine().equals(that.getCommandLine())) return false;
    return getWorkingDirectory().equals(that.getWorkingDirectory());

  }

  @Override
  public int hashCode() {
    int result = getCommandLine().hashCode();
    result = 31 * result + getWorkingDirectory().hashCode();
    return result;
  }
}
