package jetbrains.buildServer.runAs.server;

import jetbrains.buildServer.runAs.common.Constants;
import jetbrains.buildServer.serverSide.TeamCityProperties;

public class RunAsConfiguration {
  public boolean getIsUiSupported() {
    return TeamCityProperties.getBoolean(Constants.RUN_AS_UI_ENABLED);
  }
}