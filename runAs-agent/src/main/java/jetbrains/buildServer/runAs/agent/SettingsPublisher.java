package jetbrains.buildServer.runAs.agent;

import java.io.File;
import java.io.IOException;
import jetbrains.buildServer.dotNet.buildRunner.agent.BuildException;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineExecutionContext;
import jetbrains.buildServer.dotNet.buildRunner.agent.FileService;
import jetbrains.buildServer.dotNet.buildRunner.agent.ResourcePublisher;
import org.jetbrains.annotations.NotNull;

public class SettingsPublisher implements ResourcePublisher {
  private final FileService myFileService;

  public SettingsPublisher(
      @NotNull final FileService fileService) {
    myFileService = fileService;
  }

  @Override
  public void publishBeforeBuildFile(@NotNull final CommandLineExecutionContext executionContext, @NotNull final File file, @NotNull String content) {
      try {
        myFileService.writeAllTextFile(content, file);
      }
      catch ( IOException ignored) {
        throw new BuildException("Unable to create temporary file " + file.getPath() + " required to run this build");
      }
  }

  @Override
  public void publishAfterBuildArtifactFile(@NotNull final CommandLineExecutionContext executionContext, @NotNull final File file) {
  }
}