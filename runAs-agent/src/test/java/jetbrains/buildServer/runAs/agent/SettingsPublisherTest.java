package jetbrains.buildServer.runAs.agent;

import java.io.File;
import java.io.IOException;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineExecutionContext;
import jetbrains.buildServer.dotNet.buildRunner.agent.FileService;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SettingsPublisherTest {
  private Mockery myCtx;
  private FileService myFileService;

  @BeforeMethod
  public void setUp()
  {
    myCtx = new Mockery();
    myFileService = myCtx.mock(FileService.class);
  }

  @Test()
  public void shouldWriteFileToDiskWhenPublishBeforeBuild() throws IOException {
    // Given
    final File file = new File("file");
    myCtx.checking(new Expectations() {{
      oneOf(myFileService).writeAllTextFile("content", file);
    }});

    final SettingsPublisher instance = createInstance();

    // When
    instance.publishBeforeBuildFile(new CommandLineExecutionContext(0), file, "content");

    // Then
    myCtx.assertIsSatisfied();
  }

  @NotNull
  private SettingsPublisher createInstance()
  {
    return new SettingsPublisher(myFileService);
  }
}
