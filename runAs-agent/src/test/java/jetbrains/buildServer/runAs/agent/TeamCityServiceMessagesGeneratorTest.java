package jetbrains.buildServer.runAs.agent;

import bsh.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.Test;

import static org.assertj.core.api.BDDAssertions.then;

public class TeamCityServiceMessagesGeneratorTest {
  private static final String ourLineSeparator = System.getProperty("line.separator");

  @Test()
  public void shouldGenerateContent() {
    // Given
    final TeamCityServiceMessagesGenerator instance = createInstance();

    // When
    final String content = instance.create(new RunAsArgsSettings("cmd line", "w d"));
    final String[] lines = StringUtil.split(content, ourLineSeparator);

    // Then
    then(lines.length).isEqualTo(2);
    then(lines[0]).contains("cmd line");
    then(lines[1]).contains("w d");
  }

  @NotNull
  private TeamCityServiceMessagesGenerator createInstance()
  {
    return new TeamCityServiceMessagesGenerator();
  }
}
