package jetbrains.buildServer.runAs.agent;

import java.util.ArrayList;
import java.util.Arrays;
import jetbrains.buildServer.dotNet.buildRunner.agent.*;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.BDDAssertions.then;

public class RunAsSetupBuilderTest {

  private Mockery myCtx;
  private RunnerParametersService myRunnerParametersService;
  private CommandLineSetupBuilder myRunAsWindowsSetupBuilder;
  private CommandLineSetupBuilder myRunAsUnixSetupBuilder;

  @BeforeMethod
  public void setUp()
  {
    myCtx = new Mockery();
    myRunnerParametersService = myCtx.mock(RunnerParametersService.class);
    myRunAsWindowsSetupBuilder = myCtx.mock(CommandLineSetupBuilder.class, "RunAsPlatformSpecificSetupBuilder");
    myRunAsUnixSetupBuilder = myCtx.mock(CommandLineSetupBuilder.class, "RunAsUnixSetupBuilder");
  }

  @Test()
  public void shouldBuildSetupWhenWindows()
  {
    // Given
    final CommandLineSetup commandLineSetup = new CommandLineSetup("aa", new ArrayList<CommandLineArgument>(), new ArrayList<CommandLineResource>());
    final CommandLineSetup runAsCommandLineSetup = new CommandLineSetup("aa", new ArrayList<CommandLineArgument>(), new ArrayList<CommandLineResource>());

    myCtx.checking(new Expectations() {{
        oneOf(myRunnerParametersService).isRunningUnderWindows();
        will(returnValue(true));

        oneOf(myRunAsWindowsSetupBuilder).build(commandLineSetup);
        will(returnValue(Arrays.asList(runAsCommandLineSetup)));

        never(myRunAsUnixSetupBuilder).build(commandLineSetup);
    }});

    final CommandLineSetupBuilder instance = createInstance();

    // When
    final CommandLineSetup setup = instance.build(commandLineSetup).iterator().next();

    // Then
    myCtx.assertIsSatisfied();
    then(setup).isEqualTo(runAsCommandLineSetup);
  }

  @Test()
  public void shouldBuildSetupWhenLinux()
  {
    // Given
    final CommandLineSetup commandLineSetup = new CommandLineSetup("aa", new ArrayList<CommandLineArgument>(), new ArrayList<CommandLineResource>());
    final CommandLineSetup runAsCommandLineSetup = new CommandLineSetup("aa", new ArrayList<CommandLineArgument>(), new ArrayList<CommandLineResource>());

    myCtx.checking(new Expectations() {{
      oneOf(myRunnerParametersService).isRunningUnderWindows();
      will(returnValue(false));

      oneOf(myRunAsUnixSetupBuilder).build(commandLineSetup);
      will(returnValue(Arrays.asList(runAsCommandLineSetup)));

      never(myRunAsWindowsSetupBuilder).build(commandLineSetup);
    }});

    final CommandLineSetupBuilder instance = createInstance();

    // When
    final CommandLineSetup setup = instance.build(commandLineSetup).iterator().next();

    // Then
    myCtx.assertIsSatisfied();
    then(setup).isEqualTo(runAsCommandLineSetup);
  }

  private CommandLineSetupBuilder createInstance()
  {
    return new RunAsSetupBuilder(
        myRunnerParametersService,
        myRunAsWindowsSetupBuilder,
        myRunAsUnixSetupBuilder
      );
  }
}
