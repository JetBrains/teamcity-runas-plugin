package jetbrains.buildServer.runAs.agent;

import com.intellij.execution.ExecutionException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import jetbrains.buildServer.ExecResult;
import jetbrains.buildServer.agent.ToolCannotBeFoundException;
import jetbrains.buildServer.agent.ToolProvider;
import jetbrains.buildServer.agent.ToolProvidersRegistry;
import jetbrains.buildServer.agent.plugins.beans.PluginDescriptor;
import jetbrains.buildServer.dotNet.buildRunner.agent.*;
import jetbrains.buildServer.runAs.common.Constants;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.assertj.core.api.BDDAssertions.then;

public class RunAsToolProviderTest {
  private Mockery myCtx;
  private ToolProvidersRegistry myToolProvidersRegistry;
  private PluginDescriptor myPluginDescriptor;
  private RunnerParametersService myRunnerParametersService;
  private FileService myFileService;
  private CommandLineExecutor myCommandLineExecutor;

  @BeforeMethod
  public void setUp()
  {
    myCtx = new Mockery();
    myPluginDescriptor = myCtx.mock(PluginDescriptor.class);
    myToolProvidersRegistry = myCtx.mock(ToolProvidersRegistry.class);
    myRunnerParametersService = myCtx.mock(RunnerParametersService.class);
    myFileService = myCtx.mock(FileService.class);
    myCommandLineExecutor = myCtx.mock(CommandLineExecutor.class);
  }

  @DataProvider(name = "getPathCases")
  public Object[][] getPathCases() {
    return new Object[][] {
      { 64, "x64" },
      { 32, "x86" }
    };
  }

  @Test(dataProvider = "getPathCases")
  public void shouldGetPath(int bitness, @NotNull final String platform) throws IOException, ExecutionException {
    // Given
    final File pluginRootDir = new File("plugin");
    final File binPath = new File(pluginRootDir, RunAsToolProvider.BIN_PATH);
    final File osBitnessToolPath = new File(binPath, RunAsToolProvider.GET_OS_BITNESS_TOOL_FILE_NAME).getAbsoluteFile();
    final File runAsPath = new File(binPath, platform).getAbsoluteFile();
    final CommandLineSetup getOSBitnessSetup = new CommandLineSetup(osBitnessToolPath.getPath(), Collections.<CommandLineArgument>emptyList(), Collections.<CommandLineResource>emptyList());
    final ExecResult getOSBitnessExecResult = new ExecResult();
    getOSBitnessExecResult.setExitCode(bitness);

    final List<ToolProvider> toolProviders = new ArrayList<ToolProvider>();
    myCtx.checking(new Expectations() {{
      oneOf(myRunnerParametersService).isRunningUnderWindows();
      will(returnValue(true));

      oneOf(myFileService).validatePath(osBitnessToolPath);

      oneOf(myCommandLineExecutor).runProcess(getOSBitnessSetup, RunAsToolProvider.EXECUTION_TIMEOUT_SECONDS);
      will(returnValue(getOSBitnessExecResult));

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
    toolProvider.getPath(Constants.RUN_AS_TOOL_NAME);

    // Then
    myCtx.assertIsSatisfied();
    then(toolProviders.size()).isEqualTo(1);
    then(toolProvider.supports(Constants.RUN_AS_TOOL_NAME)).isTrue();
    then(new File(toolPath).getAbsolutePath()).isEqualTo(runAsPath.getAbsolutePath());
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

  @DataProvider(name = "unknownExitCodeCases")
  public Object[][] getUnknownExitCodeCases() {
    return new Object[][] {
      { 63 },
      { 33 },
      { 0 },
      { -2 },
    };
  }

  @Test(dataProvider = "unknownExitCodeCases")
  public void shouldThrowToolCannotBeFoundExceptionWhenUnknownExitCodeForOsBitness(int exitCode) throws IOException, ExecutionException {
    // Given
    final File pluginRootDir = new File("plugin");
    final File binPath = new File(pluginRootDir, RunAsToolProvider.BIN_PATH);
    final File osBitnessToolPath = new File(binPath, RunAsToolProvider.GET_OS_BITNESS_TOOL_FILE_NAME).getAbsoluteFile();
    final CommandLineSetup getOSBitnessSetup = new CommandLineSetup(osBitnessToolPath.getPath(), Collections.<CommandLineArgument>emptyList(), Collections.<CommandLineResource>emptyList());
    final ExecResult getOSBitnessExecResult = new ExecResult();
    getOSBitnessExecResult.setExitCode(exitCode);

    final List<ToolProvider> toolProviders = new ArrayList<ToolProvider>();
    myCtx.checking(new Expectations() {{
      oneOf(myRunnerParametersService).isRunningUnderWindows();
      will(returnValue(true));

      oneOf(myFileService).validatePath(osBitnessToolPath);

      oneOf(myCommandLineExecutor).runProcess(getOSBitnessSetup, RunAsToolProvider.EXECUTION_TIMEOUT_SECONDS);
      will(returnValue(getOSBitnessExecResult));

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

  @Test()
  public void shouldThrowToolCannotBeFoundExceptionWhenGetOSBitnessToolCantBeFind() throws IOException, ExecutionException {
    // Given
    final File pluginRootDir = new File("plugin");
    final File binPath = new File(pluginRootDir, RunAsToolProvider.BIN_PATH);
    final File osBitnessToolPath = new File(binPath, RunAsToolProvider.GET_OS_BITNESS_TOOL_FILE_NAME).getAbsoluteFile();

    final List<ToolProvider> toolProviders = new ArrayList<ToolProvider>();
    myCtx.checking(new Expectations() {{
      oneOf(myRunnerParametersService).isRunningUnderWindows();
      will(returnValue(true));

      oneOf(myFileService).validatePath(osBitnessToolPath);
      will(throwException(new BuildException("Failed to find ")));

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

  @Test
  public void shouldThrowToolCannotBeFoundExceptionWhenGetOSBitnessFailed() throws IOException, ExecutionException {
    // Given
    final File pluginRootDir = new File("plugin");
    final File binPath = new File(pluginRootDir, RunAsToolProvider.BIN_PATH);
    final File osBitnessToolPath = new File(binPath, RunAsToolProvider.GET_OS_BITNESS_TOOL_FILE_NAME).getAbsoluteFile();
    final CommandLineSetup getOSBitnessSetup = new CommandLineSetup(osBitnessToolPath.getPath(), Collections.<CommandLineArgument>emptyList(), Collections.<CommandLineResource>emptyList());

    final List<ToolProvider> toolProviders = new ArrayList<ToolProvider>();
    myCtx.checking(new Expectations() {{
      oneOf(myRunnerParametersService).isRunningUnderWindows();
      will(returnValue(true));

      oneOf(myFileService).validatePath(osBitnessToolPath);

      oneOf(myCommandLineExecutor).runProcess(getOSBitnessSetup, RunAsToolProvider.EXECUTION_TIMEOUT_SECONDS);
      will(throwException(new ExecutionException("some details")));

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
      myRunnerParametersService,
      myFileService,
      myCommandLineExecutor);
  }
}
