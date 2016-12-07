package jetbrains.buildServer.runAs.agent;

import com.intellij.execution.ExecutionException;
import java.io.File;
import java.util.*;
import jetbrains.buildServer.ExecResult;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineArgument;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineResource;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineSetup;
import jetbrains.buildServer.dotNet.buildRunner.agent.RunnerParametersService;
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

public class FileAccessServiceTest {
  private Mockery myCtx;
  private RunnerParametersService myRunnerParametersService;
  private CommandLineExecutor myCommandLineExecutor;

  @BeforeMethod
  public void setUp()
  {
    myCtx = new Mockery();
    myRunnerParametersService = myCtx.mock(RunnerParametersService.class);
    myCommandLineExecutor = myCtx.mock(CommandLineExecutor.class);
  }

  @DataProvider(name = "setPermissionsCases")
  public Object[][] getSetPermissionsCases() {
    return new Object[][] {
      {
        false,
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("my_file"), AccessControlAccount.forAll(), EnumSet.of(AccessPermissions.AllowRead, AccessPermissions.AllowWrite, AccessPermissions.AllowExecute), true))),
        Arrays.asList(
          new CommandLineSetup("chmod", Arrays.asList(new CommandLineArgument("-R", CommandLineArgument.Type.PARAMETER), new CommandLineArgument("a+rwx", CommandLineArgument.Type.PARAMETER), new CommandLineArgument(new File("my_file").getAbsolutePath(), CommandLineArgument.Type.PARAMETER)), Collections.<CommandLineResource>emptyList()))},

      {
        false,
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("my_file"), AccessControlAccount.forAll(), EnumSet.of(AccessPermissions.AllowRead, AccessPermissions.AllowWrite, AccessPermissions.AllowExecute), true),
          new AccessControlEntry(new File("my_file2"), AccessControlAccount.forCurrent(), EnumSet.of(AccessPermissions.AllowExecute), false))),
        Arrays.asList(
          new CommandLineSetup("chmod", Arrays.asList(new CommandLineArgument("-R", CommandLineArgument.Type.PARAMETER), new CommandLineArgument("a+rwx", CommandLineArgument.Type.PARAMETER), new CommandLineArgument(new File("my_file").getAbsolutePath(), CommandLineArgument.Type.PARAMETER)), Collections.<CommandLineResource>emptyList()),
          new CommandLineSetup("chmod", Arrays.asList(new CommandLineArgument("+x", CommandLineArgument.Type.PARAMETER), new CommandLineArgument(new File("my_file2").getAbsolutePath(), CommandLineArgument.Type.PARAMETER)), Collections.<CommandLineResource>emptyList()))},

      {
        false,
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("my_file"), AccessControlAccount.forAll(), EnumSet.of(AccessPermissions.AllowRead, AccessPermissions.DenyWrite, AccessPermissions.AllowExecute), true))),
        Arrays.asList(
          new CommandLineSetup("chmod", Arrays.asList(new CommandLineArgument("-R", CommandLineArgument.Type.PARAMETER), new CommandLineArgument("a+rx-w", CommandLineArgument.Type.PARAMETER), new CommandLineArgument(new File("my_file").getAbsolutePath(), CommandLineArgument.Type.PARAMETER)), Collections.<CommandLineResource>emptyList()))},

      {
        false,
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("my_file"), AccessControlAccount.forCurrent(), EnumSet.of(AccessPermissions.DenyWrite, AccessPermissions.DenyRead), false))),
        Arrays.asList(
          new CommandLineSetup("chmod", Arrays.asList(new CommandLineArgument("-rw", CommandLineArgument.Type.PARAMETER), new CommandLineArgument(new File("my_file").getAbsolutePath(), CommandLineArgument.Type.PARAMETER)), Collections.<CommandLineResource>emptyList()))},

      {
        false,
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("my_file"), AccessControlAccount.forCurrent(), EnumSet.noneOf(AccessPermissions.class), true))),
        Arrays.asList()},

      {
        false,
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("my_file"), AccessControlAccount.forCurrent(), EnumSet.noneOf(AccessPermissions.class), false))),
        Arrays.asList()},
    };
  }

  @Test(dataProvider = "setPermissionsCases")
  public void shouldSetPermissions(final boolean isRunningUnderWindows, @NotNull final AccessControlList accessControlList, @Nullable final List<CommandLineSetup> expectedCommandLineSetups) throws ExecutionException {
    // Given
    final FileAccessService instance = createInstance();
    final ArrayList<CommandLineSetup> actualCommandLineSetups = new ArrayList<CommandLineSetup>();

    myCtx.checking(new Expectations() {{
      oneOf(myRunnerParametersService).isRunningUnderWindows();
      will(returnValue(isRunningUnderWindows));

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
    return new FileAccessServiceImpl(myRunnerParametersService, myCommandLineExecutor);
  }
}
