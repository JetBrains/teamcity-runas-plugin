package jetbrains.buildServer.runAs.agent;

import org.jetbrains.annotations.NotNull;

public interface FileAccessService {
  public void setAccess(@NotNull final AccessControlList accessControlList);
}
