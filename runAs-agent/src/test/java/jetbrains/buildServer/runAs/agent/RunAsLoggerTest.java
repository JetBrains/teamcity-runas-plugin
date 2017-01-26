package jetbrains.buildServer.runAs.agent;

import java.io.File;
import java.util.*;
import jetbrains.buildServer.dotNet.buildRunner.agent.*;
import jetbrains.buildServer.runAs.common.LoggingLevel;
import jetbrains.buildServer.runAs.common.WindowsIntegrityLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static jetbrains.buildServer.runAs.agent.Constants.PASSWORD_REPLACEMENT_VAL;
import static jetbrains.buildServer.runAs.common.Constants.RUN_AS_LOG_ENABLED;
import static org.assertj.core.api.BDDAssertions.then;

public class RunAsLoggerTest {
  private Mockery myCtx;
  private LoggerService myLoggerService;
  private PathsService myPathsService;
  private CommandLineResource myCommandLineResource1;
  private CommandLineResource myCommandLineResource2;
  private SecuredLoggingService mySecuredLoggingService;
  private RunnerParametersService myRunnerParametersService;

  @BeforeMethod
  public void setUp()
  {
    myCtx = new Mockery();
    myLoggerService = myCtx.mock(LoggerService.class);
    myPathsService = myCtx.mock(PathsService.class);
    mySecuredLoggingService = myCtx.mock(SecuredLoggingService.class);
    myRunnerParametersService = myCtx.mock(RunnerParametersService.class);
    myCommandLineResource1 = myCtx.mock(CommandLineResource.class, "Res1");
    myCommandLineResource2 = myCtx.mock(CommandLineResource.class, "Res2");
  }

  @DataProvider(name = "getLogRunAsCases")
  public Object[][] getParamCases() {
    return new Object[][]{
      // when log is enabled
      {
        "true",
        new String[] {
          "Starting: tool cred cmd " + PASSWORD_REPLACEMENT_VAL,
          "as user: username",
          "Starting: origTool arg1",
          "in directory: CheckoutDirectory" }
      },

      // when log is disabled
      {
        "false",
        new String[] {
          "Starting: origTool arg1",
          "in directory: CheckoutDirectory" }
      },

      // when log is disabled by default
      {
        null,
        new String[] {
          "Starting: origTool arg1",
          "in directory: CheckoutDirectory" }
      }
    };
  }

  @Test(dataProvider = "getLogRunAsCases")
  public void shouldLogRunAs(@Nullable final String isLogEnabledConfigParamValue, @NotNull final String[] expectedLogLines) {
    // Given
    final String password = "abc";
    final UserCredentials userCredentials = new UserCredentials("username", password, WindowsIntegrityLevel.Auto, LoggingLevel.Normal, Collections.<CommandLineArgument>emptyList(), new AccessControlList(Collections.<AccessControlEntry>emptyList()));
    final File checkoutDirectory = new File("CheckoutDirectory");
    final ArrayList<String> logMessages = new ArrayList<String>();
    final CommandLineSetup baseCommandLineSetup = new CommandLineSetup(
      "origTool",
      Arrays.asList(
        new CommandLineArgument("arg1", CommandLineArgument.Type.PARAMETER)),
      Arrays.asList(
        myCommandLineResource1));

    final CommandLineSetup runAsCommandLineSetup = new CommandLineSetup(
      "tool",
      Arrays.asList(
        new CommandLineArgument("cred", CommandLineArgument.Type.PARAMETER),
        new CommandLineArgument("cmd", CommandLineArgument.Type.PARAMETER),
        new CommandLineArgument(password, CommandLineArgument.Type.PARAMETER)),
      Arrays.asList(
        myCommandLineResource1,
        myCommandLineResource2));

    myCtx.checking(new Expectations() {{
      oneOf(myRunnerParametersService).tryGetConfigParameter(RUN_AS_LOG_ENABLED);
      will(returnValue(isLogEnabledConfigParamValue));

      oneOf(mySecuredLoggingService).disableLoggingOfCommandLine();

      oneOf(myPathsService).getPath(WellKnownPaths.Checkout);
      will(returnValue(checkoutDirectory));

      allowing(myLoggerService).onStandardOutput(with(any(String.class)));
      will(new CustomAction("onStandardOutput") {
        @Override
        public Object invoke(final Invocation invocation) throws Throwable {
          logMessages.add((String)invocation.getParameter(0));
          return null;
        }
      });
    }});

    final RunAsLogger logger = createInstance();

    // When
    logger.LogRunAs(userCredentials, baseCommandLineSetup, runAsCommandLineSetup);

    // Then
    myCtx.assertIsSatisfied();
    then(logMessages).containsSequence(expectedLogLines);
  }

  @NotNull
  private RunAsLogger createInstance()
  {
    return new RunAsLoggerImpl(
      myLoggerService,
      myPathsService,
      mySecuredLoggingService,
      myRunnerParametersService);
  }
}
