package jetbrains.buildServer.runAs.agent;

import java.io.File;
import java.util.EnumSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class AccessControlEntry {
  @NotNull private final File myFile;
  private final AccessControlAccount myAccount;
  private final EnumSet<AccessPermissions> myPermissions;
  private final boolean myRecursive;

  AccessControlEntry(
    @NotNull final File file,
    @NotNull final AccessControlAccount account,
    @NotNull final EnumSet<AccessPermissions> permissions,
    final boolean recursive) {
    myFile = file;
    myAccount = account;
    myPermissions = permissions;
    myRecursive = recursive;
  }

  @NotNull
  File getFile() {
    return myFile;
  }

  @NotNull  AccessControlAccount getAccount() {
    return myAccount;
  }

  public EnumSet<AccessPermissions> getPermissions() {
    return myPermissions;
  }

  @Nullable
  boolean isRecursive() {
    return myRecursive;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final AccessControlEntry that = (AccessControlEntry)o;

    if (myRecursive != that.myRecursive) return false;
    if (!myFile.equals(that.myFile)) return false;
    if (!myAccount.equals(that.myAccount)) return false;
    return myPermissions.equals(that.myPermissions);

  }

  @Override
  public int hashCode() {
    int result = myFile.hashCode();
    result = 31 * result + myAccount.hashCode();
    result = 31 * result + myPermissions.hashCode();
    result = 31 * result + (myRecursive ? 1 : 0);
    return result;
  }
}
