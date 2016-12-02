package jetbrains.buildServer.runAs.server;

import java.util.Set;
import jetbrains.buildServer.runAs.common.Constants;
import jetbrains.buildServer.serverSide.BuildStartContext;
import jetbrains.buildServer.serverSide.BuildStartContextProcessor;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

public class RunAsBuildStartContextProcessor implements BuildStartContextProcessor {
  private final RunAsConfiguration myRunAsConfiguration;

  public RunAsBuildStartContextProcessor(@NotNull final RunAsConfiguration runAsConfiguration) {
    myRunAsConfiguration = runAsConfiguration;
  }

  @Override
  public void updateParameters(@NotNull final BuildStartContext buildStartContext) {
    buildStartContext.addSharedParameter(Constants.RUN_AS_UI_ENABLED_VAR, Boolean.toString(myRunAsConfiguration.getIsUiSupported()));
  }
}
