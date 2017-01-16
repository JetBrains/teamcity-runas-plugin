package jetbrains.buildServer.runAs.agent;

import com.intellij.execution.ExecutionException;
import java.io.File;
import java.util.Arrays;
import java.util.EnumSet;
import jetbrains.buildServer.dotNet.buildRunner.agent.BuildStartException;
import jetbrains.buildServer.dotNet.buildRunner.agent.TextParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.assertj.core.api.BDDAssertions.then;

public class FileAccessParserTest {
  private Mockery myCtx;
  private PathsService myPathsService;
  private File myAgentHomeDirectory;

  @BeforeMethod
  public void setUp()
  {
    myCtx = new Mockery();
    myPathsService = myCtx.mock(PathsService.class);
    myAgentHomeDirectory = new File("AgentHome");
  }

  @DataProvider(name = "parseAclCases")
  public Object[][] getParseAclCasesCases() {
    return new Object[][] {
      {
        "rc+rwx,dir1,dir2",
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("dir1"), AccessControlAccount.forCurrent(), EnumSet.of(AccessPermissions.AllowRead, AccessPermissions.AllowWrite, AccessPermissions.AllowExecute, AccessPermissions.Recursive)),
          new AccessControlEntry(new File("dir2"), AccessControlAccount.forCurrent(), EnumSet.of(AccessPermissions.AllowRead, AccessPermissions.AllowWrite, AccessPermissions.AllowExecute, AccessPermissions.Recursive))
        )),
        false},

      // with spaces
      {
        " r c + r wx , dir1 , dir2 ",
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("dir1"), AccessControlAccount.forCurrent(), EnumSet.of(AccessPermissions.AllowRead, AccessPermissions.AllowWrite, AccessPermissions.AllowExecute, AccessPermissions.Recursive)),
          new AccessControlEntry(new File("dir2"), AccessControlAccount.forCurrent(), EnumSet.of(AccessPermissions.AllowRead, AccessPermissions.AllowWrite, AccessPermissions.AllowExecute, AccessPermissions.Recursive))
        )),
        false},

      // for all users
      {
        "ru+rwx,dir1,dir2",
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("dir1"), AccessControlAccount.forUser(""), EnumSet.of(AccessPermissions.AllowRead, AccessPermissions.AllowWrite, AccessPermissions.AllowExecute, AccessPermissions.Recursive)),
          new AccessControlEntry(new File("dir2"), AccessControlAccount.forUser(""), EnumSet.of(AccessPermissions.AllowRead, AccessPermissions.AllowWrite, AccessPermissions.AllowExecute, AccessPermissions.Recursive))
        )),
        false},

      // for all users non recursive
      {
        "u+rwx,dir1,dir2",
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("dir1"), AccessControlAccount.forUser(""), EnumSet.of(AccessPermissions.AllowRead, AccessPermissions.AllowWrite, AccessPermissions.AllowExecute)),
          new AccessControlEntry(new File("dir2"), AccessControlAccount.forUser(""), EnumSet.of(AccessPermissions.AllowRead, AccessPermissions.AllowWrite, AccessPermissions.AllowExecute))
        )),
        false},

      // several acl entries
      {
        "rc+rwx,dir1;rc+rwx,dir2",
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("dir1"), AccessControlAccount.forCurrent(), EnumSet.of(AccessPermissions.AllowRead, AccessPermissions.AllowWrite, AccessPermissions.AllowExecute, AccessPermissions.Recursive)),
          new AccessControlEntry(new File("dir2"), AccessControlAccount.forCurrent(), EnumSet.of(AccessPermissions.AllowRead, AccessPermissions.AllowWrite, AccessPermissions.AllowExecute, AccessPermissions.Recursive))
        )),
        false},

      // several acl entries and spaces
      {
        "rc+rwx,dir1 ;   rc+rw x , dir2",
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("dir1"), AccessControlAccount.forCurrent(), EnumSet.of(AccessPermissions.AllowRead, AccessPermissions.AllowWrite, AccessPermissions.AllowExecute, AccessPermissions.Recursive)),
          new AccessControlEntry(new File("dir2"), AccessControlAccount.forCurrent(), EnumSet.of(AccessPermissions.AllowRead, AccessPermissions.AllowWrite, AccessPermissions.AllowExecute, AccessPermissions.Recursive))
        )),
        false},

      //invalid user spec
      {
        "rcz+rwx,dir1,dir2",
        null,
        true},

      //invalid access spec
      {
        "rc+rwz,dir1,dir2",
        null,
        true},

      //no ant pattern spec
      {
        "rc+rw",
        null,
        true},

      //invalid spec
      {
        "abc",
        null,
        true},

      //empty spec
      {
        "",
        new AccessControlList(Arrays.<AccessControlEntry>asList()),
        false},
    };
  }

  @Test(dataProvider = "parseAclCases")
  public void shouldParseAcl(
    @NotNull final String aclStr,
    @Nullable final AccessControlList expectedAcl,
    final boolean expectedThrowException) throws ExecutionException {
    // Given
    final TextParser<AccessControlList> instance = createInstance();

    myCtx.checking(new Expectations() {{
      oneOf(myPathsService).getPath(WellKnownPaths.Bin);
      will(returnValue(myAgentHomeDirectory));
    }});

    boolean actualThrowException = false;
    AccessControlList actualAcl = null;
    // When
    try {
      actualAcl = instance.parse(aclStr);
    }
    catch (BuildStartException buildStartException) {
      actualThrowException = true;
    }

    // Then
    then(actualThrowException).isEqualTo(expectedThrowException);
    then(actualAcl).isEqualTo(expectedAcl);
  }

  @NotNull
  private TextParser<AccessControlList> createInstance()
  {
    return new FileAccessParser(
      myPathsService);
  }
}
