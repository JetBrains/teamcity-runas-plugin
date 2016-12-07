package jetbrains.buildServer.runAs.agent;

import java.util.Arrays;
import java.util.List;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineArgument;
import jetbrains.buildServer.dotNet.buildRunner.agent.ResourceGenerator;
import jetbrains.buildServer.runAs.common.WindowsIntegrityLevel;
import org.jetbrains.annotations.NotNull;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.BDDAssertions.then;

public class WindowsSettingsGeneratorTest {
  private static final String ourlineSeparator = System.getProperty("line.separator");
  private Mockery myCtx;

  @BeforeMethod
  public void setUp()
  {
    myCtx = new Mockery();
  }

  @Test()
  public void shouldGenerateContent() {
    // Given
    final String expectedContent = "-u:nik" + ourlineSeparator + "arg1" + ourlineSeparator + "arg 2";
    final List<CommandLineArgument> additionalArgs = Arrays.asList(new CommandLineArgument("arg1", CommandLineArgument.Type.PARAMETER), new CommandLineArgument("arg 2", CommandLineArgument.Type.PARAMETER));

    final ResourceGenerator<Settings> instance = createInstance();

    // When
    final String content = instance.create(new Settings("nik", "aaa", WindowsIntegrityLevel.Auto, additionalArgs));

    // Then
    myCtx.assertIsSatisfied();
    then(content.trim().replace("\n", " ").replace("\r", "")).isEqualTo(expectedContent.trim().replace("\n", " ").replace("\r", ""));
  }

  @NotNull
  private ResourceGenerator<Settings> createInstance()
  {
    return new WindowsSettingsGenerator();
  }
}
