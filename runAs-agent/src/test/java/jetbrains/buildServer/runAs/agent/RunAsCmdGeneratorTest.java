package jetbrains.buildServer.runAs.agent;

import org.jetbrains.annotations.NotNull;
import org.testng.annotations.Test;

import static org.assertj.core.api.BDDAssertions.then;

public class RunAsCmdGeneratorTest {

  @Test()
  public void shouldGenerateContent() {
    // Given
    final RunAsCmdGenerator instance = createInstance();

    // When
    final String content = instance.create(new RunAsCmdSettings("cmd line", "w d"));

    // Then
    then(content).contains("PUSHD \"w d\"");
    then(content).contains("cmd line");
    then(content).contains("POPD");
  }

  @NotNull
  private RunAsCmdGenerator createInstance()
  {
    return new RunAsCmdGenerator();
  }
}
