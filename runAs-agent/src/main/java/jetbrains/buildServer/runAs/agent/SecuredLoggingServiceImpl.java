

package jetbrains.buildServer.runAs.agent;

import jetbrains.buildServer.dotNet.buildRunner.agent.BuildRunnerContextProvider;
import org.jetbrains.annotations.NotNull;

public class SecuredLoggingServiceImpl implements SecuredLoggingService {
  static final String TEAMCITY_BUILD_LOG_LOG_COMMAND_LINE = "teamcity.buildLog.logCommandLine";
  private final BuildRunnerContextProvider myContextProvider;

  public SecuredLoggingServiceImpl(
    @NotNull final BuildRunnerContextProvider contextProvider) {
    myContextProvider = contextProvider;
  }

  @Override
  public void disableLoggingOfCommandLine()
  {
    myContextProvider.getContext().addConfigParameter(TEAMCITY_BUILD_LOG_LOG_COMMAND_LINE, Boolean.toString(false));
  }
}