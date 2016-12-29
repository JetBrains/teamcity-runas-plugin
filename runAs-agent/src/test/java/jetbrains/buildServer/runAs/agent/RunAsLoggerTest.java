package jetbrains.buildServer.runAs.agent;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import jetbrains.buildServer.dotNet.buildRunner.agent.*;
import jetbrains.buildServer.runAs.common.Constants;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.BDDAssertions.then;

public class RunAsLoggerTest {
  private Mockery myCtx;
  private LoggerService myLoggerService;
  private PathsService myPathsService;
  private CommandLineResource myCommandLineResource1;
  private CommandLineResource myCommandLineResource2;
  private SecuredLoggingService mySecuredLoggingService;

  @BeforeMethod
  public void setUp()
  {
    myCtx = new Mockery();
    myLoggerService = myCtx.mock(LoggerService.class);
    myPathsService = myCtx.mock(PathsService.class);
    mySecuredLoggingService = myCtx.mock(SecuredLoggingService.class);
    myCommandLineResource1 = myCtx.mock(CommandLineResource.class, "Res1");
    myCommandLineResource2 = myCtx.mock(CommandLineResource.class, "Res2");
  }

  @Test
  public void shouldLogRunAs() {
    // Given
    final String password = "abc";
    final File checkoutDirectory = new File("CheckoutDirectory");
    final ArrayList<String> logMessages = new ArrayList<String>();
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
    logger.LogRunAs(runAsCommandLineSetup);

    // Then
    myCtx.assertIsSatisfied();
    then(logMessages.size()).isEqualTo(2);
    then(logMessages).containsSequence(
      "Starting: tool cred cmd " + Constants.PASSWORD_REPLACEMENT_VAL,
      "in directory: CheckoutDirectory");
  }

  @NotNull
  private RunAsLogger createInstance()
  {
    return new RunAsLoggerImpl(
      myLoggerService,
      myPathsService,
      mySecuredLoggingService);
  }
}
