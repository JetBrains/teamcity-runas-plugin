package jetbrains.buildServer.runAs.agent;

import com.intellij.execution.ExecutionException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import jetbrains.buildServer.agent.ToolCannotBeFoundException;
import jetbrains.buildServer.agent.ToolProvider;
import jetbrains.buildServer.agent.ToolProvidersRegistry;
import jetbrains.buildServer.agent.plugins.beans.PluginDescriptor;
import jetbrains.buildServer.dotNet.buildRunner.agent.RunnerParametersService;
import jetbrains.buildServer.runAs.common.Constants;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.BDDAssertions.then;

public class RunAsToolProviderTest {
  private Mockery myCtx;
  private ToolProvidersRegistry myToolProvidersRegistry;
  private PluginDescriptor myPluginDescriptor;
  private RunnerParametersService myRunnerParametersService;

  @BeforeMethod
  public void setUp()
  {
    myCtx = new Mockery();
    myPluginDescriptor = myCtx.mock(PluginDescriptor.class);
    myToolProvidersRegistry = myCtx.mock(ToolProvidersRegistry.class);
    myRunnerParametersService = myCtx.mock(RunnerParametersService.class);
  }

  @Test
  public void shouldGetPath() throws IOException, ExecutionException {
    // Given
    final File pluginRootDir = new File("plugin");
    final File binPath = new File(pluginRootDir, RunAsToolProvider.BIN_PATH);

    final List<ToolProvider> toolProviders = new ArrayList<ToolProvider>();
    myCtx.checking(new Expectations() {{
      oneOf(myRunnerParametersService).isRunningUnderWindows();
      will(returnValue(true));

      oneOf(myPluginDescriptor).getPluginRoot();
      will(returnValue(pluginRootDir));

      oneOf(myToolProvidersRegistry).registerToolProvider(with(any(ToolProvider.class)));
      will(new CustomAction("registerToolProvider") {
        @Override
        public Object invoke(final Invocation invocation) throws Throwable {
          toolProviders.add((ToolProvider)invocation.getParameter(0));
          return null;
        }
      });
    }});

    // When
    createInstance();

    final ToolProvider toolProvider = toolProviders.get(0);
    final String toolPath = toolProvider.getPath(Constants.RUN_AS_TOOL_NAME);

    // Then
    myCtx.assertIsSatisfied();
    then(toolProviders.size()).isEqualTo(1);
    then(toolProvider.supports(Constants.RUN_AS_TOOL_NAME)).isTrue();
    then(new File(toolPath).getAbsolutePath()).isEqualTo(binPath.getAbsolutePath());
  }

  @Test()
  public void shouldThrowToolCannotBeFoundExceptionWhenIsNotRunningUnderWindows() throws IOException, ExecutionException {
    // Given
    final List<ToolProvider> toolProviders = new ArrayList<ToolProvider>();
    myCtx.checking(new Expectations() {{
      oneOf(myRunnerParametersService).isRunningUnderWindows();
      will(returnValue(false));

      oneOf(myToolProvidersRegistry).registerToolProvider(with(any(ToolProvider.class)));
      will(new CustomAction("registerToolProvider") {
        @Override
        public Object invoke(final Invocation invocation) throws Throwable {
          toolProviders.add((ToolProvider)invocation.getParameter(0));
          return null;
        }
      });
    }});

    // When
    createInstance();

    boolean actualExceptionThrown = false;
    final ToolProvider toolProvider = toolProviders.get(0);
    try {
      toolProvider.getPath(Constants.RUN_AS_TOOL_NAME);
    }
    catch (ToolCannotBeFoundException ex){
      actualExceptionThrown = true;
    }

    // Then
    myCtx.assertIsSatisfied();
    then(actualExceptionThrown).isTrue();
  }

  @NotNull
  private RunAsToolProvider createInstance()
  {
    return new RunAsToolProvider(
      myPluginDescriptor,
      myToolProvidersRegistry,
      myRunnerParametersService);
  }
}
