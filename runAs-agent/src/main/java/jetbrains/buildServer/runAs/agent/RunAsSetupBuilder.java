package jetbrains.buildServer.runAs.agent;

import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineSetup;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineSetupBuilder;
import jetbrains.buildServer.dotNet.buildRunner.agent.RunnerParametersService;
import org.jetbrains.annotations.NotNull;

public class RunAsSetupBuilder implements CommandLineSetupBuilder {
  @NotNull private final Environment myEnvironment;
  @NotNull private final CommandLineSetupBuilder myRunAsWindowsSetupBuilder;
  @NotNull private final CommandLineSetupBuilder myRunAsUnixSetupBuilder;
  @NotNull private final CommandLineSetupBuilder myRunAsMacSetupBuilder;

  public RunAsSetupBuilder(
    @NotNull final Environment environment,
    @NotNull final CommandLineSetupBuilder runAsWindowsSetupBuilder,
    @NotNull final CommandLineSetupBuilder runAsUnixSetupBuilder,
    @NotNull final CommandLineSetupBuilder runAsMacSetupBuilder) {
    myEnvironment = environment;
    myRunAsWindowsSetupBuilder = runAsWindowsSetupBuilder;
    myRunAsUnixSetupBuilder = runAsUnixSetupBuilder;
    myRunAsMacSetupBuilder = runAsMacSetupBuilder;
  }

  @NotNull
  @Override
  public Iterable<CommandLineSetup> build(@NotNull final CommandLineSetup commandLineSetup) {
    CommandLineSetupBuilder setupBuilder = myRunAsUnixSetupBuilder;
    switch (myEnvironment.getOperationSystem()) {
      case Windows:
        setupBuilder = myRunAsWindowsSetupBuilder;
        break;

      case Mac:
        setupBuilder = myRunAsMacSetupBuilder;
        break;
    }

    return setupBuilder.build(commandLineSetup);
  }
}
