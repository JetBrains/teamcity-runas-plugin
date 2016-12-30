package jetbrains.buildServer.runAs.agent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class AccessControlListProviderImpl implements AccessControlListProvider {
  private final PathsService myPathsService;

  public AccessControlListProviderImpl(
    @NotNull final PathsService pathsService) {
    myPathsService = pathsService;
  }

  @NotNull
  @Override
  public AccessControlList getAfterAgentInitializedAcl() {
    return new AccessControlList(
      Arrays.asList(
        new AccessControlEntry(myPathsService.getPath(WellKnownPaths.Tools), AccessControlAccount.forAll(), EnumSet.of(AccessPermissions.AllowRead, AccessPermissions.AllowExecute, AccessPermissions.Recursive)),
        new AccessControlEntry(myPathsService.getPath(WellKnownPaths.Plugins), AccessControlAccount.forAll(), EnumSet.of(AccessPermissions.AllowRead, AccessPermissions.AllowExecute, AccessPermissions.Recursive)))
    );
  }

  @NotNull
  @Override
  public AccessControlList getBeforeBuildStepAcl(@NotNull final UserCredentials userCredentials) {
    final String username = userCredentials.getUser();
    final List<AccessControlEntry> acl = new ArrayList<AccessControlEntry>(
      Arrays.asList(
        new AccessControlEntry(myPathsService.getPath(WellKnownPaths.AgentTemp), AccessControlAccount.forUser(username), EnumSet.of(AccessPermissions.AllowRead, AccessPermissions.AllowWrite, AccessPermissions.AllowExecute, AccessPermissions.Recursive)),
        new AccessControlEntry(myPathsService.getPath(WellKnownPaths.BuildTemp), AccessControlAccount.forUser(username), EnumSet.of(AccessPermissions.AllowRead, AccessPermissions.AllowWrite, AccessPermissions.AllowExecute, AccessPermissions.Recursive)),
        new AccessControlEntry(myPathsService.getPath(WellKnownPaths.GlobalTemp), AccessControlAccount.forUser(username), EnumSet.of(AccessPermissions.AllowRead, AccessPermissions.AllowExecute, AccessPermissions.Recursive)),
        new AccessControlEntry(myPathsService.getPath(WellKnownPaths.Checkout), AccessControlAccount.forUser(username), EnumSet.of(AccessPermissions.AllowRead, AccessPermissions.AllowWrite, AccessPermissions.AllowExecute, AccessPermissions.Recursive)))
    );

    for (AccessControlEntry ace: userCredentials.getBeforeStepAcl()) {
      acl.add(ace);
    }

    return new AccessControlList(acl);
  }
}
