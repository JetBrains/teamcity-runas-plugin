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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineExecutionContext;
import org.jetbrains.annotations.NotNull;

public class AccessControlResourceImpl implements AccessControlResource {
  private final FileAccessService myFileAccessService;
  private AccessControlList myAccessControlList = new AccessControlList(Collections.<AccessControlEntry>emptyList());

  public AccessControlResourceImpl(@NotNull final FileAccessService fileAccessService) {
    myFileAccessService = fileAccessService;
  }

  @Override
  public void setAcl(@NotNull final AccessControlList accessControlList) {
    myAccessControlList = accessControlList;
  }

  @Override
  public void publishBeforeBuild(@NotNull final CommandLineExecutionContext commandLineExecutionContext) {
    myFileAccessService.setAccess(myAccessControlList);
  }

  @Override
  public void publishAfterBuild(@NotNull final CommandLineExecutionContext commandLineExecutionContext) {
  }

  @Override
  public String toString() {
    return LogUtils.toString(
      "AclResource",
      myAccessControlList);
  }
}
