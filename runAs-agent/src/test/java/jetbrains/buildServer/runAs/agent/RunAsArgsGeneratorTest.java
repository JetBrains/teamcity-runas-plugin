package jetbrains.buildServer.runAs.agent;

import org.jetbrains.annotations.NotNull;
import org.testng.annotations.Test;

import static org.assertj.core.api.BDDAssertions.then;

public class RunAsArgsGeneratorTest {
  private static final String ourLineSeparator = System.getProperty("line.separator");

  @Test()
  public void shouldGenerateContent() {
    // Given
    final String expectedContent = "-w:w d" + ourLineSeparator + "cmd.exe" + ourLineSeparator + "/S" + ourLineSeparator + "/C" + ourLineSeparator + "\"cmd line\"";
    final RunAsArgsGenerator instance = createInstance();

    // When
    final String content = instance.create(new RunAsArgsSettings("cmd line", "w d"));

    // Then
    then(content.trim().replace("\n", " ").replace("\r", "")).isEqualTo(expectedContent.trim().replace("\n", " ").replace("\r", ""));
  }

  @NotNull
  private RunAsArgsGenerator createInstance()
  {
    return new RunAsArgsGenerator();
  }
}
