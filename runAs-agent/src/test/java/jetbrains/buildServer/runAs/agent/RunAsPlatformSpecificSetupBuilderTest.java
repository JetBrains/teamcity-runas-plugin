package jetbrains.buildServer.runAs.agent;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import jetbrains.buildServer.agent.BuildAgentSystemInfo;
import jetbrains.buildServer.dotNet.buildRunner.agent.*;
import jetbrains.buildServer.runAs.common.Constants;
import jetbrains.buildServer.runAs.common.LoggingLevel;
import jetbrains.buildServer.runAs.common.WindowsIntegrityLevel;
import jetbrains.buildServer.util.Bitness;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
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
  private CommandLineArgumentsService myCommandLineArgumentsService;
  private UserCredentialsService myUserCredentialsService;
  private AccessControlResource myAccessControlResource;
  private FileAccessService myFileAccessService;
  private RunnerParametersService myRunnerParametersService;
  private AccessControlListProvider myAccessControlListProvider;
  private BuildAgentSystemInfo myBuildAgentSystemInfo;
  private RunAsAccessService myRunAsAccessService;

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
    myCommandLineArgumentsService = myCtx.mock(CommandLineArgumentsService.class);
    myFileAccessService = myCtx.mock(FileAccessService.class);
    myRunAsAccessService = myCtx.mock(RunAsAccessService.class);
  }

  @Test()
  public void shouldBuildSetup() throws IOException {
    // Given
    final File credentialsFile = new File("credentials");
    final File cmdFile = new File("command");
    final String toolName = "my tool";
    final String runAsToolPath = "runAsPath";
    final File runAsTool = new File(runAsToolPath, RunAsPlatformSpecificSetupBuilder.TOOL_FILE_NAME + ".abc");
    final AccessControlList runAsToolAcl = new AccessControlList(Arrays.asList(new AccessControlEntry(runAsTool, AccessControlAccount.forCurrent(), EnumSet.of(AccessPermissions.AllowExecute))));
    final String user = "nik";
    final String password = "abc";
    final List<CommandLineArgument> args = Arrays.asList(new CommandLineArgument("arg1", CommandLineArgument.Type.PARAMETER), new CommandLineArgument("arg2", CommandLineArgument.Type.PARAMETER));
    final List<CommandLineResource> resources = Arrays.asList(myCommandLineResource1, myCommandLineResource2);
    final String credentialsContent = "credentials content";
    final String cmdContent = "args content";
    final CommandLineSetup commandLineSetup = new CommandLineSetup(toolName, args, resources);
    final RunAsParams params = new RunAsParams("cmd line");
    final List<CommandLineArgument> additionalArgs = Arrays.asList(new CommandLineArgument("arg1", CommandLineArgument.Type.PARAMETER), new CommandLineArgument("arg 2", CommandLineArgument.Type.PARAMETER));
    final UserCredentials userCredentials = new UserCredentials(user, password, WindowsIntegrityLevel.Auto, LoggingLevel.Off, additionalArgs, new AccessControlList(Collections.<AccessControlEntry>emptyList()));
    final AccessControlEntry beforeBuildStepAce = new AccessControlEntry(new File("tools"), AccessControlAccount.forUser(user), EnumSet.of(AccessPermissions.AllowExecute));
    final AccessControlList beforeBuildStepAcl = new AccessControlList(Arrays.asList(beforeBuildStepAce));
    final CommandLineSetup runAsCommandLineSetup = new CommandLineSetup(
      runAsTool.getAbsolutePath(),
      Arrays.asList(
        new CommandLineArgument(credentialsFile.getAbsolutePath(), CommandLineArgument.Type.PARAMETER),
        new CommandLineArgument(cmdFile.getAbsolutePath(), CommandLineArgument.Type.PARAMETER),
        new CommandLineArgument("64", CommandLineArgument.Type.PARAMETER),
        new CommandLineArgument(password, CommandLineArgument.Type.PARAMETER)),
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

      oneOf(myAccessControlListProvider).getBeforeBuildStepAcl(userCredentials);
      will(returnValue(beforeBuildStepAcl));

      oneOf(myFileService).getTempFileName(RunAsPlatformSpecificSetupBuilder.ARGS_EXT);
      will(returnValue(credentialsFile));

      oneOf(myBuildAgentSystemInfo).bitness();
      will(returnValue(Bitness.BIT64));

      oneOf(myFileService).getTempFileName(".abc");
      will(returnValue(cmdFile));

      oneOf(myCredentialsGenerator).create(with(userCredentials));
      will(returnValue(credentialsContent));

      //noinspection unchecked
      oneOf(myCommandLineArgumentsService).createCommandLineString(with(any(List.class)));
      will(returnValue("cmd line"));

      oneOf(myArgsGenerator).create(params);
      will(returnValue(cmdContent));

      oneOf(myRunnerParametersService).getToolPath(Constants.RUN_AS_TOOL_NAME);
      will(returnValue(runAsToolPath));

      oneOf(myFileService).validatePath(runAsTool);
      oneOf(myFileAccessService).setAccess(runAsToolAcl);

      oneOf(myAccessControlResource).addEntry(new AccessControlEntry(cmdFile, AccessControlAccount.forUser(user), EnumSet.of(AccessPermissions.AllowExecute)));
      oneOf(myAccessControlResource).addEntry(beforeBuildStepAce);

      oneOf(myRunAsLogger).LogRunAs(runAsCommandLineSetup);
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
    final UserCredentials userCredentials = new UserCredentials(user, password, WindowsIntegrityLevel.Auto, LoggingLevel.Off, additionalArgs, new AccessControlList(Collections.<AccessControlEntry>emptyList()));
    final String toolName = "tool";
    final List<CommandLineArgument> args = Arrays.asList(new CommandLineArgument("arg1", CommandLineArgument.Type.PARAMETER), new CommandLineArgument("arg2", CommandLineArgument.Type.PARAMETER));
    final List<CommandLineResource> resources = Arrays.asList(myCommandLineResource1, myCommandLineResource2);
    final CommandLineSetup baseSetup = new CommandLineSetup(toolName, args, resources);
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
      myCommandLineArgumentsService,
      myFileAccessService,
      myRunAsLogger,
      myRunAsAccessService,
      ".abc");
  }
}