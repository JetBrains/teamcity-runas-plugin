package jetbrains.buildServer.runAs.agent;

import java.util.List;
import jetbrains.buildServer.dotNet.buildRunner.agent.BuildRunnerContextProvider;
import jetbrains.buildServer.dotNet.buildRunner.agent.RunnerParametersService;
import jetbrains.buildServer.runAs.common.Constants;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ParametersServiceImpl implements ParametersService {
  static final String TEAMCITY_BUILD_LOG_LOG_COMMAND_LINE = "teamcity.buildLog.logCommandLine";
  private final RunnerParametersService myParametersService;
  private final BuildFeatureParametersService myBuildFeatureParametersService;
  private final BuildRunnerContextProvider myContextProvider;

  public ParametersServiceImpl(
    @NotNull final RunnerParametersService parametersService,
    @NotNull final BuildFeatureParametersService buildFeatureParametersService,
    @NotNull final BuildRunnerContextProvider contextProvider) {
    myParametersService = parametersService;
    myBuildFeatureParametersService = buildFeatureParametersService;
    myContextProvider = contextProvider;
  }

  @Nullable
  @Override
  public String tryGetParameter(@NotNull final String paramName) {
    final String isRunAsEnabled = myParametersService.tryGetConfigParameter(Constants.RUN_AS_UI_ENABLED);
    if(StringUtil.isEmpty(isRunAsEnabled) || Boolean.toString(true).equalsIgnoreCase(isRunAsEnabled)) {
      String paramValue = myParametersService.tryGetRunnerParameter(paramName);
      if (!StringUtil.isEmptyOrSpaces(paramValue)) {
        return paramValue;
      }

      final List<String> paramValues = myBuildFeatureParametersService.getBuildFeatureParameters(Constants.BUILD_FEATURE_TYPE, paramName);
      if (paramValues.size() != 0) {
        paramValue = paramValues.get(0);
        if (paramValue != null) {
          return paramValue;
        }
      }
    }

    return myParametersService.tryGetConfigParameter(paramName);
  }

  public void disableLoggingOfCommandLine()
  {
    myContextProvider.getContext().addConfigParameter(TEAMCITY_BUILD_LOG_LOG_COMMAND_LINE, Boolean.toString(false));
  }
}
