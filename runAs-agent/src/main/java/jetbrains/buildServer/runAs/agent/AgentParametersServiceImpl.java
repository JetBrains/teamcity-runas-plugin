

package jetbrains.buildServer.runAs.agent;

import jetbrains.buildServer.dotNet.buildRunner.agent.BuildRunnerContextProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AgentParametersServiceImpl implements AgentParametersService {
  private final BuildRunnerContextProvider myContextProvider;

  public AgentParametersServiceImpl(
    @NotNull final BuildRunnerContextProvider contextProvider) {
    myContextProvider = contextProvider;
  }

  @Nullable
  @Override
  public String tryGetConfigParameter(@NotNull final String parameterName) {
    return myContextProvider.getContext().getBuild().getAgentConfiguration().getConfigurationParameters().get(parameterName);
  }
}