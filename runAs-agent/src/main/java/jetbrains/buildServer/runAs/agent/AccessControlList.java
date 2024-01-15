

package jetbrains.buildServer.runAs.agent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;

public class AccessControlList implements Iterable<AccessControlEntry> {
  private final ArrayList<AccessControlEntry> myAccessControlEntries = new ArrayList<AccessControlEntry>();

  AccessControlList(final Iterable<AccessControlEntry> accessControlEntries) {
    for (AccessControlEntry entry: accessControlEntries) {
      myAccessControlEntries.add(entry);
    }
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final AccessControlList that = (AccessControlList)o;

    return myAccessControlEntries.equals(that.myAccessControlEntries);
  }

  @Override
  public int hashCode() {
    return myAccessControlEntries.hashCode();
  }

  @Override
  public Iterator<AccessControlEntry> iterator() {
    return myAccessControlEntries.iterator();
  }


  @Override
  public String toString() {
    return LogUtils.toString(
      "ACL",
      LogUtils.toString(this));
  }
}