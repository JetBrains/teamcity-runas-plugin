package jetbrains.buildServer.runAs.agent;

import com.intellij.openapi.diagnostic.Logger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import jetbrains.buildServer.dotNet.buildRunner.agent.LoggerService;
import jetbrains.buildServer.messages.serviceMessages.Message;
import org.jetbrains.annotations.NotNull;

public class ScopedFileAccessService implements FileAccessService {
  private static final Logger LOG = Logger.getInstance(ScopedFileAccessService.class.getName());
  static final String WARNING_PERMISSIONS_ERRORS = "Errors occurred while granting permissions";
  static final String MESSAGE_GRANTING_PERMISSIONS = "Granting necessary permissions";
  private final FileAccessService myFileAccessService;
  private final LoggerService myLoggerService;
  private final FileAccessCache myGlobalFileAccessCache;
  private final FileAccessCache myBuildFileAccessCache;

  public ScopedFileAccessService(
    @NotNull final FileAccessService fileAccessService,
    @NotNull final LoggerService loggerService,
    @NotNull final FileAccessCache globalFileAccessCache,
    @NotNull final FileAccessCache buildFileAccessCache) {
    myFileAccessService = fileAccessService;
    myLoggerService = loggerService;
    myGlobalFileAccessCache = globalFileAccessCache;
    myBuildFileAccessCache = buildFileAccessCache;
  }

  @Override
  public Iterable<Result<AccessControlEntry, Boolean>> setAccess(@NotNull final AccessControlList accessControlList) {
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
      return Collections.emptyList();
    }

    myLoggerService.onMessage(new Message(MESSAGE_GRANTING_PERMISSIONS, "NORMAL", null));
    List<Result<AccessControlEntry, Boolean>> results = new ArrayList<Result<AccessControlEntry, Boolean>>();
    boolean hasError = false;
    for (Result<AccessControlEntry, Boolean> result: myFileAccessService.setAccess(new AccessControlList(newAcl))) {
      results.add(result);
      hasError |= !result.isSuccessful() || (result.getValue() != null && !result.getValue());
    }

    if(hasError) {
      LOG.info(WARNING_PERMISSIONS_ERRORS);
      myLoggerService.onMessage(new Message(WARNING_PERMISSIONS_ERRORS, "WARNING", null));
    }

    return results;
  }
}