package jetbrains.buildServer.runAs.agent;

import java.io.File;
import org.jetbrains.annotations.NotNull;

class AccessControlEntry {
  @NotNull private final File myFile;
  private final AccessControlAccount myAccount;
  private final Boolean myReading;
  private final Boolean myWriting;
  private final Boolean myExecuting;
  private final Boolean myRecursive;

  AccessControlEntry(
    @NotNull final File file,
    @NotNull AccessControlAccount account,
    final Boolean reading,
    final Boolean writing,
    final Boolean executing,
    final Boolean recursive) {
    myFile = file;
    myAccount = account;
    myReading = reading;
    myWriting = writing;
    myExecuting = executing;
    myRecursive = recursive;
  }

  @NotNull
  File getFile() {
    return myFile;
  }

  Boolean isReading() {
    return myReading;
  }

  Boolean isWriting() {
    return myWriting;
  }

  Boolean isExecuting() {
    return myExecuting;
  }

  Boolean isRecursive() {
    return myRecursive;
  }

  AccessControlAccount getAccount() {
    return myAccount;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final AccessControlEntry that = (AccessControlEntry)o;

    if (!myFile.equals(that.myFile)) return false;
    if (!myAccount.equals(that.myAccount)) return false;
    if (myReading != null ? !myReading.equals(that.myReading) : that.myReading != null) return false;
    if (myWriting != null ? !myWriting.equals(that.myWriting) : that.myWriting != null) return false;
    if (myExecuting != null ? !myExecuting.equals(that.myExecuting) : that.myExecuting != null) return false;
    return myRecursive != null ? myRecursive.equals(that.myRecursive) : that.myRecursive == null;

  }

  @Override
  public int hashCode() {
    int result = myFile.hashCode();
    result = 31 * result + myAccount.hashCode();
    result = 31 * result + (myReading != null ? myReading.hashCode() : 0);
    result = 31 * result + (myWriting != null ? myWriting.hashCode() : 0);
    result = 31 * result + (myExecuting != null ? myExecuting.hashCode() : 0);
    result = 31 * result + (myRecursive != null ? myRecursive.hashCode() : 0);
    return result;
  }
}
