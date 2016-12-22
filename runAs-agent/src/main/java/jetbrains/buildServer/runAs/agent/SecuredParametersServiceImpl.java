package jetbrains.buildServer.runAs.agent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jetbrains.buildServer.agent.*;
import jetbrains.buildServer.dotNet.buildRunner.agent.BuildRunnerContextProvider;
import jetbrains.buildServer.dotNet.buildRunner.agent.RunnerParametersService;
import jetbrains.buildServer.runAs.common.Constants;
import jetbrains.buildServer.util.EventDispatcher;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.util.positioning.PositionConstraint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SecuredParametersServiceImpl extends AgentLifeCycleAdapter implements SecuredParametersService {
  private static final String[] OurProtectedParams = new String[] { Constants.PASSWORD, Constants.PASSWORD, Constants.CREDENTIALS_PROFILE_ID, Constants.CREDENTIALS_DIRECTORY };
  private final BuildRunnerContextProvider myContextProvider;
  private final RunnerParametersService myRunnerParametersService;
  private final Parameters agentProtectedParameters = new Parameters();
  private final Parameters buildProtectedParameters = new Parameters();

  public SecuredParametersServiceImpl(
    @NotNull final EventDispatcher<AgentLifeCycleListener> agentDispatcher,
    @NotNull final BuildRunnerContextProvider contextProvider,
    @NotNull final RunnerParametersService runnerParametersService) {
    myContextProvider = contextProvider;
    myRunnerParametersService = runnerParametersService;
    agentDispatcher.addListener(this);
  }

  @Nullable
  @Override
  public String tryGetBuildFeatureParameter(@NotNull final String buildFeatureType, @NotNull final String parameterName) {
    if(buildProtectedParameters.getBuildFeatureParameters().containsKey(parameterName)) {
      return buildProtectedParameters.getBuildFeatureParameters().get(parameterName);
    }

    final List<String> paramValues = getBuildFeatureParametersInternal(buildFeatureType, parameterName);
    if (paramValues.size() != 0) {
      final String paramValue = paramValues.get(0);
      if (paramValue != null) {
        return paramValue;
      }
    }

    return null;
  }

  @NotNull
  @Override
  public String getRunnerParameter(@NotNull final String parameterName) {
    if(buildProtectedParameters.getRunnerParameters().containsKey(parameterName)) {
      return buildProtectedParameters.getRunnerParameters().get(parameterName);
    }

    return myRunnerParametersService.getConfigParameter(parameterName);
  }

  @Nullable
  @Override
  public String tryGetRunnerParameter(@NotNull final String parameterName) {
    if(buildProtectedParameters.getRunnerParameters().containsKey(parameterName)) {
      return buildProtectedParameters.getRunnerParameters().get(parameterName);
    }

    return myRunnerParametersService.tryGetRunnerParameter(parameterName);
  }

  @NotNull
  @Override
  public String getConfigParameter(@NotNull final String parameterName) {
    if(agentProtectedParameters.getConfigParameters().containsKey(parameterName)) {
      return agentProtectedParameters.getConfigParameters().get(parameterName);
    }

    if(buildProtectedParameters.getConfigParameters().containsKey(parameterName)) {
      return buildProtectedParameters.getConfigParameters().get(parameterName);
    }

    return myRunnerParametersService.getConfigParameter(parameterName);
  }

  @Nullable
  @Override
  public String tryGetConfigParameter(@NotNull final String parameterName) {
    if(agentProtectedParameters.getConfigParameters().containsKey(parameterName)) {
      return agentProtectedParameters.getConfigParameters().get(parameterName);
    }

    if(buildProtectedParameters.getConfigParameters().containsKey(parameterName)) {
      return buildProtectedParameters.getConfigParameters().get(parameterName);
    }

    return myRunnerParametersService.tryGetConfigParameter(parameterName);
  }

  @NotNull
  @Override
  public String getBuildParameter(@NotNull final String parameterName) {
    return myRunnerParametersService.getBuildParameter(parameterName);
  }

  @Nullable
  @Override
  public String tryGetBuildParameter(@NotNull final String parameterName) {
    return myRunnerParametersService.tryGetBuildParameter(parameterName);
  }

  @Override
  public boolean isRunningUnderWindows() {
    return myRunnerParametersService.isRunningUnderWindows();
  }

  @NotNull
  @Override
  public Platform getPlatform() {
    return myRunnerParametersService.getPlatform();
  }

  @NotNull
  @Override
  public String getToolPath(@NotNull final String path) throws ToolCannotBeFoundException {
    return myRunnerParametersService.getToolPath(path);
  }

  @Override
  public void agentStarted(@NotNull final BuildAgent agent) {
    agentProtectedParameters.clear();
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
    buildProtectedParameters.clear();
    if(runningBuild instanceof AgentRunningBuildEx)
    {
      AgentRunningBuildEx agentRunningBuildEx = (AgentRunningBuildEx)runningBuild;
      myContextProvider.initialize(((AgentRunningBuildEx)runningBuild).getCurrentRunnerContext());
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
        final List<String> buildFeatureParameterValues = getBuildFeatureParametersInternal(Constants.BUILD_FEATURE_TYPE, protectedParamName);
        setBuildFeatureParameters(Constants.BUILD_FEATURE_TYPE, protectedParamName, Constants.PASSWORD_REPLACEMENT_VAL);
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
    buildProtectedParameters.clear();
    super.buildFinished(build, buildStatus);
  }

  @Override
  public void agentShutdown() {
    agentProtectedParameters.clear();
    super.agentShutdown();
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

  private List<String> getBuildFeatureParametersInternal(@NotNull final String buildFeatureType, @NotNull final String parameterName) {
    final List<String> params = new ArrayList<String>();
    for(AgentBuildFeature buildFeature: myContextProvider.getContext().getBuild().getBuildFeaturesOfType(buildFeatureType))
    {
      if (!buildFeatureType.equalsIgnoreCase(buildFeature.getType()))
      {
        continue;
      }

      final Map<String, String> allParams = buildFeature.getParameters();
      params.add(allParams.get(parameterName));
    }

    return params;
  }

  private void setBuildFeatureParameters(@NotNull final String buildFeatureType, @NotNull final String parameterName, @Nullable final String parameterValue) {
    for(AgentBuildFeature buildFeature: myContextProvider.getContext().getBuild().getBuildFeaturesOfType(buildFeatureType))
    {
      if (!buildFeatureType.equalsIgnoreCase(buildFeature.getType()))
      {
        continue;
      }

      final Map<String, String> allParams = buildFeature.getParameters();
      if(allParams.containsKey(buildFeatureType)) {
        allParams.put(buildFeatureType, parameterValue);
      }
    }
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

    void clear()
    {
      myConfigParameters.clear();
      myRunnerParameters.clear();
      myBuildFeatureParameters.clear();
    }
  }
}
