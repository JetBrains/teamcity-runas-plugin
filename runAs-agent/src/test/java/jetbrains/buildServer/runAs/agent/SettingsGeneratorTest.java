package jetbrains.buildServer.runAs.agent;

import java.util.Arrays;
import java.util.List;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineArgument;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineArgumentsService;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineResource;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineSetup;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.BDDAssertions.then;

public class SettingsGeneratorTest {
  private static final String ourlineSeparator = System.getProperty("line.separator");
  private Mockery myCtx;
  private CommandLineArgumentsService myCommandLineArgumentsService;
  private CommandLineResource myCommandLineResource1;
  private CommandLineResource myCommandLineResource2;

  @BeforeMethod
  public void setUp()
  {
    myCtx = new Mockery();
    myCommandLineArgumentsService = myCtx.mock(CommandLineArgumentsService.class);
    myCommandLineResource1 = myCtx.mock(CommandLineResource.class, "Res1");
    myCommandLineResource2 = myCtx.mock(CommandLineResource.class, "Res2");
  }

  @Test()
  public void shouldGenerateContent() {
    // Given
    final List<CommandLineArgument> args = Arrays.asList(new CommandLineArgument("arg1", CommandLineArgument.Type.PARAMETER), new CommandLineArgument("arg2", CommandLineArgument.Type.PARAMETER), new CommandLineArgument("arg 3", CommandLineArgument.Type.PARAMETER));
    final Settings settings = new Settings(
      new CommandLineSetup(
        "tool",
        args,
        Arrays.asList(myCommandLineResource1, myCommandLineResource2)),
      "nik",
      "aa",
      "wd"
    );

    String expectedContent = "-u:nik" + ourlineSeparator +
                             "-p:aa" + ourlineSeparator +
                             "-w:wd" + ourlineSeparator +
                             "tool" + ourlineSeparator +
                             "arg1" + ourlineSeparator +
                             "arg2" + ourlineSeparator +
                             "arg 3";

    myCtx.checking(new Expectations() {{
      oneOf(myCommandLineArgumentsService).normalizeCommandLineArguments(settings.getSetup().getArgs());
      will(returnValue(args));
    }});

    final SettingsGenerator instance = createInstance();

    // When
    final String content = instance.create(settings);

    // Then
    myCtx.assertIsSatisfied();
    then(content.trim().replace("\n", "").replace("\r", "")).isEqualTo(expectedContent.trim().replace("\n", "").replace("\r", ""));
  }

  @Test()
  public void shouldGenerateContentWhenEmptyArgs() {
    // Given
    final List<CommandLineArgument> args = Arrays.asList();
    final Settings settings = new Settings(
      new CommandLineSetup(
        "tool",
        args,
        Arrays.asList(myCommandLineResource1, myCommandLineResource2)),
      "nik",
      "aa",
      "wd"
    );

    String expectedContent = "-u:nik" + ourlineSeparator +
                             "-p:aa" + ourlineSeparator +
                             "-w:wd" + ourlineSeparator +
                             "tool";

    myCtx.checking(new Expectations() {{
      oneOf(myCommandLineArgumentsService).normalizeCommandLineArguments(settings.getSetup().getArgs());
      will(returnValue(args));
    }});

    final SettingsGenerator instance = createInstance();

    // When
    final String content = instance.create(settings);

    // Then
    myCtx.assertIsSatisfied();
    then(content.trim().replace("\n", "").replace("\r", "")).isEqualTo(expectedContent.trim().replace("\n", "").replace("\r", ""));
  }

  @NotNull
  private SettingsGenerator createInstance()
  {
    return new SettingsGenerator(myCommandLineArgumentsService);
  }
}
