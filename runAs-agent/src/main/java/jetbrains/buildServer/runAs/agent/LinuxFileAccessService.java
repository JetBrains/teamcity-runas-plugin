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

import com.intellij.execution.ExecutionException;
import com.intellij.openapi.diagnostic.Logger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import jetbrains.buildServer.ExecResult;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineArgument;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineResource;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineSetup;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static jetbrains.buildServer.runAs.agent.Constants.CHMOD_TOOL_NAME;

public class LinuxFileAccessService implements FileAccessService {
  private static final Logger LOG = Logger.getInstance(LinuxFileAccessService.class.getName());
  private static final int EXECUTION_TIMEOUT_SECONDS = 600;
  private final CommandLineExecutor myCommandLineExecutor;

  public LinuxFileAccessService(@NotNull final CommandLineExecutor commandLineExecutor) {
    myCommandLineExecutor = commandLineExecutor;
  }

  public Iterable<Result<AccessControlEntry, Boolean>> setAccess(@NotNull final AccessControlList accessControlList) {
    List<Result<AccessControlEntry, Boolean>> results = new ArrayList<Result<AccessControlEntry, Boolean>>();
    for (AccessControlEntry ace: accessControlList) {
      for (Result<AccessControlEntry, Boolean> result: tryApplyAccess(ace)) {
        results.add(result);
      }
    }

    return results;
  }

  @NotNull
  private Iterable<Result<AccessControlEntry, Boolean>> tryApplyAccess(final AccessControlEntry entry) {
    List<Result<AccessControlEntry, Boolean>> results = new ArrayList<Result<AccessControlEntry, Boolean>>();
    final EnumSet<AccessPermissions> permissions = entry.getPermissions();
    if(permissions.size() == 0) {
      return results;
    }

    final AccessControlAccount account = entry.getAccount();
    ArrayList<String> permissionsList = new ArrayList<String>();
    if (permissions.contains(AccessPermissions.GrantRead)) {
      permissionsList.add("rX");
    }

    if (permissions.contains(AccessPermissions.GrantWrite)) {
      permissionsList.add("w");
    }

    if (permissions.contains(AccessPermissions.GrantExecute)) {
      permissionsList.add("x");
    }

    if(permissionsList.size() > 0)
    {
      switch (account.getTargetType()) {
        case User:
          permissionsList.add(0, "a+");
          break;

        case All:
          permissionsList.add(0, "a+");
          break;

        default:
          throw new IllegalStateException("Unknown AccessControlAccountType: " + account.getTargetType());
      }

      final Result<AccessControlEntry, Boolean> result = tryExecChmod(entry, permissionsList);
      if(result != null) {
        results.add(result);
      }

      permissionsList.clear();
    }

    if (permissions.contains(AccessPermissions.DenyRead)) {
      permissionsList.add("r");
    }

    if (permissions.contains(AccessPermissions.DenyWrite)) {
      permissionsList.add("w");
    }

    if (permissions.contains(AccessPermissions.DenyExecute)) {
      permissionsList.add("x");
    }

    if(permissionsList.size() > 0)
    {
      switch (account.getTargetType()) {
        case User:
          permissionsList.add(0, "go-");
          break;

        case All:
          permissionsList.add(0, "a-");
          break;
      }

      final Result<AccessControlEntry, Boolean> result = tryExecChmod(entry, permissionsList);
      if(result != null) {
        results.add(result);
      }
    }

    return results;
  }

  @Nullable
  private Result<AccessControlEntry, Boolean> tryExecChmod(@NotNull final AccessControlEntry entry, @NotNull final Iterable<String> chmodPermissions)
  {
    final String chmodPermissionsStr = StringUtil.join("", chmodPermissions);
    if(StringUtil.isEmptyOrSpaces(chmodPermissionsStr)) {
      return null;
    }

    final ArrayList<CommandLineArgument> args = new ArrayList<CommandLineArgument>();
    if (entry.getPermissions().contains(AccessPermissions.Recursive)) {
      args.add(new CommandLineArgument("-R", CommandLineArgument.Type.PARAMETER));
    }
    args.add(new CommandLineArgument(chmodPermissionsStr, CommandLineArgument.Type.PARAMETER));
    args.add(new CommandLineArgument(entry.getFile().getAbsolutePath(), CommandLineArgument.Type.PARAMETER));
    final CommandLineSetup chmodCommandLineSetup = new CommandLineSetup(CHMOD_TOOL_NAME, args, Collections.<CommandLineResource>emptyList());
    try {
      final ExecResult result = myCommandLineExecutor.runProcess(chmodCommandLineSetup, EXECUTION_TIMEOUT_SECONDS);
      if(result == null) {
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