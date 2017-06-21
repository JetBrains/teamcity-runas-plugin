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
      // grant
      {
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("my_file"), AccessControlAccount.forUser(""), EnumSet.of(AccessPermissions.GrantRead, AccessPermissions.GrantWrite, AccessPermissions.GrantExecute, AccessPermissions.Recursive), AccessControlScope.Step))),
        Arrays.asList(
          new CommandLineSetup(CHMOD_TOOL_NAME, Arrays.asList(new CommandLineArgument("-R", CommandLineArgument.Type.PARAMETER), new CommandLineArgument("a+rXwx", CommandLineArgument.Type.PARAMETER), new CommandLineArgument(new File("my_file").getAbsolutePath(), CommandLineArgument.Type.PARAMETER)), Collections.<CommandLineResource>emptyList()))},

      // deny
      {
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("my_file"), AccessControlAccount.forUser(""), EnumSet.of(AccessPermissions.DenyRead, AccessPermissions.DenyWrite, AccessPermissions.DenyExecute, AccessPermissions.Recursive), AccessControlScope.Step))),
        Arrays.asList(
          new CommandLineSetup(CHMOD_TOOL_NAME, Arrays.asList(new CommandLineArgument("-R", CommandLineArgument.Type.PARAMETER), new CommandLineArgument("go-rwx", CommandLineArgument.Type.PARAMETER), new CommandLineArgument(new File("my_file").getAbsolutePath(), CommandLineArgument.Type.PARAMETER)), Collections.<CommandLineResource>emptyList()))},

      // grant & deny
      {
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("my_file"), AccessControlAccount.forUser(""), EnumSet.of(AccessPermissions.GrantRead, AccessPermissions.GrantWrite, AccessPermissions.DenyWrite, AccessPermissions.GrantExecute, AccessPermissions.Recursive), AccessControlScope.Step))),
        Arrays.asList(
          new CommandLineSetup(CHMOD_TOOL_NAME, Arrays.asList(new CommandLineArgument("-R", CommandLineArgument.Type.PARAMETER), new CommandLineArgument("a+rXwx", CommandLineArgument.Type.PARAMETER), new CommandLineArgument(new File("my_file").getAbsolutePath(), CommandLineArgument.Type.PARAMETER)), Collections.<CommandLineResource>emptyList()),
          new CommandLineSetup(CHMOD_TOOL_NAME, Arrays.asList(new CommandLineArgument("-R", CommandLineArgument.Type.PARAMETER), new CommandLineArgument("go-w", CommandLineArgument.Type.PARAMETER), new CommandLineArgument(new File("my_file").getAbsolutePath(), CommandLineArgument.Type.PARAMETER)), Collections.<CommandLineResource>emptyList()))},

      // grant & deny all
      {
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("my_file"), AccessControlAccount.forAll(), EnumSet.of(AccessPermissions.GrantRead, AccessPermissions.GrantWrite, AccessPermissions.DenyWrite, AccessPermissions.GrantExecute, AccessPermissions.Recursive), AccessControlScope.Step))),
        Arrays.asList(
          new CommandLineSetup(CHMOD_TOOL_NAME, Arrays.asList(new CommandLineArgument("-R", CommandLineArgument.Type.PARAMETER), new CommandLineArgument("a+rXwx", CommandLineArgument.Type.PARAMETER), new CommandLineArgument(new File("my_file").getAbsolutePath(), CommandLineArgument.Type.PARAMETER)), Collections.<CommandLineResource>emptyList()),
          new CommandLineSetup(CHMOD_TOOL_NAME, Arrays.asList(new CommandLineArgument("-R", CommandLineArgument.Type.PARAMETER), new CommandLineArgument("a-w", CommandLineArgument.Type.PARAMETER), new CommandLineArgument(new File("my_file").getAbsolutePath(), CommandLineArgument.Type.PARAMETER)), Collections.<CommandLineResource>emptyList()))},

      {
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("my_file"), AccessControlAccount.forUser(""), EnumSet.of(AccessPermissions.GrantRead, AccessPermissions.GrantWrite, AccessPermissions.GrantExecute, AccessPermissions.Recursive), AccessControlScope.Step),
          new AccessControlEntry(new File("my_file2"), AccessControlAccount.forAll(), EnumSet.of(AccessPermissions.GrantExecute), AccessControlScope.Step))),
        Arrays.asList(
          new CommandLineSetup(CHMOD_TOOL_NAME, Arrays.asList(new CommandLineArgument("-R", CommandLineArgument.Type.PARAMETER), new CommandLineArgument("a+rXwx", CommandLineArgument.Type.PARAMETER), new CommandLineArgument(new File("my_file").getAbsolutePath(), CommandLineArgument.Type.PARAMETER)), Collections.<CommandLineResource>emptyList()),
          new CommandLineSetup(CHMOD_TOOL_NAME, Arrays.asList(new CommandLineArgument("a+x", CommandLineArgument.Type.PARAMETER), new CommandLineArgument(new File("my_file2").getAbsolutePath(), CommandLineArgument.Type.PARAMETER)), Collections.<CommandLineResource>emptyList()))},

      {
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("my_file"), AccessControlAccount.forAll(), EnumSet.of(AccessPermissions.Recursive), AccessControlScope.Step))),
        Arrays.asList()},

      {
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("my_file"), AccessControlAccount.forAll(), EnumSet.noneOf(AccessPermissions.class), AccessControlScope.Step))),
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
    myCtx.assertIsSatisfied();
    then(actualCommandLineSetups).isEqualTo(expectedCommandLineSetups);
  }

  @NotNull
  private FileAccessService createInstance()
  {
    return new LinuxFileAccessService(
      myCommandLineExecutor);
  }
}
