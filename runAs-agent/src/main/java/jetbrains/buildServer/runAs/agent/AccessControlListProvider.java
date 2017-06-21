package jetbrains.buildServer.runAs.agent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface AccessControlListProvider {
  @NotNull
  AccessControlList getAcl(@NotNull final UserCredentials userCredentials);
}
