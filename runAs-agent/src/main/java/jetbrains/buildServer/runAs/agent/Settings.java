package jetbrains.buildServer.runAs.agent;

import java.util.List;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineArgument;
import jetbrains.buildServer.runAs.common.LoggingLevel;
import jetbrains.buildServer.runAs.common.WindowsIntegrityLevel;
import org.jetbrains.annotations.NotNull;

public class Settings {
  private final UserCredentials myUserCredentials;
  private final WindowsIntegrityLevel myWindowsIntegrityLevel;
  private final LoggingLevel myWindowsLoggingLevel;
  private final List<CommandLineArgument> myAdditionalArgs;

  public Settings(
    @NotNull final UserCredentials userCredentials,
    @NotNull final WindowsIntegrityLevel windowsIntegrityLevel,
    @NotNull final LoggingLevel windowsLoggingLevel,
    @NotNull final List<CommandLineArgument> additionalArgs) {
    myUserCredentials = userCredentials;
    myWindowsIntegrityLevel = windowsIntegrityLevel;
    myWindowsLoggingLevel = windowsLoggingLevel;
    myAdditionalArgs = additionalArgs;
  }

  public UserCredentials getUserCredentials() {
    return myUserCredentials;
  }

  public WindowsIntegrityLevel getWindowsIntegrityLevel() {
    return myWindowsIntegrityLevel;
  }

  public LoggingLevel getWindowsLoggingLevel() {
    return myWindowsLoggingLevel;
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

    if (!myUserCredentials.equals(settings.myUserCredentials)) return false;
    if (myWindowsIntegrityLevel != settings.myWindowsIntegrityLevel) return false;
    if (myWindowsLoggingLevel != settings.myWindowsLoggingLevel) return false;
    return myAdditionalArgs.equals(settings.myAdditionalArgs);

  }

  @Override
  public int hashCode() {
    int result = myUserCredentials.hashCode();
    result = 31 * result + myWindowsIntegrityLevel.hashCode();
    result = 31 * result + myWindowsLoggingLevel.hashCode();
    result = 31 * result + myAdditionalArgs.hashCode();
    return result;
  }
}
