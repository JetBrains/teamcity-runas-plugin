package jetbrains.buildServer.runAs.agent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface AccessControlListProvider {
  @NotNull
  AccessControlList getAfterAgentInitializedAcl(@Nullable final String additionalAcl);

  @NotNull
  AccessControlList getBeforeBuildStepAcl(@NotNull final UserCredentials userCredentials);
}
