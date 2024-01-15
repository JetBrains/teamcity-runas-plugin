

package jetbrains.buildServer.runAs.agent;

import java.io.File;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.agent.ToolCannotBeFoundException;
import jetbrains.buildServer.agent.ToolProvidersRegistry;
import jetbrains.buildServer.agent.plugins.beans.PluginDescriptor;
import jetbrains.buildServer.runAs.common.Constants;
import org.jetbrains.annotations.NotNull;

public class RunAsToolProvider {
  static final String BIN_PATH = "bin";

  public RunAsToolProvider(
    @NotNull final PluginDescriptor pluginDescriptor,
    @NotNull final ToolProvidersRegistry toolProvidersRegistry) {

    toolProvidersRegistry.registerToolProvider(new jetbrains.buildServer.agent.ToolProvider() {
      @Override
      public boolean supports(@NotNull final String toolName) {
        return Constants.RUN_AS_TOOL_NAME.equalsIgnoreCase(toolName);
      }

      @Override
      @NotNull
      public String getPath(@NotNull final String toolName) throws ToolCannotBeFoundException {
        if(!supports(toolName)) {
          throw new ToolCannotBeFoundException("Tool is not supported");
        }

        try
        {
          return new File(pluginDescriptor.getPluginRoot(), BIN_PATH).getAbsolutePath();
        }
        catch (Exception ex) {
          throw new ToolCannotBeFoundException(ex.getMessage());
        }
      }

      @Override
      @NotNull
      public String getPath(@NotNull String toolName, @NotNull AgentRunningBuild build, @NotNull BuildRunnerContext runner) throws ToolCannotBeFoundException {
        return getPath(toolName);
      }
    });
  }
}