package jetbrains.buildServer.runAs.agent;

import java.io.File;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface FileAccessService {
  public void setAccess(@NotNull final AccessControlList accessControlList);
}
