package jetbrains.buildServer.runAs.agent;

import java.util.List;
import jetbrains.buildServer.agent.AgentLifeCycleAdapter;
import jetbrains.buildServer.agent.AgentLifeCycleListener;
import jetbrains.buildServer.dotNet.buildRunner.agent.RunnerParametersService;
import jetbrains.buildServer.util.positioning.PositionAware;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface SecuredParametersService extends RunnerParametersService, BuildFeatureParameters, AgentLifeCycleListener, PositionAware {
}