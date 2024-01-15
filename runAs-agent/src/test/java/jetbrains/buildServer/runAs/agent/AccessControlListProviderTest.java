

package jetbrains.buildServer.runAs.agent;

import java.io.File;
import java.io.IOException;
import java.util.*;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineArgument;
import jetbrains.buildServer.dotNet.buildRunner.agent.TextParser;
import jetbrains.buildServer.runAs.common.LoggingLevel;
import jetbrains.buildServer.runAs.common.WindowsIntegrityLevel;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static jetbrains.buildServer.runAs.common.Constants.RUN_AS_ACL;
import static jetbrains.buildServer.runAs.common.Constants.RUN_AS_ACL_DEFAULTS_ENABLED;
import static org.assertj.core.api.BDDAssertions.then;

public class AccessControlListProviderTest {
  private Mockery myCtx;
  private PathsService myPathsService;
  private TextParser<AccessControlList> myFileAccessParser;
  private AgentParametersService myAgentParametersService;
  private ProfileParametersService myProfileParametersService;

  @BeforeMethod
  public void setUp()
  {
    myCtx = new Mockery();
    myPathsService = myCtx.mock(PathsService.class);
    //noinspection unchecked
    myFileAccessParser = (TextParser<AccessControlList>)myCtx.mock(TextParser.class);
    myAgentParametersService = myCtx.mock(AgentParametersService.class);
    myProfileParametersService = myCtx.mock(ProfileParametersService.class);
  }

  @DataProvider(name = "getAclCases")
  public Object[][] getAclCases() {
    return new Object[][] {
      // Empty agent acl and empty profile acl
      {"true", "", null, null, null, null},

      // Agent acl and empty profile acl
      {
        "True",
        "",
        "agent acl",
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("someFile1"), AccessControlAccount.forAll(), EnumSet.of(AccessPermissions.GrantRead), AccessControlScope.Global))),
        null,
        null},

      // Empty agent acl and profile acl
      {
        "true",
        "myProf",
        null,
        null,
        "prof acl",
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("someFile1"), AccessControlAccount.forAll(), EnumSet.of(AccessPermissions.GrantRead), AccessControlScope.Global)))},

      // Agent acl and profile acl
      {
        "true",
        "myProf",
        "agent acl",
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("someFile1"), AccessControlAccount.forAll(), EnumSet.of(AccessPermissions.GrantRead), AccessControlScope.Global))),
        "prof acl",
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("someFile2"), AccessControlAccount.forAll(), EnumSet.of(AccessPermissions.GrantRead), AccessControlScope.Global)))},

      // Agent acl and profile acl and without default acl
      {
        "false",
        "myProf",
        "agent acl",
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("someFile1"), AccessControlAccount.forAll(), EnumSet.of(AccessPermissions.GrantRead), AccessControlScope.Global))),
        "prof acl",
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("someFile2"), AccessControlAccount.forAll(), EnumSet.of(AccessPermissions.GrantRead), AccessControlScope.Global)))},

      // Agent acl and profile acl and default acl by default :)
      {
        null,
        "myProf",
        "agent acl",
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("someFile1"), AccessControlAccount.forAll(), EnumSet.of(AccessPermissions.GrantRead), AccessControlScope.Global))),
        "prof acl",
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("someFile2"), AccessControlAccount.forAll(), EnumSet.of(AccessPermissions.GrantRead), AccessControlScope.Global)))},

      // Agent acl and profile acl and default acl by default 2 :)
      {
        "",
        "myProf",
        "agent acl",
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("someFile1"), AccessControlAccount.forAll(), EnumSet.of(AccessPermissions.GrantRead), AccessControlScope.Global))),
        "prof acl",
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("someFile2"), AccessControlAccount.forAll(), EnumSet.of(AccessPermissions.GrantRead), AccessControlScope.Global)))},
    };
  }

  @Test(dataProvider = "getAclCases")
  public void shouldGetAcl(
    @Nullable final String isDefaultsAclEnabledStr,
    @NotNull final String profile,
    @Nullable final String agentAclStr,
    @Nullable final AccessControlList agentAcl,
    @Nullable final String profileAclStr,
    @Nullable final AccessControlList profileAcl) throws IOException {
    // Given
    final String username = "user";
    final File work = new File("work");
    final File tools = new File("tools");
    final File plugins = new File("plugins");
    final File lib = new File("lib");
    final File config = new File("config");
    final File log = new File("log");
    final File checkout = new File("checkout");
    final File system = new File("system");
    final File agentTemp = new File("agentTemp");
    final File buildTemp = new File("buildTemp");
    final File globalTemp = new File("globalTemp");
    UserCredentials userCredentials = new UserCredentials(
      profile,
      username,
      "password78",
      WindowsIntegrityLevel.High,
      LoggingLevel.Debug,
      Arrays.asList(new CommandLineArgument("arg1", CommandLineArgument.Type.PARAMETER), new CommandLineArgument("arg2", CommandLineArgument.Type.PARAMETER)));

    myCtx.checking(new Expectations() {{
      configureExpectations();
    }

      void configureExpectations() {
        one(myAgentParametersService).tryGetConfigParameter(RUN_AS_ACL_DEFAULTS_ENABLED);
        will(returnValue(isDefaultsAclEnabledStr));

        allowing(myPathsService).getPath(WellKnownPaths.Work);
        will(returnValue(work));

        allowing(myPathsService).getPath(WellKnownPaths.Log);
        will(returnValue(log));

        allowing(myPathsService).getPath(WellKnownPaths.Tools);
        will(returnValue(tools));

        allowing(myPathsService).getPath(WellKnownPaths.Plugins);
        will(returnValue(plugins));

        allowing(myPathsService).getPath(WellKnownPaths.Lib);
        will(returnValue(lib));

        allowing(myPathsService).getPath(WellKnownPaths.Config);
        will(returnValue(config));

        allowing(myPathsService).getPath(WellKnownPaths.Log);
        will(returnValue(log));

        allowing(myPathsService).getPath(WellKnownPaths.Checkout);
        will(returnValue(checkout));

        allowing(myPathsService).getPath(WellKnownPaths.System);
        will(returnValue(system));

        allowing(myPathsService).getPath(WellKnownPaths.AgentTemp);
        will(returnValue(agentTemp));

        allowing(myPathsService).getPath(WellKnownPaths.BuildTemp);
        will(returnValue(buildTemp));

        allowing(myPathsService).getPath(WellKnownPaths.GlobalTemp);
        will(returnValue(globalTemp));

        allowing(myAgentParametersService).tryGetConfigParameter(RUN_AS_ACL);
        will(returnValue(agentAclStr));

        if(agentAclStr != null) {
          allowing(myFileAccessParser).parse(agentAclStr);
          will(returnValue(agentAcl));
        }

        allowing(myProfileParametersService).tryGetProperty(profile, RUN_AS_ACL);
        will(returnValue(profileAclStr));

        if(profileAclStr != null) {
          allowing(myFileAccessParser).parse(profileAclStr);
          will(returnValue(profileAcl));
        }
      }
    });

    final AccessControlListProvider instance = createInstance();

    // When
    final AccessControlList actualAcl = instance.getAcl(userCredentials);

    // Then
    myCtx.assertIsSatisfied();
    then(actualAcl).isEqualTo(getExpectedAcl(isDefaultsAclEnabledStr, username, agentAcl, profileAcl));
  }

  @NotNull
  private AccessControlListProvider createInstance()
  {
    return new AccessControlListProviderImpl(
      myPathsService,
      myFileAccessParser,
      myAgentParametersService,
      myProfileParametersService);
  }

  private AccessControlList getExpectedAcl(
    @Nullable final String isDefaultsAclEnabledStr,
    @NotNull final String username,
    @Nullable final AccessControlList agentAcl,
    @Nullable final AccessControlList profileAcl)
  {
    final List<AccessControlEntry> acl = new ArrayList<AccessControlEntry>();

    final boolean isDefaultsAclEnabled = !StringUtil.isEmptyOrSpaces(isDefaultsAclEnabledStr) && isDefaultsAclEnabledStr.toLowerCase().equals("true");
    if(isDefaultsAclEnabled) {
      final List<AccessControlEntry> defaultAcl = Arrays.asList(
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

      acl.addAll(defaultAcl);
    }

    if(agentAcl != null) {
      for (AccessControlEntry ace: agentAcl) {
        acl.add(ace);
      }
    }

    if(profileAcl != null) {
      for (AccessControlEntry ace: profileAcl) {
        acl.add(ace);
      }
    }

    return new AccessControlList(acl);
  }
}