package jetbrains.buildServer.runAs.agent;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineExecutionContext;
import jetbrains.buildServer.dotNet.buildRunner.agent.FileService;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AccessControlResourceTest {
  private Mockery myCtx;
  private FileService myFileService;
  private FileAccessService myFileAccessService;

  @BeforeMethod
  public void setUp()
  {
    myCtx = new Mockery();
    myFileAccessService = myCtx.mock(FileAccessService.class);
  }

  @Test()
  public void shouldSetAccess() throws IOException {
    // Given
    final AccessControlList acl = new AccessControlList(Arrays.asList(new AccessControlEntry(new File("file"), AccessControlAccount.forAll(), EnumSet.of(AccessPermissions.AllowWrite, AccessPermissions.AllowExecute), true)));
    final CommandLineExecutionContext executionContext = new CommandLineExecutionContext(0);
    myCtx.checking(new Expectations() {{
      oneOf(myFileAccessService).setAccess(acl);
    }});

    final AccessControlResource instance = createInstance();
    instance.setAccess(acl);

    // When
    instance.publishBeforeBuild(executionContext);

    // Then
    myCtx.assertIsSatisfied();
  }

  @NotNull
  private AccessControlResource createInstance()
  {
    return new AccessControlResourceImpl(myFileAccessService);
  }
}
