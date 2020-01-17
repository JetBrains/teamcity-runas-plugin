/*
 * Copyright 2000-2020 JetBrains s.r.o.
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