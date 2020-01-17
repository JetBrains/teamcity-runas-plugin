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

package jetbrains.buildServer.runAs.common;

public enum WindowsIntegrityLevel {
  Auto("auto", "Default"),
  Untrusted("untrusted", "Untrusted"),
  Low("low", "Low"),
  Medium("medium", "Medium"),
  MediumPlus("medium_plus", "Medium Plus"),
  High("high", "High");

  private final String myValue;
  private final String myDescription;

  private WindowsIntegrityLevel(String value, final String description) {
    myValue = value;
    myDescription = description;
  }

  public String getValue() {
    return myValue;
  }

  public String getDescription() {
    return myDescription;
  }

  public static WindowsIntegrityLevel tryParse(String value) {
    for (WindowsIntegrityLevel v : values()) {
      if (v.getValue().equals(value)) return v;
    }

    return Auto;
  }
}