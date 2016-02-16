package jetbrains.buildServer.runAs.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import jetbrains.buildServer.agent.AgentBuildFeature;
import jetbrains.buildServer.dotNet.buildRunner.agent.BuildRunnerContextProvider;
import org.jetbrains.annotations.NotNull;

public class BuildFeatureParametersServiceImpl implements BuildFeatureParametersService {
  private final BuildRunnerContextProvider myContextProvider;

  public BuildFeatureParametersServiceImpl(@NotNull final BuildRunnerContextProvider contextProvider) {

    myContextProvider = contextProvider;
  }

  @NotNull
  @Override
  public List<String> getBuildFeatureParameters(@NotNull final String buildFeatureType, @NotNull final String parameterName) {
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
}
