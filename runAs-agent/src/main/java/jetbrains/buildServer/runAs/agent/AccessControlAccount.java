package jetbrains.buildServer.runAs.agent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class AccessControlAccount {
  @NotNull private final AccessControlAccountType myTargetType;
  @Nullable private final String myTargetName;

  private AccessControlAccount(@NotNull final AccessControlAccountType targetType, @Nullable final String targetName) {
    myTargetType = targetType;
    myTargetName = targetName;
  }

  static AccessControlAccount getCurrent()
  {
    return new AccessControlAccount(AccessControlAccountType.Current, null);
  }

  static AccessControlAccount getAll()
  {
    return new AccessControlAccount(AccessControlAccountType.All, null);
  }

  public static AccessControlAccount getUser(final String userName)
  {
    return new AccessControlAccount(AccessControlAccountType.User, userName);
  }

  @NotNull
  AccessControlAccountType getTargetType() {
    return myTargetType;
  }

  @Nullable
  String getTargetName() {
    return myTargetName;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final AccessControlAccount that = (AccessControlAccount)o;

    if (myTargetType != that.myTargetType) return false;
    return myTargetName != null ? myTargetName.equals(that.myTargetName) : that.myTargetName == null;

  }

  @Override
  public int hashCode() {
    int result = myTargetType.hashCode();
    result = 31 * result + (myTargetName != null ? myTargetName.hashCode() : 0);
    return result;
  }
}
