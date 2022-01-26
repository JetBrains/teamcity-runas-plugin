/*
 * Copyright 2000-2022 JetBrains s.r.o.
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

import java.io.File;
import java.io.IOException;
import java.util.*;
import jetbrains.buildServer.agent.BuildAgentSystemInfo;
import jetbrains.buildServer.dotNet.buildRunner.agent.*;
import jetbrains.buildServer.runAs.common.Constants;
import jetbrains.buildServer.runAs.common.LoggingLevel;
import jetbrains.buildServer.runAs.common.WindowsIntegrityLevel;
import jetbrains.buildServer.util.Bitness;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.assertj.core.api.BDDAssertions.then;

public class RunAsPlatformSpecificSetupBuilderTest {
  private Mockery myCtx;
  private FileService myFileService;
  private RunAsLogger myRunAsLogger;
  private ResourcePublisher myBeforeBuildPublisher;
  private ResourceGenerator<UserCredentials> myCredentialsGenerator;
  private CommandLineResource myCommandLineResource1;
  private CommandLineResource myCommandLineResource2;
  private ResourceGenerator<RunAsParams> myArgsGenerator;
  private UserCredentialsService myUserCredentialsService;
  private AccessControlResource myAccessControlResource;
  private FileAccessService myFileAccessService;
  private RunnerParametersService myRunnerParametersService;
  private AccessControlListProvider myAccessControlListProvider;
  private BuildAgentSystemInfo myBuildAgentSystemInfo;
  private RunAsAccessService myRunAsAccessService;
  private Converter<String, String> myArgumentConverter;

  @BeforeMethod
  public void setUp()
  {
    myCtx = new Mockery();
    myUserCredentialsService = myCtx.mock(UserCredentialsService.class);
    myRunAsLogger = myCtx.mock(RunAsLogger.class);
    myRunnerParametersService = myCtx.mock(RunnerParametersService.class);
    myFileService = myCtx.mock(FileService.class);
    myBuildAgentSystemInfo = myCtx.mock(BuildAgentSystemInfo.class);
    myAccessControlListProvider = myCtx.mock(AccessControlListProvider.class);
    myBeforeBuildPublisher = myCtx.mock(ResourcePublisher.class);
    myAccessControlResource = myCtx.mock(AccessControlResource.class);
    //noinspection unchecked
    myCredentialsGenerator = (ResourceGenerator<UserCredentials>)myCtx.mock(ResourceGenerator.class, "WindowsSettingsGenerator");
    //noinspection unchecked
    myArgsGenerator = (ResourceGenerator<RunAsParams>)myCtx.mock(ResourceGenerator.class, "ArgsGenerator");
    //noinspection unchecked
    myCommandLineResource1 = myCtx.mock(CommandLineResource.class, "Res1");
    myCommandLineResource2 = myCtx.mock(CommandLineResource.class, "Res2");
    myFileAccessService = myCtx.mock(FileAccessService.class);
    myRunAsAccessService = myCtx.mock(RunAsAccessService.class);
    //noinspection unchecked
    myArgumentConverter = (Converter<String, String>)myCtx.mock(Converter.class);
  }

  @Test()
  public void shouldBuildSetup() throws IOException {
    // Given
    final File credentialsFile = new File("credentials");
    final File cmdFile = new File("command");
    final String toolName = "my tool";
    final String runAsToolPath = "runAsPath";
    final File runAsTool = new File(runAsToolPath, RunAsPlatformSpecificSetupBuilder.TOOL_FILE_NAME + ".abc");
    final String user = "nik";
    final String password = "abc";
    final AccessControlEntry someAce = new AccessControlEntry(new File("tools"), AccessControlAccount.forAll(), EnumSet.of(AccessPermissions.GrantExecute), AccessControlScope.Step);
    final List<CommandLineArgument> args = Arrays.asList(new CommandLineArgument("arg1", CommandLineArgument.Type.PARAMETER), new CommandLineArgument("arg2", CommandLineArgument.Type.PARAMETER));
    final List<CommandLineResource> resources = Arrays.asList(myCommandLineResource1, myCommandLineResource2);
    final String credentialsContent = "credentials content";
    final String cmdContent = "args content";
    final CommandLineSetup commandLineSetup = new CommandLineSetup(toolName, args, resources);
    final List<CommandLineArgument> runAsParamsArgs = new ArrayList<CommandLineArgument>(args);
    runAsParamsArgs.add(0, new CommandLineArgument(toolName, CommandLineArgument.Type.PARAMETER));
    final RunAsParams params = new RunAsParams(runAsParamsArgs);
    final List<CommandLineArgument> additionalArgs = Arrays.asList(new CommandLineArgument("arg1", CommandLineArgument.Type.PARAMETER), new CommandLineArgument("arg 2", CommandLineArgument.Type.PARAMETER));
    final UserCredentials userCredentials = new UserCredentials("profile", user, password, WindowsIntegrityLevel.Auto, LoggingLevel.Off, additionalArgs);
    final AccessControlList stepAcl = new AccessControlList(Arrays.asList(someAce));
    final CommandLineSetup runAsCommandLineSetup = new CommandLineSetup(
      runAsTool.getAbsolutePath(),
      Arrays.asList(
        new CommandLineArgument(credentialsFile.getAbsolutePath(), CommandLineArgument.Type.PARAMETER),
        new CommandLineArgument(cmdFile.getAbsolutePath(), CommandLineArgument.Type.PARAMETER),
        new CommandLineArgument("64", CommandLineArgument.Type.PARAMETER),
        new CommandLineArgument("'" + password + "'", CommandLineArgument.Type.PARAMETER)),
      Arrays.asList(
        myCommandLineResource1,
        myCommandLineResource2,
        new CommandLineFile(myBeforeBuildPublisher, credentialsFile.getAbsoluteFile(), credentialsContent),
        new CommandLineFile(myBeforeBuildPublisher, cmdFile.getAbsoluteFile(), cmdContent),
        myAccessControlResource));

    myCtx.checking(new Expectations() {{
      oneOf(myRunAsAccessService).getIsRunAsEnabled();
      will(returnValue(true));

      oneOf(myUserCredentialsService).tryGetUserCredentials();
      will(returnValue(userCredentials));

      oneOf(myAccessControlListProvider).getAcl(userCredentials);
      will(returnValue(stepAcl));

      oneOf(myFileService).getTempFileName(RunAsPlatformSpecificSetupBuilder.ARGS_EXT);
      will(returnValue(credentialsFile));

      oneOf(myBuildAgentSystemInfo).bitness();
      will(returnValue(Bitness.BIT64));

      oneOf(myFileService).getTempFileName(".abc");
      will(returnValue(cmdFile));

      oneOf(myCredentialsGenerator).create(with(userCredentials));
      will(returnValue(credentialsContent));

      oneOf(myArgsGenerator).create(params);
      will(returnValue(cmdContent));

      oneOf(myRunnerParametersService).getToolPath(Constants.RUN_AS_TOOL_NAME);
      will(returnValue(runAsToolPath));

      oneOf(myFileService).validatePath(runAsTool);

      never(myFileAccessService).setAccess(with(any(AccessControlList.class)));

      oneOf(myAccessControlResource).setAcl(
        new AccessControlList(Arrays.asList(
          someAce,
          new AccessControlEntry(cmdFile, AccessControlAccount.forUser(user), EnumSet.of(AccessPermissions.GrantExecute), AccessControlScope.Step),
          new AccessControlEntry(runAsTool, AccessControlAccount.forUser(user), EnumSet.of(AccessPermissions.GrantExecute), AccessControlScope.Build))));

      oneOf(myRunAsLogger).LogRunAs(userCredentials, commandLineSetup, runAsCommandLineSetup);

      allowing(myArgumentConverter).convert(with(any(String.class)));
      will(new CustomAction("convert") {
        @Override
        public Object invoke(final Invocation invocation) throws Throwable {
          return "'" + invocation.getParameter(0) + "'";
        }
      });
    }});

    final CommandLineSetupBuilder instance = createInstance();

    // When
    final CommandLineSetup setup = instance.build(commandLineSetup).iterator().next();

    // Then

    myCtx.assertIsSatisfied();
    then(setup).isEqualTo(runAsCommandLineSetup);
  }

  @Test()
  public void shouldNotBuildSetupWhenHaveNoSettings() throws IOException {
    // Given
    final String toolName = "tool";
    final List<CommandLineArgument> args = Arrays.asList(new CommandLineArgument("arg1", CommandLineArgument.Type.PARAMETER), new CommandLineArgument("arg2", CommandLineArgument.Type.PARAMETER));
    final List<CommandLineResource> resources = Arrays.asList(myCommandLineResource1, myCommandLineResource2);
    final CommandLineSetup baseSetup = new CommandLineSetup(toolName, args, resources);
    myCtx.checking(new Expectations() {{
      oneOf(myUserCredentialsService).tryGetUserCredentials();
      will(returnValue(null));
    }});

    final CommandLineSetupBuilder instance = createInstance();

    // When
    final CommandLineSetup setup = instance.build(new CommandLineSetup(toolName, args, resources)).iterator().next();

    // Then
    myCtx.assertIsSatisfied();
    then(setup).isEqualTo(baseSetup);
  }

  @Test()
  public void shouldThrowBuildStartExceptionWhenRunAsIsNotEnabled() throws IOException {
    // Given
    final String user = "nik";
    final String password = "abc";
    final List<CommandLineArgument> additionalArgs = Arrays.asList(new CommandLineArgument("arg1", CommandLineArgument.Type.PARAMETER));
    final UserCredentials userCredentials = new UserCredentials("profile", user, password, WindowsIntegrityLevel.Auto, LoggingLevel.Off, additionalArgs);
    final String toolName = "tool";
    final List<CommandLineArgument> args = Arrays.asList(new CommandLineArgument("arg1", CommandLineArgument.Type.PARAMETER), new CommandLineArgument("arg2", CommandLineArgument.Type.PARAMETER));
    final List<CommandLineResource> resources = Arrays.asList(myCommandLineResource1, myCommandLineResource2);
    myCtx.checking(new Expectations() {{
      oneOf(myUserCredentialsService).tryGetUserCredentials();
      will(returnValue(userCredentials));

      oneOf(myRunAsAccessService).getIsRunAsEnabled();
      will(returnValue(false));
    }});

    final CommandLineSetupBuilder instance = createInstance();

    // When
    BuildStartException actualException = null;
    try {
      instance.build(new CommandLineSetup(toolName, args, resources)).iterator().next();
    }
    catch (BuildStartException ex) {
      actualException = ex;
    }

    // Then
    myCtx.assertIsSatisfied();
    then(actualException).isNotNull();
  }

  @DataProvider(name = "configurationParametersCases")
  public Object[][] getConfigurationParametersCases() {
    return new Object[][] {
      { null, null },
    };
  }

  @NotNull
  private CommandLineSetupBuilder createInstance()
  {
    return new RunAsPlatformSpecificSetupBuilder(
      myUserCredentialsService,
      myRunnerParametersService,
      myFileService,
      myBuildAgentSystemInfo,
      myAccessControlListProvider,
      myBeforeBuildPublisher,
      myAccessControlResource,
      myCredentialsGenerator,
      myArgsGenerator,
      myFileAccessService,
      myRunAsLogger,
      myRunAsAccessService,
      myArgumentConverter,
      ".abc");
  }
}