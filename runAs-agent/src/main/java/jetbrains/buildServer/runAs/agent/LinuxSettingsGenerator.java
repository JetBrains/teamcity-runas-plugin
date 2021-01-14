/*
 * Copyright 2000-2021 JetBrains s.r.o.
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

import com.intellij.openapi.util.text.StringUtil;
import jetbrains.buildServer.dotNet.buildRunner.agent.ResourceGenerator;
import org.jetbrains.annotations.NotNull;

public class LinuxSettingsGenerator implements ResourceGenerator<UserCredentials> {
  @NotNull
  @Override
  public String create(@NotNull final UserCredentials userCredentials) {
    final StringBuilder sb = new StringBuilder();

    final String user = userCredentials.getUser();
    if(!StringUtil.isEmptyOrSpaces(user)) {
      sb.append(userCredentials.getUser());
    }
    
    return sb.toString();
  }
}