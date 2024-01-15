

package jetbrains.buildServer.runAs.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import jetbrains.buildServer.agent.AgentBuildFeature;
import jetbrains.buildServer.dotNet.buildRunner.agent.BuildRunnerContextProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BuildFeatureParametersServiceImpl implements BuildFeatureParametersService {
  private final BuildRunnerContextProvider myContextProvider;

  public BuildFeatureParametersServiceImpl(
    BuildRunnerContextProvider contextProvider) {
    myContextProvider = contextProvider;
  }

  @Nullable
  @Override
  public String tryGetBuildFeatureParameter(@NotNull final String buildFeatureType, @NotNull final String parameterName) {
    final List<String> params = new ArrayList<String>();
    for(AgentBuildFeature buildFeature: myContextProvider.getContext().getBuild().getBuildFeaturesOfType(buildFeatureType))
    {
      if (!buildFeatureType.equalsIgnoreCase(buildFeature.getType()))
      {
        continue;
      }

      final Map<String, String> allParams = buildFeature.getParameters();
      return allParams.get(parameterName);
    }

    return null;
  }
}