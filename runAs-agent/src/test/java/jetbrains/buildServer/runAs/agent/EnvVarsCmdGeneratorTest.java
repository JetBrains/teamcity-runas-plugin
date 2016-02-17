package jetbrains.buildServer.runAs.agent;

import java.util.Arrays;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.Test;

import static org.assertj.core.api.BDDAssertions.then;

public class EnvVarsCmdGeneratorTest {
  @Test()
  public void shouldGenerateContent() {
    // Given
    final EnvVarsCmdGenerator instance = createInstance();

    // When
    final String content = instance.create(Arrays.asList(new EnvironmentVariable("ENV1"), new EnvironmentVariable("Env 2"), new EnvironmentVariable("  Env 3 ")));

    // Then
    then(content).contains("@ECHO OFF");
    then(content).contains("ECHO SET \"ENV1=%ENV1%\"");
    then(content).contains("ECHO SET \"Env 2=%Env 2%\"");
    then(content).contains("ECHO SET \"  Env 3 =%  Env 3 %\"");
  }

  @NotNull
  private EnvVarsCmdGenerator createInstance()
  {
    return new EnvVarsCmdGenerator();
  }
}
