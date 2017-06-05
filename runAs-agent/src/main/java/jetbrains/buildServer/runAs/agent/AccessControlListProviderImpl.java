package jetbrains.buildServer.runAs.agent;

import com.intellij.openapi.diagnostic.Logger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import jetbrains.buildServer.dotNet.buildRunner.agent.TextParser;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AccessControlListProviderImpl implements AccessControlListProvider {
  private static final Logger LOG = Logger.getInstance(AccessControlListProviderImpl.class.getName());
  private final PathsService myPathsService;
  private final TextParser<AccessControlList> myFileAccessParser;

  public AccessControlListProviderImpl(
    @NotNull final PathsService pathsService,
    @NotNull final TextParser<AccessControlList> fileAccessParser) {
    myPathsService = pathsService;
    myFileAccessParser = fileAccessParser;
  }

  @NotNull
  @Override
  public AccessControlList getAfterAgentInitializedAcl(@Nullable final String additionalAcl) {
    final List<AccessControlEntry> aceList = new ArrayList<AccessControlEntry>(
      Arrays.asList(
        new AccessControlEntry(myPathsService.getPath(WellKnownPaths.Work), AccessControlAccount.forAll(), EnumSet.of(AccessPermissions.GrantRead)),
        new AccessControlEntry(myPathsService.getPath(WellKnownPaths.Tools), AccessControlAccount.forAll(), EnumSet.of(AccessPermissions.GrantRead, AccessPermissions.GrantExecute, AccessPermissions.Recursive)),
        new AccessControlEntry(myPathsService.getPath(WellKnownPaths.Plugins), AccessControlAccount.forAll(), EnumSet.of(AccessPermissions.GrantRead, AccessPermissions.GrantExecute, AccessPermissions.Recursive)),
        new AccessControlEntry(myPathsService.getPath(WellKnownPaths.Lib), AccessControlAccount.forAll(), EnumSet.of(AccessPermissions.GrantRead, AccessPermissions.GrantExecute, AccessPermissions.Recursive)))
    );

    if(!StringUtil.isEmptyOrSpaces(additionalAcl)) {
      for (AccessControlEntry ace : myFileAccessParser.parse(additionalAcl)) {
        aceList.add(ace);
      }
    }

    final AccessControlList acl = new AccessControlList(aceList);
    if(LOG.isDebugEnabled()) {
      LOG.debug("getAfterAgentInitializedAcl: " + acl);
    }

    return acl;
  }

  @NotNull
  @Override
  public AccessControlList getBeforeBuildStepAcl(@NotNull final UserCredentials userCredentials) {
    final String username = userCredentials.getUser();
    final AccessControlEntry checkoutAce = new AccessControlEntry(myPathsService.getPath(WellKnownPaths.Checkout), AccessControlAccount.forUser(username), EnumSet.of(AccessPermissions.GrantRead, AccessPermissions.GrantWrite, AccessPermissions.GrantExecute, AccessPermissions.Recursive));
    checkoutAce.setCachingAllowed(true);
    final List<AccessControlEntry> aceList = new ArrayList<AccessControlEntry>(
      Arrays.asList(
        new AccessControlEntry(myPathsService.getPath(WellKnownPaths.Config), AccessControlAccount.forUser(username), EnumSet.of(AccessPermissions.DenyRead, AccessPermissions.DenyWrite, AccessPermissions.DenyExecute, AccessPermissions.Recursive)),
        new AccessControlEntry(myPathsService.getPath(WellKnownPaths.Log), AccessControlAccount.forUser(username), EnumSet.of(AccessPermissions.DenyRead, AccessPermissions.DenyWrite, AccessPermissions.DenyExecute, AccessPermissions.Recursive)),
        new AccessControlEntry(myPathsService.getPath(WellKnownPaths.System), AccessControlAccount.forUser(username), EnumSet.of(AccessPermissions.GrantRead, AccessPermissions.GrantWrite, AccessPermissions.GrantExecute, AccessPermissions.Recursive)),
        new AccessControlEntry(myPathsService.getPath(WellKnownPaths.AgentTemp), AccessControlAccount.forUser(username), EnumSet.of(AccessPermissions.GrantRead, AccessPermissions.GrantWrite, AccessPermissions.GrantExecute, AccessPermissions.Recursive)),
        new AccessControlEntry(myPathsService.getPath(WellKnownPaths.BuildTemp), AccessControlAccount.forUser(username), EnumSet.of(AccessPermissions.GrantRead, AccessPermissions.GrantWrite, AccessPermissions.GrantExecute, AccessPermissions.Recursive)),
        new AccessControlEntry(myPathsService.getPath(WellKnownPaths.GlobalTemp), AccessControlAccount.forUser(username), EnumSet.of(AccessPermissions.GrantRead, AccessPermissions.GrantExecute, AccessPermissions.Recursive)),
        checkoutAce)
    );

    for (AccessControlEntry ace: userCredentials.getBeforeStepAcl()) {
      aceList.add(ace);
    }

    final AccessControlList acl = new AccessControlList(aceList);
    if(LOG.isDebugEnabled()) {
      LOG.debug("getBeforeBuildStepAcl: " + acl);
    }

    return acl;
  }
}
