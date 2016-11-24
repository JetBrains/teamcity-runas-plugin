package jetbrains.buildServer.runAs.server;

import jetbrains.buildServer.runAs.common.Constants;
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
}
