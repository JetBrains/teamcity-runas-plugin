package jetbrains.buildServer.runAs.agent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jetbrains.buildServer.agent.*;
import jetbrains.buildServer.dotNet.buildRunner.agent.BuildRunnerContextProvider;
import jetbrains.buildServer.dotNet.buildRunner.agent.RunnerParametersService;
import jetbrains.buildServer.runAs.common.Constants;
import jetbrains.buildServer.util.EventDispatcher;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.util.positioning.PositionAware;
import jetbrains.buildServer.util.positioning.PositionConstraint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ParametersServiceImpl extends AgentLifeCycleAdapter implements ParametersService, PositionAware {
  static final String TEAMCITY_BUILD_LOG_LOG_COMMAND_LINE = "teamcity.buildLog.logCommandLine";
  private static final String[] OurProtectedParams = new String[] { Constants.PASSWORD, Constants.CREDENTIALS, Constants.CREDENTIALS_DIRECTORY };
  private final BuildRunnerContextProvider myBuildRunnerContextProvider;
  private final RunnerParametersService myRunnerParametersService;
  private final BuildFeatureParametersService myBuildFeatureParametersService;
  private final BuildRunnerContextProvider myContextProvider;
  private final Parameters agentProtectedParameters = new Parameters();
  private final Parameters buildProtectedParameters = new Parameters();

  public ParametersServiceImpl(
    @Nullable final EventDispatcher<AgentLifeCycleListener> agentDispatcher,
    @NotNull final BuildRunnerContextProvider buildRunnerContextProvider,
    @NotNull final RunnerParametersService runnerParametersService,
    @NotNull final BuildFeatureParametersService buildFeatureParametersService,
    @NotNull final BuildRunnerContextProvider contextProvider) {
    myBuildRunnerContextProvider = buildRunnerContextProvider;
    myRunnerParametersService = runnerParametersService;
    myBuildFeatureParametersService = buildFeatureParametersService;
    myContextProvider = contextProvider;
    if(agentDispatcher != null) {
      agentDispatcher.addListener(this);
    }
  }

  @Nullable
  @Override
  public String tryGetParameter(@NotNull final String paramName) {
    final String isRunAsEnabled = myRunnerParametersService.tryGetConfigParameter(Constants.RUN_AS_UI_ENABLED);
    if(StringUtil.isEmpty(isRunAsEnabled) || Boolean.toString(true).equalsIgnoreCase(isRunAsEnabled)) {
      if(buildProtectedParameters.getRunnerParameters().containsKey(paramName)) {
        return buildProtectedParameters.getRunnerParameters().get(paramName);
      }

      String paramValue = myRunnerParametersService.tryGetRunnerParameter(paramName);
      if (!StringUtil.isEmptyOrSpaces(paramValue)) {
        return paramValue;
      }

      if(buildProtectedParameters.getBuildFeatureParameters().containsKey(paramName)) {
        return buildProtectedParameters.getBuildFeatureParameters().get(paramName);
      }

      final List<String> paramValues = myBuildFeatureParametersService.getBuildFeatureParameters(Constants.BUILD_FEATURE_TYPE, paramName);
      if (paramValues.size() != 0) {
        paramValue = paramValues.get(0);
        if (paramValue != null) {
          return paramValue;
        }
      }
    }

    return tryGetConfigParameter(paramName);
  }

  @Override
  public String tryGetConfigParameter(@NotNull final String configParamName) {
    if(agentProtectedParameters.getConfigParameters().containsKey(configParamName)) {
      return agentProtectedParameters.getConfigParameters().get(configParamName);
    }

    if(buildProtectedParameters.getConfigParameters().containsKey(configParamName)) {
      return buildProtectedParameters.getConfigParameters().get(configParamName);
    }

    return myRunnerParametersService.tryGetConfigParameter(configParamName);
  }

  public void disableLoggingOfCommandLine()
  {
    myContextProvider.getContext().addConfigParameter(TEAMCITY_BUILD_LOG_LOG_COMMAND_LINE, Boolean.toString(false));
  }

  @Override
  public void agentStarted(@NotNull final BuildAgent agent) {
    final Map<String, String> configParameters = agent.getConfiguration().getConfigurationParameters();
    for (final String protectedParamName: OurProtectedParams) {
      final String configParameterValue = configParameters.get(protectedParamName);
      if (!StringUtil.isEmptyOrSpaces(configParameterValue)) {
        agent.getConfiguration().addConfigurationParameter(protectedParamName, Constants.PASSWORD_REPLACEMENT_VAL);
        agentProtectedParameters.getConfigParameters().put(protectedParamName, configParameterValue);
      }
    }

    super.agentStarted(agent);
  }

  @Override
  public void buildStarted(@NotNull final AgentRunningBuild runningBuild) {
    if(runningBuild instanceof AgentRunningBuildEx)
    {
      AgentRunningBuildEx agentRunningBuildEx = (AgentRunningBuildEx)runningBuild;
      myBuildRunnerContextProvider.initialize(((AgentRunningBuildEx)runningBuild).getCurrentRunnerContext());
      final Map<String, String> configParameters = this.myContextProvider.getContext().getConfigParameters();
      final Map<String, String> runnerParameters = this.myContextProvider.getContext().getRunnerParameters();

      for (final String protectedParamName: OurProtectedParams) {
        // Config parameters
        final String configParameterValue = configParameters.get(protectedParamName);
        if(!StringUtil.isEmptyOrSpaces(configParameterValue)) {
          myContextProvider.getContext().addConfigParameter(protectedParamName, Constants.PASSWORD_REPLACEMENT_VAL);
          buildProtectedParameters.getConfigParameters().put(protectedParamName, configParameterValue);
        }

        // Runner parameters
        final String runnerParameterValue = runnerParameters.get(protectedParamName);
        if(!StringUtil.isEmptyOrSpaces(runnerParameterValue)) {
          myContextProvider.getContext().addRunnerParameter(protectedParamName, Constants.PASSWORD_REPLACEMENT_VAL);
          buildProtectedParameters.getRunnerParameters().put(protectedParamName, runnerParameterValue);
        }

        // Build Feature parameters
        final List<String> buildFeatureParameterValues = myBuildFeatureParametersService.getBuildFeatureParameters(Constants.BUILD_FEATURE_TYPE, protectedParamName);
        myBuildFeatureParametersService.setBuildFeatureParameters(Constants.BUILD_FEATURE_TYPE, protectedParamName, Constants.PASSWORD_REPLACEMENT_VAL);
        if (buildFeatureParameterValues.size() != 0) {
          final String buildFeatureParameterValue = buildFeatureParameterValues.get(0);
          if(!StringUtil.isEmptyOrSpaces(buildFeatureParameterValue)) {
            buildProtectedParameters.getBuildFeatureParameters().put(protectedParamName, buildFeatureParameterValue);
          }
        }
      }
    }

    super.buildStarted(runningBuild);
  }

  @Override
  public void buildFinished(@NotNull final AgentRunningBuild build, @NotNull final BuildFinishedStatus buildStatus) {
    super.buildFinished(build, buildStatus);
  }

  @NotNull
  @Override
  public String getOrderId() {
    return "";
  }

  @NotNull
  @Override
  public PositionConstraint getConstraint() {
    return PositionConstraint.first();
  }

  private static class Parameters {
    private final HashMap<String, String> myConfigParameters = new HashMap<String, String>();
    private final HashMap<String, String> myRunnerParameters = new HashMap<String, String>();
    private final HashMap<String, String> myBuildFeatureParameters = new HashMap<String, String>();

    HashMap<String, String> getConfigParameters() {
      return myConfigParameters;
    }

    HashMap<String, String> getRunnerParameters() {
      return myRunnerParameters;
    }

    HashMap<String, String> getBuildFeatureParameters() {
      return myBuildFeatureParameters;
    }

    public void clear()
    {
      myConfigParameters.clear();
      myRunnerParameters.clear();
      myBuildFeatureParameters.clear();
    }
  }
}
