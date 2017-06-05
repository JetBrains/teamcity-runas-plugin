package jetbrains.buildServer.runAs.agent;

import com.intellij.openapi.diagnostic.Logger;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

import static jetbrains.buildServer.runAs.common.Constants.RUN_AS_ACL_CACHING_ENABLED;

public class CachingFileAccessService implements FileAccessService {
  private static final Logger LOG = Logger.getInstance(CachingFileAccessService.class.getName());
  private final FileAccessService myFileAccessService;
  private final FileAccessCache myFileAccessCache;
  private final ParametersService myParametersService;

  public CachingFileAccessService(
    @NotNull final FileAccessService fileAccessService,
    @NotNull final FileAccessCache fileAccessCache,
    @NotNull final ParametersService parametersService) {
    myFileAccessService = fileAccessService;
    myFileAccessCache = fileAccessCache;
    myParametersService = parametersService;
  }

  @Override
  public void setAccess(@NotNull final AccessControlList accessControlList) {
    if(!ParameterUtils.parseBoolean(myParametersService.tryGetConfigParameter(RUN_AS_ACL_CACHING_ENABLED), true)) {
      myFileAccessService.setAccess(accessControlList);
      return;
    }

    final List<AccessControlEntry> newAcl = new ArrayList<AccessControlEntry>();
    for (AccessControlEntry ace : accessControlList) {
      if (ace.isCachingAllowed() && !myFileAccessCache.tryAddEntry(ace)) {
        LOG.info("Skipping setting an access for " + ace + ", because it has being done in the previous step");
        continue;
      }

      newAcl.add(ace);
    }

    if(newAcl.size() == 0) {
      return;
    }

    myFileAccessService.setAccess(new AccessControlList(newAcl));
  }
}