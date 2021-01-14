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

import java.io.File;
import java.util.EnumSet;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.assertj.core.api.BDDAssertions.then;

public class AccessControlEntryTest {
  @DataProvider(name = "implementEqAndHashCodeCases")
  public Object[][] getImplementEqAndHashCodeCasesCases() {
    return new Object[][] {
      // When eq
      {
        new AccessControlEntry(new File("file1"), AccessControlAccount.forUser("user1"), EnumSet.of(AccessPermissions.Recursive, AccessPermissions.GrantRead, AccessPermissions.GrantWrite), AccessControlScope.Step),
        new AccessControlEntry(new File("file1"), AccessControlAccount.forUser("user1"), EnumSet.of(AccessPermissions.Recursive, AccessPermissions.GrantRead, AccessPermissions.GrantWrite), AccessControlScope.Step),
        true},

      // When eq
      {
        new AccessControlEntry(new File("file1"), AccessControlAccount.forUser("user1"), EnumSet.of(AccessPermissions.Recursive, AccessPermissions.GrantRead, AccessPermissions.GrantWrite), AccessControlScope.Step),
        new AccessControlEntry(new File("file1"), AccessControlAccount.forUser("user1"), EnumSet.of(AccessPermissions.Recursive, AccessPermissions.GrantRead, AccessPermissions.GrantWrite), AccessControlScope.Step),
        true},

      // When not eq by file
      {
        new AccessControlEntry(new File("file1"), AccessControlAccount.forUser("user1"), EnumSet.of(AccessPermissions.Recursive, AccessPermissions.GrantRead, AccessPermissions.GrantWrite), AccessControlScope.Step),
        new AccessControlEntry(new File("file2"), AccessControlAccount.forUser("user1"), EnumSet.of(AccessPermissions.Recursive, AccessPermissions.GrantRead, AccessPermissions.GrantWrite), AccessControlScope.Step),
        false},

      // When not eq by scope
      {
        new AccessControlEntry(new File("file1"), AccessControlAccount.forUser("user1"), EnumSet.of(AccessPermissions.Recursive, AccessPermissions.GrantRead, AccessPermissions.GrantWrite), AccessControlScope.Step),
        new AccessControlEntry(new File("file2"), AccessControlAccount.forUser("user1"), EnumSet.of(AccessPermissions.Recursive, AccessPermissions.GrantRead, AccessPermissions.GrantWrite), AccessControlScope.Build),
        false},

      // When not eq by AccessPermissions
      {
        new AccessControlEntry(new File("file1"), AccessControlAccount.forUser("user1"), EnumSet.of(AccessPermissions.Recursive, AccessPermissions.GrantRead, AccessPermissions.GrantWrite), AccessControlScope.Step),
        new AccessControlEntry(new File("file1"), AccessControlAccount.forUser("user1"), EnumSet.of(AccessPermissions.GrantRead, AccessPermissions.GrantWrite), AccessControlScope.Step),
        false},

      // When not eq by AccessPermissions and file
      {
        new AccessControlEntry(new File("file1"), AccessControlAccount.forUser("user1"), EnumSet.of(AccessPermissions.Recursive, AccessPermissions.GrantRead, AccessPermissions.GrantWrite), AccessControlScope.Step),
        new AccessControlEntry(new File("file2"), AccessControlAccount.forUser("user1"), EnumSet.of(AccessPermissions.GrantRead, AccessPermissions.GrantWrite), AccessControlScope.Step),
        false},

      // When not eq by AccessPermissions when empty
      {
        new AccessControlEntry(new File("file1"), AccessControlAccount.forUser("user1"), EnumSet.of(AccessPermissions.Recursive, AccessPermissions.GrantRead, AccessPermissions.GrantWrite), AccessControlScope.Step),
        new AccessControlEntry(new File("file1"), AccessControlAccount.forUser("user1"), EnumSet.noneOf(AccessPermissions.class), AccessControlScope.Step),
        false},
    };
  }

  @Test(dataProvider = "implementEqAndHashCodeCases")
  public void shouldImplementEqAndHashCode(
    @NotNull final AccessControlEntry ace1,
    @NotNull final AccessControlEntry ace2,
    final boolean expectedIsEq) {
    // Given

    // When
    final int hashCode1 = ace1.hashCode();
    final int hashCode2 = ace2.hashCode();
    final boolean actualIsEq = ace1.equals(ace2);

    // Then
    then(hashCode1 == hashCode2).isEqualTo(expectedIsEq);
    then(actualIsEq).isEqualTo(expectedIsEq);
  }
}
