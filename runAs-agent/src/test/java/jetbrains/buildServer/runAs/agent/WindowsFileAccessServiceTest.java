/*
 * Copyright 2000-2020 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

import static jetbrains.buildServer.runAs.agent.Constants.ICACLS_TOOL_NAME;
import static org.assertj.core.api.BDDAssertions.then;

public class WindowsFileAccessServiceTest {
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
          new AccessControlEntry(new File("my_file"), AccessControlAccount.forUser("user1"), EnumSet.of(AccessPermissions.GrantRead, AccessPermissions.GrantWrite, AccessPermissions.GrantExecute), AccessControlScope.Step))),
        0,
        null,
        Arrays.asList(
          new CommandLineSetup(ICACLS_TOOL_NAME, Arrays.asList(
            new CommandLineArgument(new File("my_file").getAbsolutePath(), CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("/C", CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("/Q", CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("/grant", CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("user1:(R,W,D,DC,RX)", CommandLineArgument.Type.PARAMETER)), Collections.<CommandLineResource>emptyList())),
        Arrays.asList(true)},

      // full access returns non zero exit code
      {
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("my_file"), AccessControlAccount.forUser("user1"), EnumSet.of(AccessPermissions.GrantRead, AccessPermissions.GrantWrite, AccessPermissions.GrantExecute), AccessControlScope.Step))),
        1,
        null,
        Arrays.asList(
          new CommandLineSetup(ICACLS_TOOL_NAME, Arrays.asList(
            new CommandLineArgument(new File("my_file").getAbsolutePath(), CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("/C", CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("/Q", CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("/grant", CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("user1:(R,W,D,DC,RX)", CommandLineArgument.Type.PARAMETER)), Collections.<CommandLineResource>emptyList())),
        Arrays.asList(false)},

      // full access throws an exception
      {
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("my_file"), AccessControlAccount.forUser("user1"), EnumSet.of(AccessPermissions.GrantRead, AccessPermissions.GrantWrite, AccessPermissions.GrantExecute), AccessControlScope.Step))),
        0,
        new ExecutionException("some error"),
        Arrays.asList(),
        Arrays.asList((Object)null)},

      // deny full access
      {
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("my_file"), AccessControlAccount.forUser("user1"), EnumSet.of(AccessPermissions.DenyRead, AccessPermissions.DenyWrite, AccessPermissions.DenyExecute), AccessControlScope.Step))),
        0,
        null,
        Arrays.asList(
          new CommandLineSetup(ICACLS_TOOL_NAME, Arrays.asList(
            new CommandLineArgument(new File("my_file").getAbsolutePath(), CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("/C", CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("/Q", CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("/deny", CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("user1:(R,W,D,DC,X)", CommandLineArgument.Type.PARAMETER)), Collections.<CommandLineResource>emptyList())),
        Arrays.asList(true)},

      // grant & deny access
      {
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("my_file"), AccessControlAccount.forUser("user1"), EnumSet.of(AccessPermissions.GrantRead, AccessPermissions.GrantWrite, AccessPermissions.DenyWrite, AccessPermissions.GrantExecute), AccessControlScope.Step))),
        0,
        null,
        Arrays.asList(
          new CommandLineSetup(ICACLS_TOOL_NAME, Arrays.asList(
            new CommandLineArgument(new File("my_file").getAbsolutePath(), CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("/C", CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("/Q", CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("/grant", CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("user1:(R,W,D,DC,RX)", CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("/deny", CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("user1:(W,D,DC)", CommandLineArgument.Type.PARAMETER)), Collections.<CommandLineResource>emptyList())),
        Arrays.asList(true)},

      // full access recursive
      {
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("my_file"), AccessControlAccount.forUser("user1"), EnumSet.of(AccessPermissions.Recursive, AccessPermissions.GrantRead, AccessPermissions.GrantWrite, AccessPermissions.GrantExecute), AccessControlScope.Step))),
        0,
        null,
        Arrays.asList(
          new CommandLineSetup(ICACLS_TOOL_NAME, Arrays.asList(
            new CommandLineArgument(new File("my_file").getAbsolutePath(), CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("/C", CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("/Q", CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("/grant", CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("user1:(OI)(CI)(R,W,D,DC,RX)", CommandLineArgument.Type.PARAMETER)), Collections.<CommandLineResource>emptyList())),
        Arrays.asList(true)},

      // read access
      {
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("my_file"), AccessControlAccount.forUser("user1"), EnumSet.of(AccessPermissions.GrantRead), AccessControlScope.Step))),
        0,
        null,
        Arrays.asList(
          new CommandLineSetup(ICACLS_TOOL_NAME, Arrays.asList(
            new CommandLineArgument(new File("my_file").getAbsolutePath(), CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("/C", CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("/Q", CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("/grant", CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("user1:(R)", CommandLineArgument.Type.PARAMETER)), Collections.<CommandLineResource>emptyList())),
        Arrays.asList(true)},

      // write access
      {
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("my_file"), AccessControlAccount.forUser("user1"), EnumSet.of(AccessPermissions.GrantWrite), AccessControlScope.Step))),
        0,
        null,
        Arrays.asList(
          new CommandLineSetup(ICACLS_TOOL_NAME, Arrays.asList(
            new CommandLineArgument(new File("my_file").getAbsolutePath(), CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("/C", CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("/Q", CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("/grant", CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("user1:(W,D,DC)", CommandLineArgument.Type.PARAMETER)), Collections.<CommandLineResource>emptyList())),
        Arrays.asList(true)},

      // read/write access
      {
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("my_file"), AccessControlAccount.forUser("user1"), EnumSet.of(AccessPermissions.GrantRead, AccessPermissions.GrantWrite), AccessControlScope.Step))),
        0,
        null,
        Arrays.asList(
          new CommandLineSetup(ICACLS_TOOL_NAME, Arrays.asList(
            new CommandLineArgument(new File("my_file").getAbsolutePath(), CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("/C", CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("/Q", CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("/grant", CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("user1:(R,W,D,DC)", CommandLineArgument.Type.PARAMETER)), Collections.<CommandLineResource>emptyList())),
        Arrays.asList(true)},

      // replace by read/write access
      {
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("my_file"), AccessControlAccount.forUser("user1"), EnumSet.of(AccessPermissions.Recursive, AccessPermissions.GrantRead, AccessPermissions.GrantWrite), AccessControlScope.Step))),
        0,
        null,
        Arrays.asList(
          new CommandLineSetup(ICACLS_TOOL_NAME, Arrays.asList(
            new CommandLineArgument(new File("my_file").getAbsolutePath(), CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("/C", CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("/Q", CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("/grant", CommandLineArgument.Type.PARAMETER),
            new CommandLineArgument("user1:(OI)(CI)(R,W,D,DC)", CommandLineArgument.Type.PARAMETER)), Collections.<CommandLineResource>emptyList())),
        Arrays.asList(true)},
    };
  }

  @Test(dataProvider = "getSetPermissionsForWindowsCases")
  public void shouldSetPermissionsForWindows(
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
    return new WindowsFileAccessService(
      myCommandLineExecutor);
  }
}
