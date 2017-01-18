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

      case Bin:
        return new File(myBuildAgentConfiguration.getAgentHomeDirectory(), "bin");
    }

    throw new BuildStartException(String.format("Invalid path type \"%s\"", wellKnownPath));
  }
}