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

import static jetbrains.buildServer.runAs.agent.Constants.CHMOD_TOOL_NAME;
import static org.assertj.core.api.BDDAssertions.then;

public class LinuxFileAccessServiceTest {
  private Mockery myCtx;
  private CommandLineExecutor myCommandLineExecutor;

  @BeforeMethod
  public void setUp()
  {
    myCtx = new Mockery();
    myCommandLineExecutor = myCtx.mock(CommandLineExecutor.class);
  }

  @DataProvider(name = "getSetPermissionsForLinuxCases")
  public Object[][] getSetPermissionsForLinuxCases() {
    return new Object[][] {
      {
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("my_file"), AccessControlAccount.forUser(""), EnumSet.of(AccessPermissions.AllowRead, AccessPermissions.AllowWrite, AccessPermissions.AllowExecute, AccessPermissions.Recursive)))),
        Arrays.asList(
          new CommandLineSetup(CHMOD_TOOL_NAME, Arrays.asList(new CommandLineArgument("-R", CommandLineArgument.Type.PARAMETER), new CommandLineArgument("a+rXwx", CommandLineArgument.Type.PARAMETER), new CommandLineArgument(new File("my_file").getAbsolutePath(), CommandLineArgument.Type.PARAMETER)), Collections.<CommandLineResource>emptyList()))},

      {
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("my_file"), AccessControlAccount.forUser(""), EnumSet.of(AccessPermissions.AllowRead, AccessPermissions.AllowWrite, AccessPermissions.AllowExecute, AccessPermissions.Recursive)),
          new AccessControlEntry(new File("my_file2"), AccessControlAccount.forCurrent(), EnumSet.of(AccessPermissions.AllowExecute)))),
        Arrays.asList(
          new CommandLineSetup(CHMOD_TOOL_NAME, Arrays.asList(new CommandLineArgument("-R", CommandLineArgument.Type.PARAMETER), new CommandLineArgument("a+rXwx", CommandLineArgument.Type.PARAMETER), new CommandLineArgument(new File("my_file").getAbsolutePath(), CommandLineArgument.Type.PARAMETER)), Collections.<CommandLineResource>emptyList()),
          new CommandLineSetup(CHMOD_TOOL_NAME, Arrays.asList(new CommandLineArgument("+x", CommandLineArgument.Type.PARAMETER), new CommandLineArgument(new File("my_file2").getAbsolutePath(), CommandLineArgument.Type.PARAMETER)), Collections.<CommandLineResource>emptyList()))},

      {
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("my_file"), AccessControlAccount.forUser(""), EnumSet.of(AccessPermissions.AllowRead, AccessPermissions.Revoke, AccessPermissions.AllowExecute, AccessPermissions.Recursive)))),
        Arrays.asList(
          new CommandLineSetup(CHMOD_TOOL_NAME, Arrays.asList(new CommandLineArgument("-R", CommandLineArgument.Type.PARAMETER), new CommandLineArgument("a-rwx+rXx", CommandLineArgument.Type.PARAMETER), new CommandLineArgument(new File("my_file").getAbsolutePath(), CommandLineArgument.Type.PARAMETER)), Collections.<CommandLineResource>emptyList()))},

      {
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("my_file"), AccessControlAccount.forCurrent(), EnumSet.of(AccessPermissions.Revoke)))),
        Arrays.asList(
          new CommandLineSetup(CHMOD_TOOL_NAME, Arrays.asList(new CommandLineArgument("-rwx", CommandLineArgument.Type.PARAMETER), new CommandLineArgument(new File("my_file").getAbsolutePath(), CommandLineArgument.Type.PARAMETER)), Collections.<CommandLineResource>emptyList()))},

      {
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("my_file"), AccessControlAccount.forCurrent(), EnumSet.of(AccessPermissions.Recursive)))),
        Arrays.asList()},

      {
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("my_file"), AccessControlAccount.forCurrent(), EnumSet.noneOf(AccessPermissions.class)))),
        Arrays.asList()},
    };
  }

  @Test(dataProvider = "getSetPermissionsForLinuxCases")
  public void shouldSetPermissionsForLinux(
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
    return new LinuxFileAccessService(
      myCommandLineExecutor);
  }
}
