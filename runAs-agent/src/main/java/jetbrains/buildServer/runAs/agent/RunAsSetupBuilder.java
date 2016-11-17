package jetbrains.buildServer.runAs.agent;

import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineSetup;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineSetupBuilder;
import jetbrains.buildServer.dotNet.buildRunner.agent.RunnerParametersService;
import org.jetbrains.annotations.NotNull;

public class RunAsSetupBuilder implements CommandLineSetupBuilder {
  @NotNull private final RunnerParametersService myRunnerParametersService;
  @NotNull private final CommandLineSetupBuilder myRunAsWindowsSetupBuilder;
  @NotNull private final CommandLineSetupBuilder myRunAsUnixSetupBuilder;

  public RunAsSetupBuilder(
    @NotNull final RunnerParametersService runnerParametersService,
    @NotNull final CommandLineSetupBuilder runAsWindowsSetupBuilder,
    @NotNull final CommandLineSetupBuilder runAsUnixSetupBuilder) {
    myRunnerParametersService = runnerParametersService;
    myRunAsWindowsSetupBuilder = runAsWindowsSetupBuilder;
    myRunAsUnixSetupBuilder = runAsUnixSetupBuilder;
  }

  @NotNull
  @Override
  public Iterable<CommandLineSetup> build(@NotNull final CommandLineSetup commandLineSetup) {
    if(myRunnerParametersService.isRunningUnderWindows()) {
      return myRunAsWindowsSetupBuilder.build(commandLineSetup);
    }

    return myRunAsUnixSetupBuilder.build(commandLineSetup);
  }
}
