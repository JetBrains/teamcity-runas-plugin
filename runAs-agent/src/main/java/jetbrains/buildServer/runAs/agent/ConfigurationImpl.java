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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConfigurationImpl implements Configuration {
  private final HashMap<Object, Object> myMap = new HashMap<Object, Object>();

  @Override
  public void load(@NotNull final ByteArrayInputStream stream) throws IOException {
    myMap.clear();
    Scanner scanner = new Scanner(stream);
    try {
        while(scanner.hasNext()) {
          String line = scanner.nextLine();
          if(line == null) {
            continue;
          }

          line = line.trim();
          if(line.length() == 0) {
            continue;
          }

          final int index = line.indexOf('=');
          if(index < 1 || index >= line.length() - 2) {
            continue;
          }

          if(line.startsWith("#")) {
            continue;
          }

          String key = line.substring(0, index).trim();
          String value = line.substring(index + 1, line.length()).trim();
          myMap.put(key, value);
      }
    }
    finally {
      scanner.close();
    }
  }

  @Override
  public int size() {
    return myMap.size();
  }

  @NotNull
  @Override
  public Set<Map.Entry<Object, Object>> entrySet() {
    return myMap.entrySet();
  }

  @Nullable
  @Override
  public String getProperty(@NotNull final String key) {
    return (String)myMap.get(key);
  }
}
