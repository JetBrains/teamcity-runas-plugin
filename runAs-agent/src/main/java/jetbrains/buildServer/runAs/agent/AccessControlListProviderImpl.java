package jetbrains.buildServer.runAs.agent;

import com.intellij.openapi.diagnostic.Logger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import jetbrains.buildServer.dotNet.buildRunner.agent.TextParser;
import jetbrains.buildServer.runAs.common.Constants;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AccessControlListProviderImpl implements AccessControlListProvider {
  @NotNull private static final Logger LOG = Logger.getInstance(AccessControlListProviderImpl.class.getName());
  @NotNull private final PathsService myPathsService;
  @NotNull private final TextParser<AccessControlList> myFileAccessParser;
  @NotNull private final AgentParametersService myAgentParametersService;
  @NotNull private final ProfileParametersService myProfileParametersService;
  @Nullable private List<AccessControlEntry> myDefaultAcl;

  public AccessControlListProviderImpl(
    @NotNull final PathsService pathsService,
    @NotNull final TextParser<AccessControlList> fileAccessParser,
    @NotNull final AgentParametersService agentParametersService,
    @NotNull final ProfileParametersService profileParametersService) {
    myPathsService = pathsService;
    myFileAccessParser = fileAccessParser;
    myAgentParametersService = agentParametersService;
    myProfileParametersService = profileParametersService;
  }

  @NotNull
  @Override
  public AccessControlList getAcl(@NotNull final UserCredentials userCredentials) {
    final List<AccessControlEntry> aceList = new ArrayList<AccessControlEntry>();

    final boolean isAclDefaultsEnabled = ParameterUtils.parseBoolean(myAgentParametersService.tryGetConfigParameter(Constants.RUN_AS_ACL_DEFAULTS_ENABLED), false);
    if(isAclDefaultsEnabled) {
      for (AccessControlEntry ace : getDefaultAcl(userCredentials.getUser())) {
        aceList.add(ace);
      }
    }

    appendAcl(aceList, myAgentParametersService.tryGetConfigParameter(Constants.RUN_AS_ACL));
    appendAcl(aceList, myProfileParametersService.tryGetProperty(userCredentials.getProfile(), Constants.RUN_AS_ACL));

    final AccessControlList acl = new AccessControlList(aceList);
    if(LOG.isDebugEnabled()) {
      LOG.debug("getAcl: " + acl);
    }

    return acl;
  }

  private void appendAcl(final List<AccessControlEntry> aceList, final String agentAclStr) {
    if (!StringUtil.isEmptyOrSpaces(agentAclStr)) {
      for (AccessControlEntry ace : myFileAccessParser.parse(agentAclStr)) {
        aceList.add(ace);
      }
    }
  }

  private List<AccessControlEntry> getDefaultAcl(@NotNull final String username)
  {
    if(myDefaultAcl == null) {
      myDefaultAcl = Arrays.asList(
        new AccessControlEntry(myPathsService.getPath(WellKnownPaths.Work), AccessControlAccount.forAll(), EnumSet.of(AccessPermissions.GrantRead), AccessControlScope.Global),
        new AccessControlEntry(myPathsService.getPath(WellKnownPaths.Tools), AccessControlAccount.forAll(), EnumSet.of(AccessPermissions.GrantRead, AccessPermissions.GrantExecute, AccessPermissions.Recursive), AccessControlScope.Global),
        new AccessControlEntry(myPathsService.getPath(WellKnownPaths.Plugins), AccessControlAccount.forAll(), EnumSet.of(AccessPermissions.GrantRead, AccessPermissions.GrantExecute, AccessPermissions.Recursive), AccessControlScope.Global),
        new AccessControlEntry(myPathsService.getPath(WellKnownPaths.Lib), AccessControlAccount.forAll(), EnumSet.of(AccessPermissions.GrantRead, AccessPermissions.GrantExecute, AccessPermissions.Recursive), AccessControlScope.Global),
        new AccessControlEntry(myPathsService.getPath(WellKnownPaths.Config), AccessControlAccount.forUser(username), EnumSet.of(AccessPermissions.DenyRead, AccessPermissions.DenyWrite, AccessPermissions.DenyExecute, AccessPermissions.Recursive), AccessControlScope.Build),
        new AccessControlEntry(myPathsService.getPath(WellKnownPaths.Checkout), AccessControlAccount.forUser(username), EnumSet.of(AccessPermissions.GrantRead, AccessPermissions.GrantWrite, AccessPermissions.GrantExecute, AccessPermissions.Recursive), AccessControlScope.Build),
        new AccessControlEntry(myPathsService.getPath(WellKnownPaths.Log), AccessControlAccount.forUser(username), EnumSet.of(AccessPermissions.DenyRead, AccessPermissions.DenyWrite, AccessPermissions.DenyExecute, AccessPermissions.Recursive), AccessControlScope.Step),
        new AccessControlEntry(myPathsService.getPath(WellKnownPaths.System), AccessControlAccount.forUser(username), EnumSet.of(AccessPermissions.GrantRead, AccessPermissions.GrantWrite, AccessPermissions.GrantExecute, AccessPermissions.Recursive), AccessControlScope.Step),
        new AccessControlEntry(myPathsService.getPath(WellKnownPaths.AgentTemp), AccessControlAccount.forUser(username), EnumSet.of(AccessPermissions.GrantRead, AccessPermissions.GrantWrite, AccessPermissions.GrantExecute, AccessPermissions.Recursive), AccessControlScope.Step),
        new AccessControlEntry(myPathsService.getPath(WellKnownPaths.BuildTemp), AccessControlAccount.forUser(username), EnumSet.of(AccessPermissions.GrantRead, AccessPermissions.GrantWrite, AccessPermissions.GrantExecute, AccessPermissions.Recursive), AccessControlScope.Step),
        new AccessControlEntry(myPathsService.getPath(WellKnownPaths.GlobalTemp), AccessControlAccount.forUser(username), EnumSet.of(AccessPermissions.GrantRead, AccessPermissions.GrantExecute, AccessPermissions.Recursive), AccessControlScope.Step));
    }

    return myDefaultAcl;
  }
}
