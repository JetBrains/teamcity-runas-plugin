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

import java.util.Map;
import jetbrains.buildServer.util.*;
import jetbrains.buildServer.util.Converter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LogUtils {
  private static final String Nothing = "null";

  @NotNull
  public static <T> String toString(@Nullable final Iterable<T> iterable) {
    if(iterable == null) {
      return Nothing;
    }

    return StringUtil.join(", ",
        CollectionsUtil.convertSet(iterable, new Converter<String, T>() {
          @Override
          public String createFrom(@NotNull final T item) {
              return item.toString();
          }
        }));
  }

  @NotNull
  public static <T> String toString(@NotNull String name, T value) {
    if(value == null) {
      return Nothing;
    }

    return name + "{ " + value.toString() + " }";
  }

  @NotNull
  public static String toString(@NotNull String name, @Nullable final Map<String, Object> values) {
    if(values == null) {
      return Nothing;
    }

    return
      toString(
        name,
        toString(
          CollectionsUtil.convertSet(values.entrySet(), new Converter<String, Map.Entry<String, Object>>() {
            @Override
            public String createFrom(@NotNull final Map.Entry<String, Object> item) {
              return "\"" + item.getKey() + "\": " + (item.getValue() != null ? item.getValue().toString() : Nothing);
            }
          }))
      );
  }
}
