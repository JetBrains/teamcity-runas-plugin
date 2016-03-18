package jetbrains.buildServer.runAs.agent;

import org.jetbrains.annotations.NotNull;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.assertj.core.api.BDDAssertions.then;

public class RunAsCmdGeneratorTest {
  private static final String LINE_SEPARATOR = System.getProperty("line.separator");

  @DataProvider(name = "cmdLinesCases")
  public Object[][] getCmdLinesCases() {
    return new Object[][] {
      { "cmd line", "cmd line" },
      { "^", "^^" },
      { "^^", "^^^^" },
      { "\\", "^\\" },
      { "&", "^&" },
      { "|", "^|^|" },
      { "\\", "^\\" },
      { "'", "^|^'" },
      { ">", "^>" },
      { "<", "^<" },
      { "cmd line where cat!=OOMT&&cat!=OOM", "cmd line where cat!=OOMT^&^&cat!=OOM" },
      { "cmd line where cat!='OOMT&&cat!=OOM'", "cmd line where cat!=^|^'OOMT^&^&cat!=OOM^|^'" },
      { "'\\&|><^'", "^|^'^\\^&^|^|^>^<^^^|^'" },
    };
  }

  @Test(dataProvider = "cmdLinesCases")
  public void shouldGenerateContent(@NotNull final String cmdLine, @NotNull final String cmdLineInMessage) {
    // Given
    final RunAsCmdGenerator instance = createInstance();

    // When
    final String content = instance.create(new RunAsCmdSettings(cmdLine));

    // Then
    then(content).isEqualTo("@ECHO OFF"
                            + LINE_SEPARATOR + "ECHO ##teamcity[message text=^'Starting: " + cmdLineInMessage + "^' status=^'NORMAL^']"
                            + LINE_SEPARATOR + cmdLine
                            + LINE_SEPARATOR + "SET \"EXIT_CODE=%ERRORLEVEL%\""
                            + LINE_SEPARATOR + "EXIT /B %EXIT_CODE%");
  }

  @NotNull
  private RunAsCmdGenerator createInstance()
  {
    return new RunAsCmdGenerator();
  }
}
