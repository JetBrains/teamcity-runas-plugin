package jetbrains.buildServer.runAs.agent;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import jetbrains.buildServer.dotNet.buildRunner.agent.*;
import jetbrains.buildServer.runAs.common.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.assertj.core.api.BDDAssertions.then;

public class RunAsSetupBuilderTest {
  private Mockery myCtx;
  private FileService myFileService;
  private RunnerParametersService myRunnerParametersService;
  private ResourcePublisher myResourcePublisher;
  private ResourceGenerator<CredentialsSettings> myCredentialsGenerator;
  private CommandLineResource myCommandLineResource1;
  private CommandLineResource myCommandLineResource2;
  private ResourceGenerator<RunAsCmdSettings> myArgsGenerator;
  private CommandLineArgumentsService myCommandLineArgumentsService;
  private BuildFeatureParametersService myBuildFeatureParametersService;

  @BeforeMethod
  public void setUp()
  {
    myCtx = new Mockery();
    myRunnerParametersService = myCtx.mock(RunnerParametersService.class);
    myBuildFeatureParametersService = myCtx.mock(BuildFeatureParametersService.class);
    myFileService = myCtx.mock(FileService.class);
    myResourcePublisher = myCtx.mock(ResourcePublisher.class);
    //noinspection unchecked
    myCredentialsGenerator = (ResourceGenerator<CredentialsSettings>)myCtx.mock(ResourceGenerator.class, "CredentialsGenerator");
    //noinspection unchecked
    myArgsGenerator = (ResourceGenerator<RunAsCmdSettings>)myCtx.mock(ResourceGenerator.class, "ArgsGenerator");
    //noinspection unchecked
    myCommandLineResource1 = myCtx.mock(CommandLineResource.class, "Res1");
    myCommandLineResource2 = myCtx.mock(CommandLineResource.class, "Res2");
    myCommandLineArgumentsService = myCtx.mock(CommandLineArgumentsService.class);
  }

  @Test()
  public void shouldBuildSetup() throws IOException {
    // Given
    final File checkoutDir = new File("checkoutDir");
    final File credentialsFile = new File("credentials");
    final File cmdFile = new File("command");
    final String toolName = "my tool";
    final String runAsToolPath = "runAsPath";
    final File runAsTool = new File(runAsToolPath, RunAsSetupBuilder.TOOL_FILE_NAME);
    final String user = "nik";
    final String password = "abc";
    final List<CommandLineArgument> args = Arrays.asList(new CommandLineArgument("arg1", CommandLineArgument.Type.PARAMETER), new CommandLineArgument("arg2", CommandLineArgument.Type.PARAMETER));
    final List<CommandLineResource> resources = Arrays.asList(myCommandLineResource1, myCommandLineResource2);
    final String credentialsContent = "credentials content";
    final String cmdContent = "args content";
    final CommandLineSetup commandLineSetup = new CommandLineSetup(toolName, args, resources);
    final RunAsCmdSettings runAsCmdSettings = new RunAsCmdSettings("cmd line", checkoutDir.getAbsolutePath());
    myCtx.checking(new Expectations() {{
      oneOf(myRunnerParametersService).isRunningUnderWindows();
      will(returnValue(true));

      oneOf(myBuildFeatureParametersService).getBuildFeatureParameters(Constants.BUILD_FEATURE_TYPE, Constants.USER_VAR);
      will(returnValue(Arrays.asList(user, "bbb")));

      oneOf(myBuildFeatureParametersService).getBuildFeatureParameters(Constants.BUILD_FEATURE_TYPE, Constants.PASSWORD_VAR);
      will(returnValue(Arrays.asList(password, "aaa")));

      oneOf(myFileService).getTempFileName(RunAsSetupBuilder.CREDENTIALS_EXT);
      will(returnValue(credentialsFile));

      oneOf(myFileService).getTempFileName(RunAsSetupBuilder.CMD_EXT);
      will(returnValue(cmdFile));

      oneOf(myCredentialsGenerator).create(with(new CredentialsSettings(user, password)));
      will(returnValue(credentialsContent));

      //noinspection unchecked
      oneOf(myCommandLineArgumentsService).createCommandLineString(with(any(List.class)));
      will(returnValue("cmd line"));

      oneOf(myFileService).getCheckoutDirectory();
      will(returnValue(checkoutDir));

      oneOf(myArgsGenerator).create(runAsCmdSettings);
      will(returnValue(cmdContent));

      oneOf(myRunnerParametersService).getToolPath(Constants.RUN_AS_TOOL_NAME);
      will(returnValue(runAsToolPath));

      oneOf(myFileService).validatePath(runAsTool);
    }});

    final CommandLineSetupBuilder instance = createInstance();

    // When
    final CommandLineSetup setup = instance.build(commandLineSetup).iterator().next();

    // Then
    myCtx.assertIsSatisfied();
    then(setup.getToolPath()).isEqualTo(runAsTool.getAbsolutePath());

    then(setup.getResources()).containsExactly(
      myCommandLineResource1,
      myCommandLineResource2,
      new CommandLineFile(myResourcePublisher, credentialsFile.getAbsoluteFile(), credentialsContent),
      new CommandLineFile(myResourcePublisher, cmdFile.getAbsoluteFile(), cmdContent));

    then(setup.getArgs()).containsExactly(
      new CommandLineArgument(credentialsFile.getAbsolutePath(), CommandLineArgument.Type.PARAMETER),
      new CommandLineArgument(cmdFile.getAbsolutePath(), CommandLineArgument.Type.PARAMETER));
  }

  @DataProvider(name = "emptyUserNameCases")
  public Object[][] getEmptyUserNameCases() {
    return new Object[][] {
      { "  " }, { "" }, { null },
    };
  }

  @Test(dataProvider = "emptyUserNameCases")
  public void shouldNotBuildSetupWhenHasEmptyUserName(@Nullable final String userName) throws IOException {
    // Given
    final String toolName = "my tool";
    final String password = "abc";
    final List<CommandLineArgument> args = Arrays.asList(new CommandLineArgument("arg1", CommandLineArgument.Type.PARAMETER), new CommandLineArgument("arg2", CommandLineArgument.Type.PARAMETER));
    final List<CommandLineResource> resources = Arrays.asList(myCommandLineResource1, myCommandLineResource2);
    final CommandLineSetup baseSetup = new CommandLineSetup(toolName, args, resources);
    myCtx.checking(new Expectations() {{
      oneOf(myRunnerParametersService).isRunningUnderWindows();
      will(returnValue(true));

      oneOf(myBuildFeatureParametersService).getBuildFeatureParameters(Constants.BUILD_FEATURE_TYPE, Constants.USER_VAR);
      will(returnValue(Arrays.asList(userName)));

      oneOf(myBuildFeatureParametersService).getBuildFeatureParameters(Constants.BUILD_FEATURE_TYPE, Constants.PASSWORD_VAR);
      will(returnValue(Arrays.asList(password)));
    }});

    final CommandLineSetupBuilder instance = createInstance();

    // When
    final CommandLineSetup setup = instance.build(baseSetup).iterator().next();

    // Then
    myCtx.assertIsSatisfied();
    then(setup).isEqualTo(baseSetup);
  }

  @Test()
  public void shouldNotBuildSetupWhenBuildFeatureWasNotFound() throws IOException {
    // Given
    final String toolName = "my tool";
    final List<CommandLineArgument> args = Arrays.asList(new CommandLineArgument("arg1", CommandLineArgument.Type.PARAMETER), new CommandLineArgument("arg2", CommandLineArgument.Type.PARAMETER));
    final List<CommandLineResource> resources = Arrays.asList(myCommandLineResource1, myCommandLineResource2);
    final CommandLineSetup baseSetup = new CommandLineSetup(toolName, args, resources);
    myCtx.checking(new Expectations() {{
      oneOf(myRunnerParametersService).isRunningUnderWindows();
      will(returnValue(true));

      oneOf(myBuildFeatureParametersService).getBuildFeatureParameters(Constants.BUILD_FEATURE_TYPE, Constants.USER_VAR);
      will(returnValue(Arrays.asList()));

      oneOf(myBuildFeatureParametersService).getBuildFeatureParameters(Constants.BUILD_FEATURE_TYPE, Constants.PASSWORD_VAR);
      will(returnValue(Arrays.asList()));
    }});

    final CommandLineSetupBuilder instance = createInstance();

    // When
    final CommandLineSetup setup = instance.build(baseSetup).iterator().next();

    // Then
    myCtx.assertIsSatisfied();
    then(setup).isEqualTo(baseSetup);
  }

  @Test()
  public void shouldNotBuildSetupWhenIsNotWindows() throws IOException {
    // Given
    final String toolName = "tool";
    final List<CommandLineArgument> args = Arrays.asList(new CommandLineArgument("arg1", CommandLineArgument.Type.PARAMETER), new CommandLineArgument("arg2", CommandLineArgument.Type.PARAMETER));
    final List<CommandLineResource> resources = Arrays.asList(myCommandLineResource1, myCommandLineResource2);
    final CommandLineSetup baseSetup = new CommandLineSetup(toolName, args, resources);
    myCtx.checking(new Expectations() {{
      oneOf(myRunnerParametersService).isRunningUnderWindows();
      will(returnValue(false));
    }});

    final CommandLineSetupBuilder instance = createInstance();

    // When
    final CommandLineSetup setup = instance.build(new CommandLineSetup(toolName, args, resources)).iterator().next();

    // Then
    myCtx.assertIsSatisfied();
    then(setup).isEqualTo(baseSetup);
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
    return new RunAsSetupBuilder(
      myRunnerParametersService,
      myBuildFeatureParametersService,
      myFileService,
      myResourcePublisher,
      myCredentialsGenerator,
      myArgsGenerator,
      myCommandLineArgumentsService);
  }
}
