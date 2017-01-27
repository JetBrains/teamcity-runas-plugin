package jetbrains.buildServer.runAs.agent;

import com.intellij.execution.ExecutionException;
import java.io.File;
import java.util.Arrays;
import java.util.EnumSet;
import jetbrains.buildServer.dotNet.buildRunner.agent.BuildStartException;
import jetbrains.buildServer.dotNet.buildRunner.agent.TextParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.assertj.core.api.BDDAssertions.then;

public class FileAccessParserTest {
  @DataProvider(name = "parseAclCases")
  public Object[][] getParseAclCasesCases() {
    return new Object[][] {
      {
        "ra+rwx,dir1,dir2",
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("dir1"), AccessControlAccount.forAll(), EnumSet.of(AccessPermissions.GrantRead, AccessPermissions.GrantWrite, AccessPermissions.GrantExecute, AccessPermissions.Recursive)),
          new AccessControlEntry(new File("dir2"), AccessControlAccount.forAll(), EnumSet.of(AccessPermissions.GrantRead, AccessPermissions.GrantWrite, AccessPermissions.GrantExecute, AccessPermissions.Recursive))
        )),
        false},

      // with spaces
      {
        " r a + r wx , dir1 , dir2 ",
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("dir1"), AccessControlAccount.forAll(), EnumSet.of(AccessPermissions.GrantRead, AccessPermissions.GrantWrite, AccessPermissions.GrantExecute, AccessPermissions.Recursive)),
          new AccessControlEntry(new File("dir2"), AccessControlAccount.forAll(), EnumSet.of(AccessPermissions.GrantRead, AccessPermissions.GrantWrite, AccessPermissions.GrantExecute, AccessPermissions.Recursive))
        )),
        false},

      // for all users
      {
        "ru+rwx,dir1,dir2",
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("dir1"), AccessControlAccount.forUser(""), EnumSet.of(AccessPermissions.GrantRead, AccessPermissions.GrantWrite, AccessPermissions.GrantExecute, AccessPermissions.Recursive)),
          new AccessControlEntry(new File("dir2"), AccessControlAccount.forUser(""), EnumSet.of(AccessPermissions.GrantRead, AccessPermissions.GrantWrite, AccessPermissions.GrantExecute, AccessPermissions.Recursive))
        )),
        false},

      // for all users non recursive
      {
        "u+rwx,dir1,dir2",
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("dir1"), AccessControlAccount.forUser(""), EnumSet.of(AccessPermissions.GrantRead, AccessPermissions.GrantWrite, AccessPermissions.GrantExecute)),
          new AccessControlEntry(new File("dir2"), AccessControlAccount.forUser(""), EnumSet.of(AccessPermissions.GrantRead, AccessPermissions.GrantWrite, AccessPermissions.GrantExecute))
        )),
        false},

      // several acl entries
      {
        "ra+rwx,dir1;ra+rwx,dir2",
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("dir1"), AccessControlAccount.forAll(), EnumSet.of(AccessPermissions.GrantRead, AccessPermissions.GrantWrite, AccessPermissions.GrantExecute, AccessPermissions.Recursive)),
          new AccessControlEntry(new File("dir2"), AccessControlAccount.forAll(), EnumSet.of(AccessPermissions.GrantRead, AccessPermissions.GrantWrite, AccessPermissions.GrantExecute, AccessPermissions.Recursive))
        )),
        false},

      // several acl entries and spaces
      {
        "ra+rwx,dir1 ;   ra+rw x , dir2",
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("dir1"), AccessControlAccount.forAll(), EnumSet.of(AccessPermissions.GrantRead, AccessPermissions.GrantWrite, AccessPermissions.GrantExecute, AccessPermissions.Recursive)),
          new AccessControlEntry(new File("dir2"), AccessControlAccount.forAll(), EnumSet.of(AccessPermissions.GrantRead, AccessPermissions.GrantWrite, AccessPermissions.GrantExecute, AccessPermissions.Recursive))
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
    return new FileAccessParser();
  }
}
