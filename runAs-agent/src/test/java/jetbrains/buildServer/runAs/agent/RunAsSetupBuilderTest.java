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
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
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
  private LoggerService myLoggerService;
  private CommandLineResource myCommandLineResource1;
  private CommandLineResource myCommandLineResource2;
  private ResourceGenerator<RunAsArgsSettings> myArgsGenerator;
  private CommandLineArgumentsService myCommandLineArgumentsService;
  private ResourceGenerator<RunAsArgsSettings> myTeamCityServiceMessagesGenerator;

  @BeforeMethod
  public void setUp()
  {
    myCtx = new Mockery();
    myRunnerParametersService = myCtx.mock(RunnerParametersService.class);
    myFileService = myCtx.mock(FileService.class);
    myResourcePublisher = myCtx.mock(ResourcePublisher.class);
    //noinspection unchecked
    myCredentialsGenerator = (ResourceGenerator<CredentialsSettings>)myCtx.mock(ResourceGenerator.class, "CredentialsGenerator");
    //noinspection unchecked
    myArgsGenerator = (ResourceGenerator<RunAsArgsSettings>)myCtx.mock(ResourceGenerator.class, "ArgsGenerator");
    //noinspection unchecked
    myTeamCityServiceMessagesGenerator = (ResourceGenerator<RunAsArgsSettings>)myCtx.mock(ResourceGenerator.class, "TeamCityServiceMessagesGenerator");
    myLoggerService = myCtx.mock(LoggerService.class);
    myCommandLineResource1 = myCtx.mock(CommandLineResource.class, "Res1");
    myCommandLineResource2 = myCtx.mock(CommandLineResource.class, "Res2");
    myCommandLineArgumentsService = myCtx.mock(CommandLineArgumentsService.class);
  }

  @Test()
  public void shouldBuildSetup() throws IOException {
    // Given
    final File checkoutDir = new File("checkoutDir");
    final File tempDir = new File("tempDir");
    final String sessionId = "session";
    final File sessionFile = new File(tempDir, sessionId);
    final String toolName = "my tool";
    final String runAsToolPath = "runAsPath";
    final File runAsTool = new File(runAsToolPath, RunAsSetupBuilder.TOOL_FILE_NAME);
    final String user = "nik";
    final String password = "abc";
    final List<CommandLineArgument> args = Arrays.asList(new CommandLineArgument("arg1", CommandLineArgument.Type.PARAMETER), new CommandLineArgument("arg2", CommandLineArgument.Type.PARAMETER));
    final List<CommandLineResource> resources = Arrays.asList(myCommandLineResource1, myCommandLineResource2);
    final String credentialsContent = "credentials content";
    final String cmdContent = "args content";
    final String messagesContent = "messages content";
    final CommandLineSetup commandLineSetup = new CommandLineSetup(toolName, args, resources);
    final RunAsArgsSettings runAsArgsSettings = new RunAsArgsSettings("cmd line", checkoutDir.getAbsolutePath());
    myCtx.checking(new Expectations() {{
      oneOf(myRunnerParametersService).isRunningUnderWindows();
      will(returnValue(true));

      oneOf(myRunnerParametersService).tryGetConfigParameter(Constants.USER_VAR);
      will(returnValue(user));

      oneOf(myRunnerParametersService).tryGetConfigParameter(Constants.PASSWORD_VAR);
      will(returnValue(password));

      oneOf(myFileService).getTempFileName("");
      will(returnValue(sessionFile));

      oneOf(myCredentialsGenerator).create(with(new CredentialsSettings(user, password)));
      will(returnValue(credentialsContent));

      //noinspection unchecked
      oneOf(myCommandLineArgumentsService).createCommandLineString(with(any(List.class)));
      will(returnValue("cmd line"));

      oneOf(myFileService).getCheckoutDirectory();
      will(returnValue(checkoutDir));

      oneOf(myArgsGenerator).create(runAsArgsSettings);
      will(returnValue(cmdContent));

      oneOf(myTeamCityServiceMessagesGenerator).create(runAsArgsSettings);
      will(returnValue(messagesContent));

      allowing(myFileService).getRelativePath(with(tempDir), with(any(File.class)));
      will(new CustomAction("getRelativePath") {
        @Override
        public Object invoke(final Invocation invocation) throws Throwable {
          return invocation.getParameter(1);
        }
      });

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
      new CommandLineFile(myResourcePublisher, new File(tempDir, sessionId + RunAsSetupBuilder.CREDENTIALS_EXT).getAbsoluteFile(), credentialsContent),
      new CommandLineFile(myResourcePublisher, new File(tempDir, sessionId + RunAsSetupBuilder.CMD_EXT).getAbsoluteFile(), cmdContent),
      new CommandLineFile(myResourcePublisher, new File(tempDir, sessionId + RunAsSetupBuilder.MESSAGES_EXT).getAbsoluteFile(), messagesContent));

    then(setup.getArgs()).containsExactly(
      new CommandLineArgument(sessionId, CommandLineArgument.Type.PARAMETER));
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
      myFileService,
      myResourcePublisher,
      myCredentialsGenerator,
      myArgsGenerator,
      myTeamCityServiceMessagesGenerator,
      myLoggerService,
      myCommandLineArgumentsService);
  }
}
