package jetbrains.buildServer.runAs.agent;

import java.util.List;
import jetbrains.buildServer.dotNet.buildRunner.agent.RunnerParametersService;
import jetbrains.buildServer.runAs.common.Constants;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ParametersServiceImpl implements ParametersService {
  private final RunnerParametersService myParametersService;
  private final BuildFeatureParametersService myBuildFeatureParametersService;

  public ParametersServiceImpl(
    @NotNull final RunnerParametersService parametersService,
    @NotNull final BuildFeatureParametersService buildFeatureParametersService) {
    myParametersService = parametersService;
    myBuildFeatureParametersService = buildFeatureParametersService;
  }

  @Nullable
  @Override
  public String tryGetParameter(@NotNull final String paramName) {
    final String isRunAsEnabled = myParametersService.tryGetConfigParameter(Constants.RUN_AS_UI_ENABLED_VAR);
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
}
