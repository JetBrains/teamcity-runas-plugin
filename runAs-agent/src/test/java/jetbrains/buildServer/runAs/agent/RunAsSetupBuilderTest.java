package jetbrains.buildServer.runAs.agent;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import jetbrains.buildServer.dotNet.buildRunner.agent.*;
import jetbrains.buildServer.messages.serviceMessages.Message;
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
  private ResourceGenerator<Settings> mySettingsGenerator;
  private LoggerService myLoggerService;
  private CommandLineResource myCommandLineResource1;
  private CommandLineResource myCommandLineResource2;

  @BeforeMethod
  public void setUp()
  {
    myCtx = new Mockery();
    myRunnerParametersService = myCtx.mock(RunnerParametersService.class);
    myFileService = myCtx.mock(FileService.class);
    myResourcePublisher = myCtx.mock(ResourcePublisher.class);
    //noinspection unchecked
    mySettingsGenerator = (ResourceGenerator<Settings>)myCtx.mock(ResourceGenerator.class);
    myLoggerService = myCtx.mock(LoggerService.class);
    myCommandLineResource1 = myCtx.mock(CommandLineResource.class, "Res1");
    myCommandLineResource2 = myCtx.mock(CommandLineResource.class, "Res2");
  }

  @Test()
  public void shouldBuildSetup() throws IOException {
    // Given
    final File checkoutDir = new File("checkoutDir");
    final File settingsFile = new File("my.settings");
    final String toolName = "tool";
    final String runAsToolPath = "runAsPath";
    final File runAsTool = new File(runAsToolPath, RunAsSetupBuilder.TOOL_FILE_NAME);
    final String user = "nik";
    final String password = "abc";
    final List<CommandLineArgument> args = Arrays.asList(new CommandLineArgument("arg1", CommandLineArgument.Type.PARAMETER), new CommandLineArgument("arg2", CommandLineArgument.Type.PARAMETER));
    final List<CommandLineResource> resources = Arrays.asList(myCommandLineResource1, myCommandLineResource2);
    final CommandLineSetup baseSetup = new CommandLineSetup(toolName, args, resources);
    final String settingsContent = "content";
    myCtx.checking(new Expectations() {{
      oneOf(myRunnerParametersService).isRunningUnderWindows();
      will(returnValue(true));

      oneOf(myRunnerParametersService).tryGetConfigParameter(Constants.USER_VAR);
      will(returnValue(user));

      oneOf(myRunnerParametersService).tryGetConfigParameter(Constants.PASSWORD_VAR);
      will(returnValue(password));

      oneOf(myFileService).getCheckoutDirectory();
      will(returnValue(checkoutDir));

      oneOf(myFileService).getTempFileName(RunAsSetupBuilder.SETTINGS_EXT);
      will(returnValue(settingsFile));

      oneOf(mySettingsGenerator).create(with(new Settings(baseSetup, user, password, checkoutDir.getAbsolutePath())));
      will(returnValue(settingsContent));

      oneOf(myRunnerParametersService).getToolPath(Constants.RUN_AS_TOOL_NAME);
      will(returnValue(runAsToolPath));

      oneOf(myFileService).validatePath(runAsTool);
    }});

    final CommandLineSetupBuilder instance = createInstance();

    // When
    final CommandLineSetup setup = instance.build(new CommandLineSetup(toolName, args, resources)).iterator().next();

    // Then
    myCtx.assertIsSatisfied();
    then(setup.getToolPath()).isEqualTo(runAsTool.getAbsolutePath());
    then(setup.getResources()).containsExactly(myCommandLineResource1, myCommandLineResource2, new CommandLineFile(myResourcePublisher, settingsFile, settingsContent));
    then(setup.getArgs()).containsExactly(new CommandLineArgument(RunAsSetupBuilder.CONFIG_FILE_CMD_KEY + settingsFile.getAbsolutePath(), CommandLineArgument.Type.PARAMETER));
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

  @Test(dataProvider = "configurationParametersCases")
  public void shouldNotBuildSetupAndSendWarningWhenConfigurationParametersWereNotSpecified(@Nullable final String user, @Nullable final String password) throws IOException {
    // Given
    final String toolName = "tool";
    final List<CommandLineArgument> args = Arrays.asList(new CommandLineArgument("arg1", CommandLineArgument.Type.PARAMETER), new CommandLineArgument("arg2", CommandLineArgument.Type.PARAMETER));
    final List<CommandLineResource> resources = Arrays.asList(myCommandLineResource1, myCommandLineResource2);
    final CommandLineSetup baseSetup = new CommandLineSetup(toolName, args, resources);
    myCtx.checking(new Expectations() {{
      oneOf(myRunnerParametersService).isRunningUnderWindows();
      will(returnValue(true));

      oneOf(myRunnerParametersService).tryGetConfigParameter(Constants.USER_VAR);
      will(returnValue(user));

      allowing(myRunnerParametersService).tryGetConfigParameter(Constants.PASSWORD_VAR);
      will(returnValue(password));

      oneOf(myLoggerService).onMessage(with(any(Message.class)));
      will(returnValue(password));
    }});

    final CommandLineSetupBuilder instance = createInstance();

    // When
    final CommandLineSetup setup = instance.build(new CommandLineSetup(toolName, args, resources)).iterator().next();

    // Then
    myCtx.assertIsSatisfied();
    then(setup).isEqualTo(baseSetup);
  }

  @NotNull
  private CommandLineSetupBuilder createInstance()
  {
    return new RunAsSetupBuilder(
      myRunnerParametersService,
      myFileService,
      myResourcePublisher,
      mySettingsGenerator,
      myLoggerService);
  }
}
