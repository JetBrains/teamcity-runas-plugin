package jetbrains.buildServer.runAs.server;

import java.util.Arrays;
import java.util.Collection;
import jetbrains.buildServer.runAs.common.Constants;
import jetbrains.buildServer.runAs.common.WindowsIntegrityLevel;
import org.jetbrains.annotations.NotNull;

public class RunAsBean {
  public static final RunAsBean Shared = new RunAsBean();

  @NotNull
  public String getRunAsUserKey() {
    return Constants.USER_VAR;
  }

  @NotNull
  public String getRunAsPasswordKey() {
    return Constants.PASSWORD_VAR;
  }

  @NotNull
  public String getAdditionalCommandLineParametersKey() {
    return Constants.ADDITIONAL_ARGS_VAR;
  }

  @NotNull
  public String getWindowsIntegrityLevelKey() {
    return Constants.WINDOWS_INTEGRITY_LEVEL_VAR;
  }

  @NotNull
  public Collection<WindowsIntegrityLevel> getWindowsIntegrityLevels() {
    return Arrays.asList(WindowsIntegrityLevel.values());
  }
}
