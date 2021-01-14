/*
 * Copyright 2000-2021 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
