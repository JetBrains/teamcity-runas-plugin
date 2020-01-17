/*
 * Copyright 2000-2020 JetBrains s.r.o.
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
 import jetbrains.buildServer.agent.BuildAgentConfiguration;
import jetbrains.buildServer.agent.impl.config.BuildAgentConfigurablePaths;
import jetbrains.buildServer.dotNet.buildRunner.agent.BuildStartException;
import jetbrains.buildServer.dotNet.buildRunner.agent.FileService;
import org.jetbrains.annotations.NotNull;

public class PathsServiceImpl implements PathsService {
  private final FileService myFileService;
  private final BuildAgentConfiguration myBuildAgentConfiguration;
  private final BuildAgentConfigurablePaths myBuildAgentConfigurablePaths;

  public PathsServiceImpl(
    @NotNull final FileService fileService,
    @NotNull final BuildAgentConfiguration buildAgentConfiguration,
    @NotNull final BuildAgentConfigurablePaths buildAgentConfigurablePaths) {
    myFileService = fileService;
    myBuildAgentConfiguration = buildAgentConfiguration;
    myBuildAgentConfigurablePaths = buildAgentConfigurablePaths;
  }

  @NotNull
  @Override
  public File getPath(WellKnownPaths wellKnownPath) {
    switch (wellKnownPath) {
      case Checkout:
        return myFileService.getCheckoutDirectory();

      case AgentTemp:
        return myBuildAgentConfigurablePaths.getAgentTempDirectory();

      case BuildTemp:
        return myBuildAgentConfigurablePaths.getBuildTempDirectory();

      case GlobalTemp:
        return myBuildAgentConfigurablePaths.getCacheDirectory();

      case Plugins:
        return myBuildAgentConfiguration.getAgentPluginsDirectory();

      case Tools:
        return myBuildAgentConfiguration.getAgentToolsDirectory();

      case Lib:
        return myBuildAgentConfiguration.getAgentLibDirectory();

      case Work:
        return myBuildAgentConfiguration.getWorkDirectory();

      case System:
        return myBuildAgentConfiguration.getSystemDirectory();

      case Bin:
        return new File(myBuildAgentConfiguration.getAgentHomeDirectory(), "bin");

      case Config:
        return myBuildAgentConfigurablePaths.getAgentConfDirectory();

      case Log:
        return myBuildAgentConfigurablePaths.getAgentLogsDirectory();
    }

    throw new BuildStartException(String.format("Invalid path type \"%s\"", wellKnownPath));
  }
}