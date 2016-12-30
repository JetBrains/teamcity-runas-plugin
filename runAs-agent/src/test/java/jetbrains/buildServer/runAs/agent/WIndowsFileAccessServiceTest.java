package jetbrains.buildServer.runAs.agent;

import com.intellij.execution.ExecutionException;
import java.io.File;
import java.util.*;
import jetbrains.buildServer.ExecResult;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineArgument;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineResource;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineSetup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.assertj.core.api.BDDAssertions.then;

public class WIndowsFileAccessServiceTest {
  private Mockery myCtx;
  private CommandLineExecutor myCommandLineExecutor;

  @BeforeMethod
  public void setUp()
  {
    myCtx = new Mockery();
    myCommandLineExecutor = myCtx.mock(CommandLineExecutor.class);
  }

  @DataProvider(name = "getSetPermissionsForWindowsCases")
  public Object[][] getSetPermissionsForWindowsCases() {
    return new Object[][] {
      // full access
      {
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("my_file"), AccessControlAccount.forUser("user1"), EnumSet.of(AccessPermissions.AllowRead, AccessPermissions.AllowWrite, AccessPermissions.AllowExecute)))),
        Arrays.asList(
          new CommandLineSetup(WindowsFileAccessService.CACLS_TOOL, Arrays.asList(
            new CommandLineArgument(new File("my_file").getAbsolutePath(), CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("/C", CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("/Q", CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("/grant", CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("user1:(R,W,D,DC,RX)", CommandLineArgument.Type.PARAMETER)), Collections.<CommandLineResource>emptyList()))},

      // full access recursive
      {
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("my_file"), AccessControlAccount.forUser("user1"), EnumSet.of(AccessPermissions.Recursive, AccessPermissions.AllowRead, AccessPermissions.AllowWrite, AccessPermissions.AllowExecute)))),
        Arrays.asList(
          new CommandLineSetup(WindowsFileAccessService.CACLS_TOOL, Arrays.asList(
            new CommandLineArgument(new File("my_file").getAbsolutePath(), CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("/C", CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("/Q", CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("/grant", CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("user1:(OI)(CI)(R,W,D,DC,RX)", CommandLineArgument.Type.PARAMETER)), Collections.<CommandLineResource>emptyList()))},

      // full access recursive and revoke
      {
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("my_file"), AccessControlAccount.forUser("user1"), EnumSet.of(AccessPermissions.Revoke, AccessPermissions.Recursive, AccessPermissions.AllowRead, AccessPermissions.AllowWrite, AccessPermissions.AllowExecute)))),
        Arrays.asList(
          new CommandLineSetup(WindowsFileAccessService.CACLS_TOOL, Arrays.asList(
            new CommandLineArgument(new File("my_file").getAbsolutePath(), CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("/C", CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("/Q", CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("/remove", CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("user1", CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("/grant", CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("user1:(OI)(CI)(R,W,D,DC,RX)", CommandLineArgument.Type.PARAMETER)), Collections.<CommandLineResource>emptyList()))},

      // read access
      {
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("my_file"), AccessControlAccount.forUser("user1"), EnumSet.of(AccessPermissions.AllowRead)))),
        Arrays.asList(
          new CommandLineSetup(WindowsFileAccessService.CACLS_TOOL, Arrays.asList(
            new CommandLineArgument(new File("my_file").getAbsolutePath(), CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("/C", CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("/Q", CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("/grant", CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("user1:(R)", CommandLineArgument.Type.PARAMETER)), Collections.<CommandLineResource>emptyList()))},

      // write access
      {
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("my_file"), AccessControlAccount.forUser("user1"), EnumSet.of(AccessPermissions.AllowWrite)))),
        Arrays.asList(
          new CommandLineSetup(WindowsFileAccessService.CACLS_TOOL, Arrays.asList(
            new CommandLineArgument(new File("my_file").getAbsolutePath(), CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("/C", CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("/Q", CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("/grant", CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("user1:(W,D,DC)", CommandLineArgument.Type.PARAMETER)), Collections.<CommandLineResource>emptyList()))},

      // read/write access
      {
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("my_file"), AccessControlAccount.forUser("user1"), EnumSet.of(AccessPermissions.AllowRead, AccessPermissions.AllowWrite)))),
        Arrays.asList(
          new CommandLineSetup(WindowsFileAccessService.CACLS_TOOL, Arrays.asList(
            new CommandLineArgument(new File("my_file").getAbsolutePath(), CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("/C", CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("/Q", CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("/grant", CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("user1:(R,W,D,DC)", CommandLineArgument.Type.PARAMETER)), Collections.<CommandLineResource>emptyList()))},

      // revoke access
      {
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("my_file"), AccessControlAccount.forUser("user1"), EnumSet.of(AccessPermissions.Revoke)))),
        Arrays.asList(
          new CommandLineSetup(WindowsFileAccessService.CACLS_TOOL, Arrays.asList(
            new CommandLineArgument(new File("my_file").getAbsolutePath(), CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("/C", CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("/Q", CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("/remove", CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("user1", CommandLineArgument.Type.PARAMETER)), Collections.<CommandLineResource>emptyList()))},

      // replace by read/write access
      {
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("my_file"), AccessControlAccount.forUser("user1"), EnumSet.of(AccessPermissions.Recursive, AccessPermissions.AllowRead, AccessPermissions.AllowWrite)))),
        Arrays.asList(
          new CommandLineSetup(WindowsFileAccessService.CACLS_TOOL, Arrays.asList(
            new CommandLineArgument(new File("my_file").getAbsolutePath(), CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("/C", CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("/Q", CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("/grant", CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("user1:(OI)(CI)(R,W,D,DC)", CommandLineArgument.Type.PARAMETER)), Collections.<CommandLineResource>emptyList()))},

    };
  }

  @Test(dataProvider = "getSetPermissionsForWindowsCases")
  public void shouldSetPermissionsForWindows(
    @NotNull final AccessControlList accessControlList,
    @Nullable final List<CommandLineSetup> expectedCommandLineSetups) throws ExecutionException {
    // Given
    final FileAccessService instance = createInstance();
    final ArrayList<CommandLineSetup> actualCommandLineSetups = new ArrayList<CommandLineSetup>();

    myCtx.checking(new Expectations() {{
      allowing(myCommandLineExecutor).runProcess(with(any(CommandLineSetup.class)), with(any(int.class)));
      will(new CustomAction("runProcess") {
        @Override
        public Object invoke(final Invocation invocation) throws Throwable {
          actualCommandLineSetups.add((CommandLineSetup)invocation.getParameter(0));
          return new ExecResult();
        }
      });
    }});

    // When
    instance.setAccess(accessControlList);

    // Then
    then(actualCommandLineSetups).isEqualTo(expectedCommandLineSetups);
  }

  @NotNull
  private FileAccessService createInstance()
  {
    return new WindowsFileAccessService(
      myCommandLineExecutor);
  }
}
