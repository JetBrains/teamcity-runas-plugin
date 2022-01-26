/*
 * Copyright 2000-2022 JetBrains s.r.o.
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

import java.util.LinkedHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Result<TContext, TValue> {
  @NotNull private final boolean myIsSuccessful;
  @NotNull private TContext myContext;
  @Nullable private TValue myValue;
  @Nullable private Exception myError;

  public Result(
    @NotNull final TContext context,
    @NotNull final TValue value) {
    myContext = context;
    myValue = value;
    myIsSuccessful = true;
  }

  public Result(
    @NotNull final TContext context,
    @NotNull final Exception error) {
    myContext = context;
    myError = error;
    myIsSuccessful = false;
  }

  @NotNull
  public TContext getContext() {
    return myContext;
  }

  public boolean isSuccessful() {
    return myIsSuccessful;
  }

  @Nullable
  public TValue getValue() {
    return myValue;
  }

  @Nullable
  public Exception getError() {
    return myError;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (!(o instanceof Result)) return false;

    final Result<?, ?> result = (Result<?, ?>)o;

    if (isSuccessful() != result.isSuccessful()) return false;
    if (!getContext().equals(result.getContext())) return false;
    if (getValue() != null ? !getValue().equals(result.getValue()) : result.getValue() != null) return false;
    return getError() != null ? getError().equals(result.getError()) : result.getError() == null;

  }

  @Override
  public int hashCode() {
    int result = (isSuccessful() ? 1 : 0);
    result = 31 * result + getContext().hashCode();
    result = 31 * result + (getValue() != null ? getValue().hashCode() : 0);
    result = 31 * result + (getError() != null ? getError().hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return LogUtils.toString(
      "Result",
      new LinkedHashMap<String, Object>() {{
        this.put("Context", myContext);
        this.put("IsSuccessful", myIsSuccessful);
        this.put("Value", myValue);
        this.put("Error", myError);
      }});
  }
}
