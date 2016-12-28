package jetbrains.buildServer.runAs.agent;

import java.io.File;
 import jetbrains.buildServer.agent.BuildAgentConfiguration;
import jetbrains.buildServer.dotNet.buildRunner.agent.BuildStartException;
import jetbrains.buildServer.dotNet.buildRunner.agent.FileService;
import org.jetbrains.annotations.NotNull;

public class PathsServiceImpl implements PathsService {
  private final FileService myFileService;
  private final BuildAgentConfiguration myBuildAgentConfiguration;

  public PathsServiceImpl(
    @NotNull final FileService fileService,
    @NotNull final BuildAgentConfiguration buildAgentConfiguration) {
    myFileService = fileService;
    myBuildAgentConfiguration = buildAgentConfiguration;
  }

  @NotNull
  @Override
  public File getPath(WellKnownPaths wellKnownPath) {
    switch (wellKnownPath) {
      case Checkout:
        return myFileService.getCheckoutDirectory();

      case AgentTemp:
        return myBuildAgentConfiguration.getAgentTempDirectory();

      case BuildTemp:
        return myBuildAgentConfiguration.getBuildTempDirectory();

      case Plugin:
        return myBuildAgentConfiguration.getAgentPluginsDirectory();

      case Tool:
        return myBuildAgentConfiguration.getAgentToolsDirectory();

      case Bin:
        return new File(myBuildAgentConfiguration.getAgentHomeDirectory(), "bin");
    }

    throw new BuildStartException(String.format("Invalid path type \"%s\"", wellKnownPath));
  }
}