

package jetbrains.buildServer.runAs.agent;

import org.jetbrains.annotations.NotNull;

public interface FileAccessCache {
  boolean tryAddEntry(@NotNull final AccessControlEntry acl);
}