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
