package jetbrains.buildServer.runAs.agent;

import java.io.File;
import java.io.IOException;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineExecutionContext;
import jetbrains.buildServer.dotNet.buildRunner.agent.FileService;
import jetbrains.buildServer.dotNet.buildRunner.agent.ResourcePublisher;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ExecutableFilePublisherTest {
  private Mockery myCtx;
  private FileService myFileService;
  private FileAccessService myFileAccessService;
  private ResourcePublisher myResourcePublisher;

  @BeforeMethod
  public void setUp()
  {
    myCtx = new Mockery();
    myFileAccessService = myCtx.mock(FileAccessService.class);
    myResourcePublisher = myCtx.mock(ResourcePublisher.class);
  }

  @Test()
  public void shouldInvokePublishBeforeBuildFileInBasePublisherAndMakeItExecutableForAllWhenPublishBeforeBuildFile() throws IOException {
    // Given
    final File file = new File("file");
    final CommandLineExecutionContext executionContext = new CommandLineExecutionContext(0);
    myCtx.checking(new Expectations() {{
      oneOf(myResourcePublisher).publishBeforeBuildFile(executionContext, file, "content");

      oneOf(myFileAccessService).makeExecutableForAll(file);
    }});

    final ExecutableFilePublisher instance = createInstance();

    // When
    instance.publishBeforeBuildFile(executionContext, file, "content");

    // Then
    myCtx.assertIsSatisfied();
  }

  @Test()
  public void shouldInvokePublishAfterBuildArtifactFileInBasePublisherWhenPublishBeforeBuildFile() throws IOException {
    // Given
    final File file = new File("file");
    final CommandLineExecutionContext executionContext = new CommandLineExecutionContext(0);
    myCtx.checking(new Expectations() {{
      oneOf(myResourcePublisher).publishAfterBuildArtifactFile(executionContext, file);
    }});

    final ExecutableFilePublisher instance = createInstance();

    // When
    instance.publishAfterBuildArtifactFile(executionContext, file);

    // Then
    myCtx.assertIsSatisfied();
  }

  @NotNull
  private ExecutableFilePublisher createInstance()
  {
    return new ExecutableFilePublisher(myFileAccessService, myResourcePublisher);
  }
}
