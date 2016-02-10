package jetbrains.buildServer.runAs.agent;

import org.jetbrains.annotations.NotNull;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.BDDAssertions.then;

public class CredentialsGeneratorTest {
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
    String expectedContent = "-u:nik" + ourlineSeparator + "-p:aa";

    final CredentialsGenerator instance = createInstance();

    // When
    final String content = instance.create(new CredentialsSettings("nik", "aa"));

    // Then
    myCtx.assertIsSatisfied();
    then(content.trim().replace("\n", "").replace("\r", "")).isEqualTo(expectedContent.trim().replace("\n", "").replace("\r", ""));
  }

  @NotNull
  private CredentialsGenerator createInstance()
  {
    return new CredentialsGenerator();
  }
}
