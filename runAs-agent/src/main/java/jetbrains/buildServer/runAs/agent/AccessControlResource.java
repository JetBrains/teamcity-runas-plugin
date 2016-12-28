package jetbrains.buildServer.runAs.agent;

import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineResource;
import org.jetbrains.annotations.NotNull;

public interface AccessControlResource extends CommandLineResource {
  public void addEntry(@NotNull final AccessControlEntry accessControlEntry);
}
