package jetbrains.buildServer.runAs.agent;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.assertj.core.api.BDDAssertions.then;

public class EnvironmentVariablesParserTest {
  private static final String LINE_DELIMITER = System.getProperty("line.separator");

  @DataProvider(name = "environmentVariablesCases")
  public Object[][] getEnvironmentVariablesCases() {
    return new Object[][] {
      { "var1", Arrays.asList(new EnvironmentVariable("var1")) },
      { "var1,var2", Arrays.asList(new EnvironmentVariable("var1"), new EnvironmentVariable("var2")) },
      { "var1,var2,Var3,Var 4", Arrays.asList(new EnvironmentVariable("var1"), new EnvironmentVariable("var2"), new EnvironmentVariable("Var3"), new EnvironmentVariable("Var 4")) },
      { "  var1 ,  var2  , Var3 , Var 4 ", Arrays.asList(new EnvironmentVariable("  var1 "), new EnvironmentVariable("  var2  "), new EnvironmentVariable(" Var3 "), new EnvironmentVariable(" Var 4 ")) },
      { "var1" + LINE_DELIMITER + "var2", Arrays.asList(new EnvironmentVariable("var1"), new EnvironmentVariable("var2")) },
      { "var1 " + LINE_DELIMITER + " v a r 2, var3 ", Arrays.asList(new EnvironmentVariable("var1 "), new EnvironmentVariable(" v a r 2"), new EnvironmentVariable(" var3 ")) },
    };
  }

  @Test(dataProvider = "environmentVariablesCases")
  public void shouldParseTextToEnvironmentVariables(@NotNull final String text, @NotNull final List<EnvironmentVariable> expectedEnvironmentVariables) throws IOException {
    // Given

    final EnvironmentVariablesParser instance = createInstance();

    // When
    final List<EnvironmentVariable> actualEnvironmentVariables = instance.parse(text);

    // Then
    then(expectedEnvironmentVariables).containsExactlyElementsOf(actualEnvironmentVariables);
  }

  @NotNull
  private EnvironmentVariablesParser createInstance()
  {
    return new EnvironmentVariablesParser();
  }
}
