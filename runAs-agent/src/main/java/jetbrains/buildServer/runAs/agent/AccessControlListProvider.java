package jetbrains.buildServer.runAs.agent;

import org.jetbrains.annotations.NotNull;

public interface AccessControlListProvider {
  @NotNull
  AccessControlList getAfterAgentInitializedAcl();

  @NotNull
  AccessControlList getBeforeBuildStepAcl(@NotNull final UserCredentials userCredentials);
}
