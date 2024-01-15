

package jetbrains.buildServer.runAs.agent;

import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.dotNet.buildRunner.agent.BuildRunnerContextProvider;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SecuredLoggingServiceTest {
  private Mockery myCtx;
  private BuildRunnerContextProvider myBuildRunnerContextProvider;
  private BuildRunnerContext myBuildRunnerContext;

  @BeforeMethod
  public void setUp()
  {
    myCtx = new Mockery();
    myBuildRunnerContextProvider = myCtx.mock(BuildRunnerContextProvider.class);
    myBuildRunnerContext = myCtx.mock(BuildRunnerContext.class);
  }

  @Test
  public void shouldDisableLoggingOfCommandLine() {
    // Given
    myCtx.checking(new Expectations() {{
      oneOf(myBuildRunnerContextProvider).getContext();
      will(returnValue(myBuildRunnerContext));

      oneOf(myBuildRunnerContext).addConfigParameter(SecuredLoggingServiceImpl.TEAMCITY_BUILD_LOG_LOG_COMMAND_LINE, Boolean.toString(false));
    }});

    final SecuredLoggingService service = createInstance();

    // When
    service.disableLoggingOfCommandLine();

    // Then
    myCtx.assertIsSatisfied();
  }

  @NotNull
  private SecuredLoggingService createInstance()
  {
    return new SecuredLoggingServiceImpl(
      myBuildRunnerContextProvider);
  }
}