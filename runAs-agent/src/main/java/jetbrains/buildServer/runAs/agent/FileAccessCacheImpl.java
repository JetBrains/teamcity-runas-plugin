

package jetbrains.buildServer.runAs.agent;

import java.util.HashSet;
import org.jetbrains.annotations.NotNull;

public class FileAccessCacheImpl implements FileAccessCache, FileAccessCacheManager {
  @SuppressWarnings("SpellCheckingInspection")
  private final HashSet<AccessControlEntry> myAcls = new HashSet<AccessControlEntry>();

  @Override
  public boolean tryAddEntry(@NotNull final AccessControlEntry acl) {
    return myAcls.add(acl);
  }

  @Override
  public void reset() {
    myAcls.clear();
  }
}