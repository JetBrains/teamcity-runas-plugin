

package jetbrains.buildServer.runAs.server;

import jetbrains.buildServer.runAs.common.Constants;
import jetbrains.buildServer.serverSide.TeamCityProperties;

public class RunAsConfiguration {
  public boolean getIsUiSupported() {
    return TeamCityProperties.getBooleanOrTrue(Constants.RUN_AS_UI_ENABLED);
  }

  public boolean getIsUiForBuildStepsSupported() {
    return TeamCityProperties.getBoolean(Constants.RUN_AS_UI_STEPS_ENABLED);
  }

  public boolean getIsAclConfiguringSupported() {
    return TeamCityProperties.getBoolean(Constants.RUN_AS_ACL_DEFAULTS_ENABLED);
  }
}