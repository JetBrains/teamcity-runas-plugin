package jetbrains.buildServer.runAs.agent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class AccessControlAccount {
  @NotNull private final AccessControlAccountType myTargetType;

  private AccessControlAccount(@NotNull final AccessControlAccountType targetType) {
    myTargetType = targetType;
  }

  static AccessControlAccount forCurrent()
  {
    return new AccessControlAccount(AccessControlAccountType.Current);
  }

  static AccessControlAccount forAll()
  {
    return new AccessControlAccount(AccessControlAccountType.All);
  }

  @NotNull
  AccessControlAccountType getTargetType() {
    return myTargetType;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final AccessControlAccount that = (AccessControlAccount)o;

    return myTargetType == that.myTargetType;

  }

  @Override
  public int hashCode() {
    return myTargetType.hashCode();
  }
}
