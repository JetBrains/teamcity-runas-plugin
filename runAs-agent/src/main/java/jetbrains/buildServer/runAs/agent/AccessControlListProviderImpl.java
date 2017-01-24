package jetbrains.buildServer.runAs.agent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import jetbrains.buildServer.dotNet.buildRunner.agent.TextParser;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AccessControlListProviderImpl implements AccessControlListProvider {
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
    final List<AccessControlEntry> acl = new ArrayList<AccessControlEntry>(
      Arrays.asList(
        new AccessControlEntry(myPathsService.getPath(WellKnownPaths.Work), AccessControlAccount.forAll(), EnumSet.of(AccessPermissions.AllowRead, AccessPermissions.Recursive)),
        new AccessControlEntry(myPathsService.getPath(WellKnownPaths.System), AccessControlAccount.forAll(), EnumSet.of(AccessPermissions.AllowRead, AccessPermissions.AllowWrite, AccessPermissions.AllowExecute, AccessPermissions.Recursive)),
        new AccessControlEntry(myPathsService.getPath(WellKnownPaths.Tools), AccessControlAccount.forAll(), EnumSet.of(AccessPermissions.AllowRead, AccessPermissions.AllowExecute, AccessPermissions.Recursive)),
        new AccessControlEntry(myPathsService.getPath(WellKnownPaths.Plugins), AccessControlAccount.forAll(), EnumSet.of(AccessPermissions.AllowRead, AccessPermissions.AllowExecute, AccessPermissions.Recursive)),
        new AccessControlEntry(myPathsService.getPath(WellKnownPaths.Lib), AccessControlAccount.forAll(), EnumSet.of(AccessPermissions.AllowRead, AccessPermissions.AllowExecute, AccessPermissions.Recursive)))
    );

    if(!StringUtil.isEmptyOrSpaces(additionalAcl)) {
      for (AccessControlEntry ace : myFileAccessParser.parse(additionalAcl)) {
        acl.add(ace);
      }
    }

    return new AccessControlList(acl);
  }

  @NotNull
  @Override
  public AccessControlList getBeforeBuildStepAcl(@NotNull final UserCredentials userCredentials) {
    final String username = userCredentials.getUser();
    final List<AccessControlEntry> acl = new ArrayList<AccessControlEntry>(
      Arrays.asList(
        new AccessControlEntry(myPathsService.getPath(WellKnownPaths.Config), AccessControlAccount.forUser(username), EnumSet.of(AccessPermissions.DenyRead, AccessPermissions.DenyWrite, AccessPermissions.DenyExecute, AccessPermissions.Recursive)),
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
