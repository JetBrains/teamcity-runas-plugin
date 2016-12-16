package jetbrains.buildServer.runAs.agent;

import org.jetbrains.annotations.Nullable;

public interface UserCredentialsService {
  @Nullable
  UserCredentials tryGetUserCredentials();
}
