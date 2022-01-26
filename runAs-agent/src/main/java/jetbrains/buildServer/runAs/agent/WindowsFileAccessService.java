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

import com.intellij.execution.ExecutionException;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import jetbrains.buildServer.ExecResult;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineArgument;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineResource;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineSetup;
import jetbrains.buildServer.util.*;
import org.jetbrains.annotations.NotNull;

import static jetbrains.buildServer.runAs.agent.Constants.ICACLS_TOOL_NAME;

public class WindowsFileAccessService implements FileAccessService {
  private static final Logger LOG = Logger.getInstance(WindowsFileAccessService.class.getName());
  private static final int EXECUTION_TIMEOUT_SECONDS = 600;
  private final CommandLineExecutor myCommandLineExecutor;

  public WindowsFileAccessService(
    @NotNull final CommandLineExecutor commandLineExecutor) {
    myCommandLineExecutor = commandLineExecutor;
  }

  public Iterable<Result<AccessControlEntry, Boolean>> setAccess(@NotNull final AccessControlList accessControlList) {
    return CollectionsUtil.convertAndFilterNulls(accessControlList, new jetbrains.buildServer.util.Converter<Result<AccessControlEntry, Boolean>, AccessControlEntry>() {
      @Override
      public Result<AccessControlEntry, Boolean> createFrom(@NotNull final AccessControlEntry accessControlEntry) {
        return tryApplyAccess(accessControlEntry);
      }
    });
  }

  private Result<AccessControlEntry, Boolean> tryApplyAccess(@NotNull final AccessControlEntry entry) {
    final EnumSet<AccessPermissions> permissions = entry.getPermissions();
    if(permissions.size() == 0) {
      return null;
    }

    final AccessControlAccount account = entry.getAccount();
    String username = null;
    switch (account.getTargetType()) {
      case User:
        username = account.getUserName();
        break;

      case All:
        username = "NT AUTHORITY\\Authenticated Users";
        break;

      default:
        throw new IllegalStateException("Unknown AccessControlAccountType: " + account.getTargetType());
    }

    String filePath = entry.getFile().getAbsolutePath();

    final ArrayList<CommandLineArgument> args = new ArrayList<CommandLineArgument>();
    args.add(new CommandLineArgument(filePath, CommandLineArgument.Type.PARAMETER));
    args.add(new CommandLineArgument("/C", CommandLineArgument.Type.PARAMETER));
    args.add(new CommandLineArgument("/Q", CommandLineArgument.Type.PARAMETER));

    List<String> grantedPermissionList = new ArrayList<String>();
    if(permissions.contains(AccessPermissions.GrantRead)) {
      grantedPermissionList.add("R");
    }

    if(permissions.contains(AccessPermissions.GrantWrite)) {
      grantedPermissionList.add("W,D,DC");
    }

    if(permissions.contains(AccessPermissions.GrantExecute)) {
      grantedPermissionList.add("RX");
    }

    List<String> deniedPermissionList = new ArrayList<String>();
    if(permissions.contains(AccessPermissions.DenyRead)) {
      deniedPermissionList.add("R");
    }

    if(permissions.contains(AccessPermissions.DenyWrite)) {
      deniedPermissionList.add("W,D,DC");
    }

    if(permissions.contains(AccessPermissions.DenyExecute)) {
      deniedPermissionList.add("X");
    }

    if(grantedPermissionList.size() > 0) {
      args.add(new CommandLineArgument("/grant", CommandLineArgument.Type.PARAMETER));
      final boolean recursive = permissions.contains(AccessPermissions.Recursive);
      final String permissionsStr = username + ":" + (recursive ? "(OI)(CI)" : "") + "(" + StringUtil.join(grantedPermissionList, ",") + ")";
      args.add(new CommandLineArgument(permissionsStr, CommandLineArgument.Type.PARAMETER));
    }

    if(deniedPermissionList.size() > 0) {
      args.add(new CommandLineArgument("/deny", CommandLineArgument.Type.PARAMETER));
      final boolean recursive = permissions.contains(AccessPermissions.Recursive);
      final String permissionsStr = username + ":" + (recursive ? "(OI)(CI)" : "") + "(" + StringUtil.join(deniedPermissionList, ",") + ")";
      args.add(new CommandLineArgument(permissionsStr, CommandLineArgument.Type.PARAMETER));
    }

    final CommandLineSetup icaclsCommandLineSetup = new CommandLineSetup(ICACLS_TOOL_NAME, args, Collections.<CommandLineResource>emptyList());
    try {
      final ExecResult result = myCommandLineExecutor.runProcess(icaclsCommandLineSetup, EXECUTION_TIMEOUT_SECONDS);
      if(result == null ) {
        return null;
      }

      return processResult(entry, result);
    }
    catch (ExecutionException e) {
      LOG.error(e);
      return new Result<AccessControlEntry, Boolean>(entry, e);
    }
  }

  private Result<AccessControlEntry, Boolean> processResult(@NotNull final AccessControlEntry entry, @NotNull final ExecResult result) {
    if(result.getExitCode() != 0) {
      final String resultStr = result.toString();
      LOG.warn(resultStr);
      return new Result<AccessControlEntry, Boolean>(entry, false);
    }

    return new Result<AccessControlEntry, Boolean>(entry, true);
  }
}