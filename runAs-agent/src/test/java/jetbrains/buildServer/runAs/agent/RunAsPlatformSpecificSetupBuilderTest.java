package jetbrains.buildServer.runAs.agent;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import jetbrains.buildServer.dotNet.buildRunner.agent.*;
import jetbrains.buildServer.runAs.common.Constants;
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
  private RunnerParametersService myRunnerParametersService;
  private ResourcePublisher myBeforeBuildPublisher;
  private ResourceGenerator<Settings> myCredentialsGenerator;
  private CommandLineResource myCommandLineResource1;
  private CommandLineResource myCommandLineResource2;
  private ResourceGenerator<RunAsCmdSettings> myArgsGenerator;
  private CommandLineArgumentsService myCommandLineArgumentsService;
  private SettingsProvider mySettingsProvider;
  private ResourcePublisher myExecutableFilePublisher;
  private FileAccessService myFileAccessService;

  @BeforeMethod
  public void setUp()
  {
    myCtx = new Mockery();
    mySettingsProvider = myCtx.mock(SettingsProvider.class);
    myRunnerParametersService = myCtx.mock(RunnerParametersService.class);
    myFileService = myCtx.mock(FileService.class);
    myBeforeBuildPublisher = myCtx.mock(ResourcePublisher.class, "BeforeBuildPublisher");
    myExecutableFilePublisher = myCtx.mock(ResourcePublisher.class, "ExecutableFilePublisher");
    //noinspection unchecked
    myCredentialsGenerator = (ResourceGenerator<Settings>)myCtx.mock(ResourceGenerator.class, "WindowsSettingsGenerator");
    //noinspection unchecked
    myArgsGenerator = (ResourceGenerator<RunAsCmdSettings>)myCtx.mock(ResourceGenerator.class, "ArgsGenerator");
    //noinspection unchecked
    myCommandLineResource1 = myCtx.mock(CommandLineResource.class, "Res1");
    myCommandLineResource2 = myCtx.mock(CommandLineResource.class, "Res2");
    myCommandLineArgumentsService = myCtx.mock(CommandLineArgumentsService.class);
    myFileAccessService = myCtx.mock(FileAccessService.class);
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
    final List<CommandLineArgument> args = Arrays.asList(new CommandLineArgument("arg1", CommandLineArgument.Type.PARAMETER), new CommandLineArgument("arg2", CommandLineArgument.Type.PARAMETER));
    final List<CommandLineResource> resources = Arrays.asList(myCommandLineResource1, myCommandLineResource2);
    final String credentialsContent = "credentials content";
    final String cmdContent = "args content";
    final CommandLineSetup commandLineSetup = new CommandLineSetup(toolName, args, resources);
    final RunAsCmdSettings runAsCmdSettings = new RunAsCmdSettings("cmd line");
    final List<CommandLineArgument> additionalArgs = Arrays.asList(new CommandLineArgument("arg1", CommandLineArgument.Type.PARAMETER), new CommandLineArgument("arg 2", CommandLineArgument.Type.PARAMETER));
    final Settings settings = new Settings(user, password, additionalArgs);
    myCtx.checking(new Expectations() {{
      oneOf(mySettingsProvider).tryGetSettings();
      will(returnValue(settings));

      oneOf(myFileService).getTempFileName(RunAsPlatformSpecificSetupBuilder.ARGS_EXT);
      will(returnValue(credentialsFile));

      oneOf(myFileService).getTempFileName(".abc");
      will(returnValue(cmdFile));

      oneOf(myCredentialsGenerator).create(with(settings));
      will(returnValue(credentialsContent));

      //noinspection unchecked
      oneOf(myCommandLineArgumentsService).createCommandLineString(with(any(List.class)));
      will(returnValue("cmd line"));

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
      new CommandLineFile(myBeforeBuildPublisher, credentialsFile.getAbsoluteFile(), credentialsContent),
      new CommandLineFile(myBeforeBuildPublisher, cmdFile.getAbsoluteFile(), cmdContent));

    then(setup.getArgs()).containsExactly(
      new CommandLineArgument(credentialsFile.getAbsolutePath(), CommandLineArgument.Type.PARAMETER),
      new CommandLineArgument(cmdFile.getAbsolutePath(), CommandLineArgument.Type.PARAMETER),
      new CommandLineArgument(password, CommandLineArgument.Type.PARAMETER));
  }

  @Test()
  public void shouldNotBuildSetupWhenHaveNoSettings() throws IOException {
    // Given
    final String toolName = "tool";
    final List<CommandLineArgument> args = Arrays.asList(new CommandLineArgument("arg1", CommandLineArgument.Type.PARAMETER), new CommandLineArgument("arg2", CommandLineArgument.Type.PARAMETER));
    final List<CommandLineResource> resources = Arrays.asList(myCommandLineResource1, myCommandLineResource2);
    final CommandLineSetup baseSetup = new CommandLineSetup(toolName, args, resources);
    myCtx.checking(new Expectations() {{
      oneOf(mySettingsProvider).tryGetSettings();
      will(returnValue(null));
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
    return new RunAsPlatformSpecificSetupBuilder(
      mySettingsProvider,
      myRunnerParametersService,
      myFileService,
      myBeforeBuildPublisher,
      myExecutableFilePublisher,
      myCredentialsGenerator,
      myArgsGenerator,
      myCommandLineArgumentsService,
      myFileAccessService,
      ".abc");
  }
}