package jetbrains.buildServer.runAs.agent;

import java.util.Arrays;
import java.util.Collections;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineArgument;
import jetbrains.buildServer.dotNet.buildRunner.agent.ResourceGenerator;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.assertj.core.api.BDDAssertions.then;

public class CmdGeneratorTest {
  private static final String LINE_SEPARATOR = System.getProperty("line.separator");

  @DataProvider(name = "cmdLinesCases")
  public Object[][] getCmdLinesCases() {
    return new Object[][] {
      {
        new RunAsParams(
          Arrays.asList(
            new CommandLineArgument("tool", CommandLineArgument.Type.TOOL),
            new CommandLineArgument("a b", CommandLineArgument.Type.PARAMETER))),
      "\"tool\" \"a b\""},

      // using double quotes
      {
        new RunAsParams(
          Arrays.asList(
            new CommandLineArgument("tool", CommandLineArgument.Type.TOOL),
            new CommandLineArgument("a \" b", CommandLineArgument.Type.PARAMETER))),
        "\"tool\" \"a \"\" b\""},

      // empty args
      {
        new RunAsParams(
          Collections.<CommandLineArgument>emptyList()),
          ""},
    };
  }

  @Test(dataProvider = "cmdLinesCases")
  public void shouldGenerateContent(@NotNull final RunAsParams runAsParams, @NotNull final String expectedCmdLine) {
    // Given
    final ResourceGenerator<RunAsParams> instance = createInstance();

    // When
    final String content = instance.create(runAsParams);

    // Then
    then(content).isEqualTo("@ECHO OFF"
                            + LINE_SEPARATOR + expectedCmdLine
                            + LINE_SEPARATOR + "SET \"EXIT_CODE=%ERRORLEVEL%\""
                            + LINE_SEPARATOR + "EXIT /B %EXIT_CODE%");
  }

  @NotNull
  private ResourceGenerator<RunAsParams> createInstance()
  {
    return new CmdGenerator();
  }
}