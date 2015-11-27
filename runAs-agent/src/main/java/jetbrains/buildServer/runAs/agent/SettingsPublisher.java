package jetbrains.buildServer.runAs.agent;

import java.io.File;
import java.io.IOException;
import jetbrains.buildServer.dotNet.buildRunner.agent.*;
import jetbrains.buildServer.messages.serviceMessages.Message;
import org.jetbrains.annotations.NotNull;

public class SettingsPublisher implements ResourcePublisher {
  private final FileService myFileService;

  public SettingsPublisher(
      @NotNull final FileService fileService) {
    myFileService = fileService;
  }

  public void publishBeforeBuildFile(@NotNull final CommandLineExecutionContext executionContext, @NotNull final File file, @NotNull String content) {
      try {
        myFileService.writeAllTextFile(content, file);
      }
      catch ( IOException ignored) {
        throw new BuildException("Unable to create temporary file " + file.getPath() + " required to run this build");
      }
  }

  public void publishAfterBuildArtifactFile(@NotNull final CommandLineExecutionContext executionContext, @NotNull final File file) {
  }
}