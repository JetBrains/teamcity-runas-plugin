package jetbrains.buildServer.runAs.agent;

import java.io.File;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineExecutionContext;
import jetbrains.buildServer.dotNet.buildRunner.agent.ResourcePublisher;
import org.jetbrains.annotations.NotNull;

public class ExecutableFilePublisher implements ResourcePublisher {

  private final FileAccessService myFileAccessService;
  private final ResourcePublisher myBeforeBuildPublisher;

  public ExecutableFilePublisher(
    @NotNull final FileAccessService fileAccessService,
    @NotNull final ResourcePublisher beforeBuildPublisher) {
    myFileAccessService = fileAccessService;
    myBeforeBuildPublisher = beforeBuildPublisher;
  }

  @Override
  public void publishBeforeBuildFile(@NotNull final CommandLineExecutionContext executionContext, @NotNull final File file, @NotNull String content) {
    myBeforeBuildPublisher.publishBeforeBuildFile(executionContext, file, content);
    myFileAccessService.makeExecutableForAll(file);
  }

  @Override
  public void publishAfterBuildArtifactFile(@NotNull final CommandLineExecutionContext executionContext, @NotNull final File file) {
    myBeforeBuildPublisher.publishAfterBuildArtifactFile(executionContext, file);
  }
}