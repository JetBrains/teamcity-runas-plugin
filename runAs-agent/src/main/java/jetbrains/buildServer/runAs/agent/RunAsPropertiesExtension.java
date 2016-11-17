package jetbrains.buildServer.runAs.agent;

import com.intellij.execution.ExecutionException;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.SystemInfo;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import jetbrains.buildServer.ExecResult;
import jetbrains.buildServer.agent.*;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineArgument;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineResource;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineSetup;
import jetbrains.buildServer.runAs.common.Constants;
import jetbrains.buildServer.util.EventDispatcher;
import org.jetbrains.annotations.NotNull;

public class RunAsPropertiesExtension extends AgentLifeCycleAdapter {
  private static final Logger LOG = Logger.getInstance(RunAsPropertiesExtension.class.getName());
  private final ToolProvidersRegistry myToolProvidersRegistry;
  private final CommandLineExecutor myCommandLineExecutor;

  public RunAsPropertiesExtension(
    @NotNull final EventDispatcher<AgentLifeCycleListener> events,
    @NotNull ToolProvidersRegistry toolProvidersRegistry,
    @NotNull final CommandLineExecutor commandLineExecutor) {
    myToolProvidersRegistry = toolProvidersRegistry;
    myCommandLineExecutor = commandLineExecutor;
    events.addListener(this);
  }

  @Override
  public void agentInitialized(@NotNull final BuildAgent agent) {
    super.agentInitialized(agent);
    refreshProperties(agent.getConfiguration());
  }

  @Override
  public void buildFinished(@NotNull final AgentRunningBuild build, @NotNull final BuildFinishedStatus buildStatus) {
    super.buildFinished(build, buildStatus);
    refreshProperties(build.getAgentConfiguration());
  }

  private void refreshProperties(final @NotNull BuildAgentConfiguration config) {
    if(SystemInfo.isWindows) {
      final ToolProvider toolProvider = myToolProvidersRegistry.findToolProvider(Constants.RUN_AS_TOOL_NAME);
      if (toolProvider == null) {
        return;
      }

      final String pathToRunAsPlugin = toolProvider.getPath(Constants.RUN_AS_TOOL_NAME);
      final CommandLineSetup cmdLineSetup = new CommandLineSetup(
        new File(pathToRunAsPlugin, "x86/JetBrains.runAs.exe").getAbsolutePath(),
        Arrays.asList(new CommandLineArgument("-t", CommandLineArgument.Type.PARAMETER)),
        Collections.<CommandLineResource>emptyList());

      try {
        final ExecResult result = myCommandLineExecutor.runProcess(cmdLineSetup, 5000);
        if (result != null) {
          LOG.info("runAs self-test exit code: " + result.getExitCode());
          final int bitness = result.getExitCode();
          if (bitness == 32 || bitness == 64) {
            config.addConfigurationParameter(Constants.RUN_AS_READY_VAR, "YES");
          }
        }
      } catch (ExecutionException e) {
        LOG.error(e);
      }
    }
    else
    {
      config.addConfigurationParameter(Constants.RUN_AS_READY_VAR, "YES");
    }
  }
}