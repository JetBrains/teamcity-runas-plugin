package jetbrains.buildServer.runAs.server;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import jetbrains.buildServer.runAs.common.Constants;
import jetbrains.buildServer.runAs.common.LoggingLevel;
import jetbrains.buildServer.runAs.common.WindowsIntegrityLevel;
import org.jetbrains.annotations.NotNull;

public class RunAsBean {
  public static final RunAsBean Shared = new RunAsBean();

  @NotNull
  public String getRunAsUserKey() {
    return Constants.USER;
  }

  @NotNull
  public String getRunAsPasswordKey() {
    return Constants.PASSWORD;
  }

  @NotNull
  public String getAdditionalCommandLineParametersKey() {
    return Constants.ADDITIONAL_ARGS;
  }

  @NotNull
  public String getWindowsIntegrityLevelKey() {
    return Constants.WINDOWS_INTEGRITY_LEVEL;
  }

  @NotNull
  public List<WindowsIntegrityLevel> getWindowsIntegrityLevels() {
    return Arrays.asList(WindowsIntegrityLevel.values());
  }

  @NotNull
  public String getWindowsLoggingLevelKey() {
    return Constants.LOGGING_LEVEL;
  }

  @NotNull
  public List<LoggingLevel> getLoggingLevels() {
    return Arrays.asList(LoggingLevel.values());
  }
}
