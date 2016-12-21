package jetbrains.buildServer.runAs.agent;

import java.util.List;
import jetbrains.buildServer.agent.*;
import jetbrains.buildServer.dotNet.buildRunner.agent.BuildRunnerContextProvider;
import jetbrains.buildServer.dotNet.buildRunner.agent.RunnerParametersService;
import jetbrains.buildServer.runAs.common.Constants;
import jetbrains.buildServer.util.EventDispatcher;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ParametersServiceImpl implements ParametersService {
  private final SecuredParametersService mySecuredParametersService;

  public ParametersServiceImpl(
    @NotNull final SecuredParametersService securedParametersService) {
    mySecuredParametersService = securedParametersService;
  }

  @Nullable
  @Override
  public String tryGetParameter(@NotNull final String paramName) {
    final String isRunAsEnabled = mySecuredParametersService.tryGetConfigParameter(Constants.RUN_AS_UI_ENABLED);
    if(StringUtil.isEmpty(isRunAsEnabled) || Boolean.toString(true).equalsIgnoreCase(isRunAsEnabled)) {
      String paramValue = mySecuredParametersService.tryGetRunnerParameter(paramName);
      if (!StringUtil.isEmptyOrSpaces(paramValue)) {
        return paramValue;
      }

      paramValue = mySecuredParametersService.tryGetBuildFeatureParameter(Constants.BUILD_FEATURE_TYPE, paramName);
      if (!StringUtil.isEmptyOrSpaces(paramValue)) {
        return paramValue;
      }
    }

    return tryGetConfigParameter(paramName);
  }

  @Override
  public String tryGetConfigParameter(@NotNull final String configParamName) {
    return mySecuredParametersService.tryGetConfigParameter(configParamName);
  }
}
