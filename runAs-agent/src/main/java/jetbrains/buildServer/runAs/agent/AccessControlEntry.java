/*
 * Copyright 2000-2022 JetBrains s.r.o.
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
import java.util.LinkedHashMap;
import org.jetbrains.annotations.NotNull;

class AccessControlEntry {
  @NotNull private final File myFile;
  @NotNull private final AccessControlAccount myAccount;
  @NotNull private final EnumSet<AccessPermissions> myPermissions;
  @NotNull private final AccessControlScope myScope;

  AccessControlEntry(
    @NotNull final File file,
    @NotNull final AccessControlAccount account,
    @NotNull final EnumSet<AccessPermissions> permissions,
    @NotNull final AccessControlScope scope) {
    myFile = file;
    myAccount = account;
    myPermissions = permissions;
    myScope = scope;
  }

  @NotNull
  File getFile() {
    return myFile;
  }

  @NotNull AccessControlAccount getAccount() {
    return myAccount;
  }

  @NotNull EnumSet<AccessPermissions> getPermissions() {
    return myPermissions;
  }

  @NotNull AccessControlScope getScope() {
    return myScope;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (!(o instanceof AccessControlEntry)) return false;

    final AccessControlEntry that = (AccessControlEntry)o;

    if (!getFile().equals(that.getFile())) return false;
    if (!getAccount().equals(that.getAccount())) return false;
    return getPermissions().equals(that.getPermissions());
  }

  @Override
  public int hashCode() {
    int result = getFile().hashCode();
    result = 31 * result + getAccount().hashCode();
    result = 31 * result + getPermissions().hashCode();
    return result;
  }

  @Override
  public String toString() {
    return LogUtils.toString(
      "ACE",
      new LinkedHashMap<String, Object>() {{
        this.put("File", myFile);
        this.put("Account", myAccount);
        this.put("Permissions", LogUtils.toString(myPermissions));
        this.put("Scope", myScope);
    }});
  }
}