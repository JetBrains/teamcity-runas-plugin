package jetbrains.buildServer.runAs.agent;

import com.intellij.execution.ExecutionException;
import java.io.File;
import java.util.*;
import java.util.Arrays;
import java.util.Collections;
import jetbrains.buildServer.ExecResult;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineArgument;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineResource;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineSetup;
import jetbrains.buildServer.util.*;
import jetbrains.buildServer.util.Converter;
import org.assertj.core.util.*;
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
        0,
        null,
        Arrays.asList(
          new CommandLineSetup(CHMOD_TOOL_NAME, Arrays.asList(new CommandLineArgument("-R", CommandLineArgument.Type.PARAMETER), new CommandLineArgument("a+rXwx", CommandLineArgument.Type.PARAMETER), new CommandLineArgument(new File("my_file").getAbsolutePath(), CommandLineArgument.Type.PARAMETER)), Collections.<CommandLineResource>emptyList())),
        Arrays.asList(true)},

      // grant returns non zero exit code
      {
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("my_file"), AccessControlAccount.forUser(""), EnumSet.of(AccessPermissions.GrantRead, AccessPermissions.GrantWrite, AccessPermissions.GrantExecute, AccessPermissions.Recursive), AccessControlScope.Step))),
        1,
        null,
        Arrays.asList(
          new CommandLineSetup(CHMOD_TOOL_NAME, Arrays.asList(new CommandLineArgument("-R", CommandLineArgument.Type.PARAMETER), new CommandLineArgument("a+rXwx", CommandLineArgument.Type.PARAMETER), new CommandLineArgument(new File("my_file").getAbsolutePath(), CommandLineArgument.Type.PARAMETER)), Collections.<CommandLineResource>emptyList())),
        Arrays.asList(false)},

      // grant throws an exception
      {
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("my_file"), AccessControlAccount.forUser(""), EnumSet.of(AccessPermissions.GrantRead, AccessPermissions.GrantWrite, AccessPermissions.GrantExecute, AccessPermissions.Recursive), AccessControlScope.Step))),
        0,
        new ExecutionException("some error"),
        Arrays.asList(),
        Arrays.asList((Object)null)},

      // deny
      {
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("my_file"), AccessControlAccount.forUser(""), EnumSet.of(AccessPermissions.DenyRead, AccessPermissions.DenyWrite, AccessPermissions.DenyExecute, AccessPermissions.Recursive), AccessControlScope.Step))),
        0,
        null,
        Arrays.asList(
          new CommandLineSetup(CHMOD_TOOL_NAME, Arrays.asList(new CommandLineArgument("-R", CommandLineArgument.Type.PARAMETER), new CommandLineArgument("go-rwx", CommandLineArgument.Type.PARAMETER), new CommandLineArgument(new File("my_file").getAbsolutePath(), CommandLineArgument.Type.PARAMETER)), Collections.<CommandLineResource>emptyList())),
        Arrays.asList(true)},

      // grant & deny
      {
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("my_file"), AccessControlAccount.forUser(""), EnumSet.of(AccessPermissions.GrantRead, AccessPermissions.GrantWrite, AccessPermissions.DenyWrite, AccessPermissions.GrantExecute, AccessPermissions.Recursive), AccessControlScope.Step))),
        0,
        null,
        Arrays.asList(
          new CommandLineSetup(CHMOD_TOOL_NAME, Arrays.asList(new CommandLineArgument("-R", CommandLineArgument.Type.PARAMETER), new CommandLineArgument("a+rXwx", CommandLineArgument.Type.PARAMETER), new CommandLineArgument(new File("my_file").getAbsolutePath(), CommandLineArgument.Type.PARAMETER)), Collections.<CommandLineResource>emptyList()),
          new CommandLineSetup(CHMOD_TOOL_NAME, Arrays.asList(new CommandLineArgument("-R", CommandLineArgument.Type.PARAMETER), new CommandLineArgument("go-w", CommandLineArgument.Type.PARAMETER), new CommandLineArgument(new File("my_file").getAbsolutePath(), CommandLineArgument.Type.PARAMETER)), Collections.<CommandLineResource>emptyList())),
        Arrays.asList(true, true)},

      // grant & deny all
      {
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("my_file"), AccessControlAccount.forAll(), EnumSet.of(AccessPermissions.GrantRead, AccessPermissions.GrantWrite, AccessPermissions.DenyWrite, AccessPermissions.GrantExecute, AccessPermissions.Recursive), AccessControlScope.Step))),
        0,
        null,
        Arrays.asList(
          new CommandLineSetup(CHMOD_TOOL_NAME, Arrays.asList(new CommandLineArgument("-R", CommandLineArgument.Type.PARAMETER), new CommandLineArgument("a+rXwx", CommandLineArgument.Type.PARAMETER), new CommandLineArgument(new File("my_file").getAbsolutePath(), CommandLineArgument.Type.PARAMETER)), Collections.<CommandLineResource>emptyList()),
          new CommandLineSetup(CHMOD_TOOL_NAME, Arrays.asList(new CommandLineArgument("-R", CommandLineArgument.Type.PARAMETER), new CommandLineArgument("a-w", CommandLineArgument.Type.PARAMETER), new CommandLineArgument(new File("my_file").getAbsolutePath(), CommandLineArgument.Type.PARAMETER)), Collections.<CommandLineResource>emptyList())),
        Arrays.asList(true, true)},

      // grant & deny
      {
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("my_file"), AccessControlAccount.forUser(""), EnumSet.of(AccessPermissions.GrantRead, AccessPermissions.GrantWrite, AccessPermissions.GrantExecute, AccessPermissions.Recursive), AccessControlScope.Step),
          new AccessControlEntry(new File("my_file2"), AccessControlAccount.forAll(), EnumSet.of(AccessPermissions.GrantExecute), AccessControlScope.Step))),
        0,
        null,
        Arrays.asList(
          new CommandLineSetup(CHMOD_TOOL_NAME, Arrays.asList(new CommandLineArgument("-R", CommandLineArgument.Type.PARAMETER), new CommandLineArgument("a+rXwx", CommandLineArgument.Type.PARAMETER), new CommandLineArgument(new File("my_file").getAbsolutePath(), CommandLineArgument.Type.PARAMETER)), Collections.<CommandLineResource>emptyList()),
          new CommandLineSetup(CHMOD_TOOL_NAME, Arrays.asList(new CommandLineArgument("a+x", CommandLineArgument.Type.PARAMETER), new CommandLineArgument(new File("my_file2").getAbsolutePath(), CommandLineArgument.Type.PARAMETER)), Collections.<CommandLineResource>emptyList())),
        Arrays.asList(true, true)},

      // grant & deny returns non zero exit code
      {
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("my_file"), AccessControlAccount.forUser(""), EnumSet.of(AccessPermissions.GrantRead, AccessPermissions.GrantWrite, AccessPermissions.GrantExecute, AccessPermissions.Recursive), AccessControlScope.Step),
          new AccessControlEntry(new File("my_file2"), AccessControlAccount.forAll(), EnumSet.of(AccessPermissions.GrantExecute), AccessControlScope.Step))),
        -1,
        null,
        Arrays.asList(
          new CommandLineSetup(CHMOD_TOOL_NAME, Arrays.asList(new CommandLineArgument("-R", CommandLineArgument.Type.PARAMETER), new CommandLineArgument("a+rXwx", CommandLineArgument.Type.PARAMETER), new CommandLineArgument(new File("my_file").getAbsolutePath(), CommandLineArgument.Type.PARAMETER)), Collections.<CommandLineResource>emptyList()),
          new CommandLineSetup(CHMOD_TOOL_NAME, Arrays.asList(new CommandLineArgument("a+x", CommandLineArgument.Type.PARAMETER), new CommandLineArgument(new File("my_file2").getAbsolutePath(), CommandLineArgument.Type.PARAMETER)), Collections.<CommandLineResource>emptyList())),
        Arrays.asList(false, false)},

      // empty
      {
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("my_file"), AccessControlAccount.forAll(), EnumSet.of(AccessPermissions.Recursive), AccessControlScope.Step))),
        0,
        null,
        Arrays.asList(),
        Arrays.asList()},

      // empty
      {
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("my_file"), AccessControlAccount.forAll(), EnumSet.noneOf(AccessPermissions.class), AccessControlScope.Step))),
        0,
        null,
        Arrays.asList(),
        Arrays.asList()},
    };
  }

  @Test(dataProvider = "getSetPermissionsForLinuxCases")
  public void shouldSetPermissionsForLinux(
    @NotNull final AccessControlList accessControlList,
    final int exitCode,
    @Nullable final Exception error,
    @Nullable final List<CommandLineSetup> expectedCommandLineSetups,
    @Nullable final List<Object> expectedResult) throws ExecutionException {
    // Given
    final FileAccessService instance = createInstance();
    final ArrayList<CommandLineSetup> actualCommandLineSetups = new ArrayList<CommandLineSetup>();

    myCtx.checking(new Expectations() {{
      allowing(myCommandLineExecutor).runProcess(with(any(CommandLineSetup.class)), with(any(int.class)));
      if(error != null) {
        will(throwException(error));
      }
      else {
        will(new CustomAction("runProcess") {
          @Override
          public Object invoke(final Invocation invocation) throws Throwable {
            actualCommandLineSetups.add((CommandLineSetup)invocation.getParameter(0));
            final ExecResult execResult = new ExecResult();
            execResult.setExitCode(exitCode);
            return execResult;
          }
        });
      }
    }});

    // When
    final Iterable<Result<AccessControlEntry, Boolean>> result = instance.setAccess(accessControlList);
    final List<Object> actualResult = CollectionsUtil.convertCollection(
      Lists.newArrayList(result), new Converter<Object, Result<AccessControlEntry, Boolean>>() {
        @Override
        public Object createFrom(@NotNull final Result<AccessControlEntry, Boolean> aceResult) {
          if(aceResult.isSuccessful()) {
            return aceResult.getValue();
          }

          return null;
        }
      });

    // Then
    myCtx.assertIsSatisfied();
    then(actualCommandLineSetups).isEqualTo(expectedCommandLineSetups);
    then(actualResult).isEqualTo(expectedResult);
  }

  @NotNull
  private FileAccessService createInstance()
  {
    return new LinuxFileAccessService(
      myCommandLineExecutor);
  }
}
