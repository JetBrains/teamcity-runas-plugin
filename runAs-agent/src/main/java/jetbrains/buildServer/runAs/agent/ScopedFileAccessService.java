package jetbrains.buildServer.runAs.agent;

import com.intellij.openapi.diagnostic.Logger;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class ScopedFileAccessService implements FileAccessService {
  private static final Logger LOG = Logger.getInstance(ScopedFileAccessService.class.getName());
  private final FileAccessService myFileAccessService;
  private final FileAccessCache myGlobalFileAccessCache;
  private final FileAccessCache myBuildFileAccessCache;

  public ScopedFileAccessService(
    @NotNull final FileAccessService fileAccessService,
    @NotNull final FileAccessCache globalFileAccessCache,
    @NotNull final FileAccessCache buildFileAccessCache) {
    myFileAccessService = fileAccessService;
    myGlobalFileAccessCache = globalFileAccessCache;
    myBuildFileAccessCache = buildFileAccessCache;
  }

  @Override
  public void setAccess(@NotNull final AccessControlList accessControlList) {
    final List<AccessControlEntry> newAcl = new ArrayList<AccessControlEntry>();
    for (AccessControlEntry ace : accessControlList) {
      switch (ace.getScope()) {
        case Global:
          if (!myGlobalFileAccessCache.tryAddEntry(ace)) {
            LOG.info("Skipping setting an access for " + ace + ", because it has being done previously");
            continue;
          }

          break;

        case Build:
          if (!myBuildFileAccessCache.tryAddEntry(ace)) {
            LOG.info("Skipping setting an access for " + ace + ", because it has being done on the previous step");
            continue;
          }

          break;
      }

      newAcl.add(ace);
    }

    if(newAcl.size() == 0) {
      return;
    }

    myFileAccessService.setAccess(new AccessControlList(newAcl));
  }
}