package jetbrains.buildServer.runAs.agent;

import jetbrains.buildServer.dotNet.buildRunner.agent.*;
import jetbrains.buildServer.util.positioning.PositionAware;
import jetbrains.buildServer.util.positioning.PositionConstraint;
import org.jetbrains.annotations.NotNull;

public class RunAsBuildCommandLineProcessorAdapter extends BuildCommandLineProcessorAdapterImpl implements PositionAware {

  public RunAsBuildCommandLineProcessorAdapter(
      @NotNull final CommandLineSetupBuilder setupBuilder,
      @NotNull final BuildRunnerContextProvider contextProvider,
      @NotNull final AgentEventDispatcher eventDispatcher,
      @NotNull final LoggerService logger,
      @NotNull final CommandLineArgumentsService commandLineArgumentsService) {
    super(setupBuilder, contextProvider, eventDispatcher, logger, commandLineArgumentsService);
  }

  @NotNull
  @Override
  public String getOrderId() {
    return "";
  }

  @NotNull
  @Override
  public PositionConstraint getConstraint() {
    return PositionConstraint.last();
  }
}
