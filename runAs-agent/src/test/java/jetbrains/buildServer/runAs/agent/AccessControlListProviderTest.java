package jetbrains.buildServer.runAs.agent;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineArgument;
import jetbrains.buildServer.runAs.common.LoggingLevel;
import jetbrains.buildServer.runAs.common.WindowsIntegrityLevel;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.BDDAssertions.then;

public class AccessControlListProviderTest {
  private Mockery myCtx;
  private PathsService myPathsService;

  @BeforeMethod
  public void setUp()
  {
    myCtx = new Mockery();
    myPathsService = myCtx.mock(PathsService.class);
  }

  @Test()
  public void shouldGetBeforeBuildStepAcl() throws IOException {
    // Given
    final String username = "user";
    final File checkout = new File("checkout");
    final File agentTemp = new File("agentTemp");
    final File buildTemp = new File("buildTemp");
    final File globalTemp = new File("globalTemp");
    final File custom1 = new File("custom1");
    final File custom2 = new File("custom2");
    final AccessControlEntry baseAce1 = new AccessControlEntry(custom1, AccessControlAccount.forUser(username), EnumSet.of(AccessPermissions.AllowRead, AccessPermissions.AllowExecute, AccessPermissions.Recursive));
    final AccessControlEntry baseAce2 = new AccessControlEntry(custom2, AccessControlAccount.forUser(username), EnumSet.of(AccessPermissions.AllowRead));
    final AccessControlList baseAccessControlList = new AccessControlList(Arrays.asList(baseAce1, baseAce2));
    UserCredentials userCredentials = new UserCredentials(
      username,
      "password78",
      WindowsIntegrityLevel.High,
      LoggingLevel.Debug,
      Arrays.asList(new CommandLineArgument("arg1", CommandLineArgument.Type.PARAMETER), new CommandLineArgument("arg2", CommandLineArgument.Type.PARAMETER)),
      baseAccessControlList);

    myCtx.checking(new Expectations() {{
      oneOf(myPathsService).getPath(WellKnownPaths.Checkout);
      will(returnValue(checkout));

      oneOf(myPathsService).getPath(WellKnownPaths.AgentTemp);
      will(returnValue(agentTemp));

      oneOf(myPathsService).getPath(WellKnownPaths.BuildTemp);
      will(returnValue(buildTemp));

      oneOf(myPathsService).getPath(WellKnownPaths.GlobalTemp);
      will(returnValue(globalTemp));
    }});

    final AccessControlListProvider instance = createInstance();

    // When
    final AccessControlList actualAcl = instance.getBeforeBuildStepAcl(userCredentials);

    // Then
    myCtx.assertIsSatisfied();
    then(actualAcl).isEqualTo(new AccessControlList(Arrays.asList(
      new AccessControlEntry(checkout, AccessControlAccount.forUser(username), EnumSet.of(AccessPermissions.AllowRead, AccessPermissions.AllowWrite, AccessPermissions.AllowExecute, AccessPermissions.Recursive)),
      new AccessControlEntry(agentTemp, AccessControlAccount.forUser(username), EnumSet.of(AccessPermissions.AllowRead, AccessPermissions.AllowWrite, AccessPermissions.AllowExecute, AccessPermissions.Recursive)),
      new AccessControlEntry(buildTemp, AccessControlAccount.forUser(username), EnumSet.of(AccessPermissions.AllowRead, AccessPermissions.AllowWrite, AccessPermissions.AllowExecute, AccessPermissions.Recursive)),
      new AccessControlEntry(globalTemp, AccessControlAccount.forUser(username), EnumSet.of(AccessPermissions.AllowRead, AccessPermissions.AllowExecute, AccessPermissions.Recursive)),
      baseAce1,
      baseAce2)));
  }

  @Test()
  public void shouldGetAfterAgentInitializedAcl() throws IOException {
    // Given
    final File tools = new File("tools");
    final File plugins = new File("plugins");
    myCtx.checking(new Expectations() {{
      oneOf(myPathsService).getPath(WellKnownPaths.Tools);
      will(returnValue(tools));

      oneOf(myPathsService).getPath(WellKnownPaths.Plugins);
      will(returnValue(plugins));
    }});

    final AccessControlListProvider instance = createInstance();

    // When
    final AccessControlList actualAcl = instance.getAfterAgentInitializedAcl();

    // Then
    myCtx.assertIsSatisfied();
    then(actualAcl).isEqualTo(new AccessControlList(Arrays.asList(
      new AccessControlEntry(tools, AccessControlAccount.forAll(), EnumSet.of(AccessPermissions.AllowRead, AccessPermissions.AllowExecute, AccessPermissions.Recursive)),
      new AccessControlEntry(plugins, AccessControlAccount.forAll(), EnumSet.of(AccessPermissions.AllowRead, AccessPermissions.AllowExecute, AccessPermissions.Recursive)))));
  }

  @NotNull
  private AccessControlListProvider createInstance()
  {
    return new AccessControlListProviderImpl(myPathsService);
  }
}
