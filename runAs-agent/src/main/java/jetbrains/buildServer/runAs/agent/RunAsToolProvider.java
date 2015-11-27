package jetbrains.buildServer.runAs.agent;

import com.intellij.openapi.util.text.StringUtil;
import java.io.File;
import java.util.Collections;
import jetbrains.buildServer.ExecResult;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.agent.ToolCannotBeFoundException;
import jetbrains.buildServer.agent.ToolProvidersRegistry;
import jetbrains.buildServer.agent.plugins.beans.PluginDescriptor;
import jetbrains.buildServer.dotNet.buildRunner.agent.*;
import jetbrains.buildServer.runAs.common.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RunAsToolProvider {
  static final String BIN_PATH = "bin";
  static final String GET_OS_BITNESS_TOOL_FILE_NAME = "JetBrains.getOSBitness.exe";
  @SuppressWarnings("FieldCanBeLocal") static final int EXECUTION_TIMEOUT_SECONDS = 60;
  private static final int OS_32_BITNESS = 32;
  private static final int OS_64_BITNESS = 64;
  private static final String OS_64_BITNESS_PLATFORM_SHORT_NAME = "x64";
  private static final String OS_32_BITNESS_PLATFORM_SHORT_NAME = "x86";
  private boolean myHasPath;
  @Nullable private String myPath;

  public RunAsToolProvider(
    @NotNull final PluginDescriptor pluginDescriptor,
    @NotNull final ToolProvidersRegistry toolProvidersRegistry,
    @NotNull final RunnerParametersService parametersService,
    @NotNull final FileService fileService,
    @NotNull final CommandLineExecutor commandLineExecutor) {

    toolProvidersRegistry.registerToolProvider(new jetbrains.buildServer.agent.ToolProvider() {
      @Override
      public boolean supports(@NotNull final String toolName) {
        return Constants.RUN_AS_TOOL_NAME.equalsIgnoreCase(toolName);
      }

      @Override
      @NotNull
      public String getPath(@NotNull final String toolName) throws ToolCannotBeFoundException {
        if(myHasPath) {
          return myPath;
        }

        final boolean isRunningUnderWindows = parametersService.isRunningUnderWindows();
        if(!supports(toolName)) {
          throw new ToolCannotBeFoundException("Tool is not supported");
        }

        if (!isRunningUnderWindows) {
          throw new ToolCannotBeFoundException("Tool is not supported for this OS.");
        }

        try
        {
          myPath = tryGetPath(pluginDescriptor, fileService, commandLineExecutor);
          myHasPath = true;
        }
        catch (Exception ex) {
          throw new ToolCannotBeFoundException(ex.getMessage());
        }

        if(StringUtil.isEmptyOrSpaces(myPath)) {
          throw new ToolCannotBeFoundException("Tool path can't be defined.");
        }

        return myPath;
      }

      @Override
      @NotNull
      public String getPath(@NotNull String toolName, @NotNull AgentRunningBuild build, @NotNull BuildRunnerContext runner) throws ToolCannotBeFoundException {
        return getPath(toolName);
      }
    });
  }

  @Nullable
  private String tryGetPath(final @NotNull PluginDescriptor pluginDescriptor, final @NotNull FileService fileService, final @NotNull CommandLineExecutor commandLineExecutor) throws Exception {
    final File rootPath = new File(pluginDescriptor.getPluginRoot(), BIN_PATH).getAbsoluteFile();
    final File path = new File(rootPath, GET_OS_BITNESS_TOOL_FILE_NAME);
    fileService.validatePath(path);

    final CommandLineSetup commandLineSetup = new CommandLineSetup(path.getAbsolutePath(), Collections.<CommandLineArgument>emptyList(), Collections.<CommandLineResource>emptyList());
    ExecResult result = commandLineExecutor.runProcess(commandLineSetup, EXECUTION_TIMEOUT_SECONDS);

    if (result != null) {
      switch (result.getExitCode())
      {
        case OS_64_BITNESS:
          return new File(rootPath, OS_64_BITNESS_PLATFORM_SHORT_NAME).getAbsolutePath();

        case OS_32_BITNESS:
          return new File(rootPath, OS_32_BITNESS_PLATFORM_SHORT_NAME).getAbsolutePath();

        default:
          return null;
      }
    }

    return null;
  }
}
