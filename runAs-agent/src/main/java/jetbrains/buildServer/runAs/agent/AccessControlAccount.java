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

import java.util.LinkedHashMap;
import org.jetbrains.annotations.NotNull;

class AccessControlAccount {
  @NotNull private final AccessControlAccountType myTargetType;
  @NotNull private final String myUserName;

  private AccessControlAccount(@NotNull final AccessControlAccountType targetType, @NotNull final String userName) {
    myTargetType = targetType;
    myUserName = userName;
  }

  static AccessControlAccount forUser(@NotNull final String userName)
  {
    return new AccessControlAccount(AccessControlAccountType.User, userName);
  }

  static AccessControlAccount forAll()
  {
    return new AccessControlAccount(AccessControlAccountType.All, "");
  }

  @NotNull
  AccessControlAccountType getTargetType() {
    return myTargetType;
  }

  @NotNull
  public String getUserName() {
    return myUserName;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final AccessControlAccount that = (AccessControlAccount)o;

    if (myTargetType != that.myTargetType) return false;
    return myUserName.equals(that.myUserName);

  }

  @Override
  public int hashCode() {
    int result = myTargetType.hashCode();
    result = 31 * result + myUserName.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return LogUtils.toString(
      "Account",
      new LinkedHashMap<String, Object>() {{
      this.put("Type", myTargetType);
      this.put("UserName", myUserName);
    }});
  }
}
