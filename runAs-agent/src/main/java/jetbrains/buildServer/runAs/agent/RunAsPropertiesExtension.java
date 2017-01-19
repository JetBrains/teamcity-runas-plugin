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

import static jetbrains.buildServer.runAs.agent.Constants.*;
import static jetbrains.buildServer.runAs.common.Constants.RUN_AS_TOOL_NAME;

public class RunAsPropertiesExtension extends AgentLifeCycleAdapter implements RunAsAccessService {
  private static final Logger LOG = Logger.getInstance(RunAsPropertiesExtension.class.getName());
  private static final CommandLineSetup OurIcaclsCmdLineSetup = new CommandLineSetup(ICACLS_TOOL_NAME, Collections.<CommandLineArgument>emptyList(), Collections.<CommandLineResource>emptyList());
  private static final CommandLineSetup OurChmodHelpCmdLineSetup = new CommandLineSetup(CHMOD_TOOL_NAME, Arrays.asList(new CommandLineArgument("--help", CommandLineArgument.Type.PARAMETER)), Collections.<CommandLineResource>emptyList());
  private static final CommandLineSetup OurSuCmdLineSetup = new CommandLineSetup(SU_TOOL_NAME, Arrays.asList(new CommandLineArgument("--help", CommandLineArgument.Type.PARAMETER)), Collections.<CommandLineResource>emptyList());
  private final ToolProvidersRegistry myToolProvidersRegistry;
  private final CommandLineExecutor myCommandLineExecutor;
  private boolean myIsRunAsEnabled;

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

  @Override
  public boolean getIsRunAsEnabled() {
    return myIsRunAsEnabled;
  }

  private void refreshProperties(final @NotNull BuildAgentConfiguration config) {
    myIsRunAsEnabled = false;
    if(SystemInfo.isWindows) {
      try {
        myCommandLineExecutor.runProcess(OurIcaclsCmdLineSetup, 600);
      } catch (ExecutionException e) {
        LOG.warn(ICACLS_TOOL_NAME + " is not supported", e);
        return;
      }

      final ToolProvider toolProvider = myToolProvidersRegistry.findToolProvider(RUN_AS_TOOL_NAME);
      if (toolProvider == null) {
        return;
      }

      final String pathToRunAsPlugin = toolProvider.getPath(RUN_AS_TOOL_NAME);
      final String runAsToolPath = new File("x86", RUN_AS_WIN32_TOOL_NAME).getPath();
      final CommandLineSetup cmdLineSetup = new CommandLineSetup(
        new File(pathToRunAsPlugin, runAsToolPath).getAbsolutePath(),
        Arrays.asList(new CommandLineArgument("-t", CommandLineArgument.Type.PARAMETER)),
        Collections.<CommandLineResource>emptyList());

      try {
        final ExecResult result = myCommandLineExecutor.runProcess(cmdLineSetup, 600);
        if (result != null) {
          LOG.info(RUN_AS_WIN32_TOOL_NAME + " self-test exit code: " + result.getExitCode());
          final int bitness = result.getExitCode();
          if (bitness == 32 || bitness == 64) {
            myIsRunAsEnabled = true;
            config.addConfigurationParameter(Constants.RUN_AS_ENABLED, Boolean.toString(true));
          } else {
            LOG.warn("Invalid " + RUN_AS_WIN32_TOOL_NAME + " exit code: " + bitness);
          }
        }
      } catch (ExecutionException e) {
        LOG.warn(RUN_AS_WIN32_TOOL_NAME + " is not supported" , e);
      }
    }
    else
    {
      try {
        myCommandLineExecutor.runProcess(OurChmodHelpCmdLineSetup, 600);
      } catch (ExecutionException e) {
        LOG.warn(CHMOD_TOOL_NAME + " is not supported", e);
        return;
      }

      try {
        myCommandLineExecutor.runProcess(OurSuCmdLineSetup, 600);
      } catch (ExecutionException e) {
        LOG.warn(SU_TOOL_NAME + " is not supported", e);
        return;
      }

      myIsRunAsEnabled = true;
      config.addConfigurationParameter(Constants.RUN_AS_ENABLED, Boolean.toString(true));
    }
  }
}