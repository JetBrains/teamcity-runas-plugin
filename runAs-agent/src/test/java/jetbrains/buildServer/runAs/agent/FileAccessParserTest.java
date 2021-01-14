/*
 * Copyright 2000-2021 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
          new AccessControlEntry(new File("dir1"), AccessControlAccount.forAll(), EnumSet.of(AccessPermissions.GrantRead, AccessPermissions.GrantWrite, AccessPermissions.GrantExecute, AccessPermissions.Recursive), AccessControlScope.Step),
          new AccessControlEntry(new File("dir2"), AccessControlAccount.forAll(), EnumSet.of(AccessPermissions.GrantRead, AccessPermissions.GrantWrite, AccessPermissions.GrantExecute, AccessPermissions.Recursive), AccessControlScope.Step)
        )),
        false},

      // global scope

      {
        "gra+rwx,dir1,dir2",
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("dir1"), AccessControlAccount.forAll(), EnumSet.of(AccessPermissions.GrantRead, AccessPermissions.GrantWrite, AccessPermissions.GrantExecute, AccessPermissions.Recursive), AccessControlScope.Global),
          new AccessControlEntry(new File("dir2"), AccessControlAccount.forAll(), EnumSet.of(AccessPermissions.GrantRead, AccessPermissions.GrantWrite, AccessPermissions.GrantExecute, AccessPermissions.Recursive), AccessControlScope.Global)
        )),
        false},

      // with spaces
      {
        " r a + r wx , dir1 , dir2 ",
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("dir1"), AccessControlAccount.forAll(), EnumSet.of(AccessPermissions.GrantRead, AccessPermissions.GrantWrite, AccessPermissions.GrantExecute, AccessPermissions.Recursive), AccessControlScope.Step),
          new AccessControlEntry(new File("dir2"), AccessControlAccount.forAll(), EnumSet.of(AccessPermissions.GrantRead, AccessPermissions.GrantWrite, AccessPermissions.GrantExecute, AccessPermissions.Recursive), AccessControlScope.Step)
        )),
        false},

      // for all users
      {
        "ru+rwx,dir1,dir2",
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("dir1"), AccessControlAccount.forUser(""), EnumSet.of(AccessPermissions.GrantRead, AccessPermissions.GrantWrite, AccessPermissions.GrantExecute, AccessPermissions.Recursive), AccessControlScope.Step),
          new AccessControlEntry(new File("dir2"), AccessControlAccount.forUser(""), EnumSet.of(AccessPermissions.GrantRead, AccessPermissions.GrantWrite, AccessPermissions.GrantExecute, AccessPermissions.Recursive), AccessControlScope.Step)
        )),
        false},

      // for all users non recursive
      {
        "u+rwx,dir1,dir2",
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("dir1"), AccessControlAccount.forUser(""), EnumSet.of(AccessPermissions.GrantRead, AccessPermissions.GrantWrite, AccessPermissions.GrantExecute), AccessControlScope.Step),
          new AccessControlEntry(new File("dir2"), AccessControlAccount.forUser(""), EnumSet.of(AccessPermissions.GrantRead, AccessPermissions.GrantWrite, AccessPermissions.GrantExecute), AccessControlScope.Step)
        )),
        false},

      // several acl entries
      {
        "ra+rwx,dir1;ra+rwx,dir2",
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("dir1"), AccessControlAccount.forAll(), EnumSet.of(AccessPermissions.GrantRead, AccessPermissions.GrantWrite, AccessPermissions.GrantExecute, AccessPermissions.Recursive), AccessControlScope.Step),
          new AccessControlEntry(new File("dir2"), AccessControlAccount.forAll(), EnumSet.of(AccessPermissions.GrantRead, AccessPermissions.GrantWrite, AccessPermissions.GrantExecute, AccessPermissions.Recursive), AccessControlScope.Step)
        )),
        false},

      // several acl entries with scopes
      {
        "rab+rwx,dir1;rag+rwx,dir2",
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("dir1"), AccessControlAccount.forAll(), EnumSet.of(AccessPermissions.GrantRead, AccessPermissions.GrantWrite, AccessPermissions.GrantExecute, AccessPermissions.Recursive), AccessControlScope.Build),
          new AccessControlEntry(new File("dir2"), AccessControlAccount.forAll(), EnumSet.of(AccessPermissions.GrantRead, AccessPermissions.GrantWrite, AccessPermissions.GrantExecute, AccessPermissions.Recursive), AccessControlScope.Global)
        )),
        false},

      // several acl entries and spaces
      {
        "ra+rwx,dir1 ;   ra+rw x , dir2",
        new AccessControlList(Arrays.asList(
          new AccessControlEntry(new File("dir1"), AccessControlAccount.forAll(), EnumSet.of(AccessPermissions.GrantRead, AccessPermissions.GrantWrite, AccessPermissions.GrantExecute, AccessPermissions.Recursive), AccessControlScope.Step),
          new AccessControlEntry(new File("dir2"), AccessControlAccount.forAll(), EnumSet.of(AccessPermissions.GrantRead, AccessPermissions.GrantWrite, AccessPermissions.GrantExecute, AccessPermissions.Recursive), AccessControlScope.Step)
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
