package jetbrains.buildServer.runAs.agent;

import org.jetbrains.annotations.NotNull;
import org.testng.annotations.Test;

import static org.assertj.core.api.BDDAssertions.then;

public class RunAsCmdGeneratorTest {
  private static final String ourLineSeparator = System.getProperty("line.separator");

  @Test()
  public void shouldGenerateContent() {
    // Given
    final RunAsCmdGenerator instance = createInstance();

    // When
    final String content = instance.create(new RunAsArgsSettings("cmd line", "w d"));

    // Then
    then(content).contains("PUSHD \"w d\"");
    then(content).contains("cmd line");
  }

  @NotNull
  private RunAsCmdGenerator createInstance()
  {
    return new RunAsCmdGenerator();
  }
}
