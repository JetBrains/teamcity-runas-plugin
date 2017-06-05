package jetbrains.buildServer.runAs.agent;

import java.io.File;
import java.util.EnumSet;
import java.util.HashMap;
import org.jetbrains.annotations.NotNull;

class AccessControlEntry {
  @NotNull private final File myFile;
  private final AccessControlAccount myAccount;
  private final EnumSet<AccessPermissions> myPermissions;
  private boolean myIsCachingAllowed;

  AccessControlEntry(
    @NotNull final File file,
    @NotNull final AccessControlAccount account,
    @NotNull final EnumSet<AccessPermissions> permissions) {
    myFile = file;
    myAccount = account;
    myPermissions = permissions;
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

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final AccessControlEntry that = (AccessControlEntry)o;

    if (!myFile.equals(that.myFile)) return false;
    if (!myAccount.equals(that.myAccount)) return false;
    return myPermissions.equals(that.myPermissions);
  }

  @Override
  public int hashCode() {
    int result = myFile.hashCode();
    result = 31 * result + myAccount.hashCode();
    result = 31 * result + myPermissions.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return LogUtils.toString(
      "ACE",
      new HashMap<String, Object>() {{
        this.put("File", myFile);
        this.put("Account", myAccount);
        this.put("Permissions", LogUtils.toString(myPermissions));
        this.put("IsCachingAllowed", isCachingAllowed());
    }});
  }

  public boolean isCachingAllowed() {
    return myIsCachingAllowed;
  }

  public void setCachingAllowed(final boolean isCachingAllowed) {
    myIsCachingAllowed = isCachingAllowed;
  }
}