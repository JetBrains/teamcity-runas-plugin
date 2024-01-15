

package jetbrains.buildServer.runAs.agent;

import jetbrains.buildServer.ExecResult;
import org.jetbrains.annotations.NotNull;

public interface FileAccessService {
  public Iterable<Result<AccessControlEntry, Boolean>> setAccess(@NotNull final AccessControlList accessControlList);
}