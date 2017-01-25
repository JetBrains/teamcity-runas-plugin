package jetbrains.buildServer.runAs.agent;

import com.intellij.execution.ExecutionException;
import com.intellij.openapi.diagnostic.Logger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import jetbrains.buildServer.ExecResult;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineArgument;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineResource;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineSetup;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import static jetbrains.buildServer.runAs.agent.Constants.CHMOD_TOOL_NAME;

public class LinuxFileAccessService implements FileAccessService {
  private static final Logger LOG = Logger.getInstance(LinuxFileAccessService.class.getName());
  private static final int EXECUTION_TIMEOUT_SECONDS = 600;
  private final CommandLineExecutor myCommandLineExecutor;

  public LinuxFileAccessService(@NotNull final CommandLineExecutor commandLineExecutor) {
    myCommandLineExecutor = commandLineExecutor;
  }

  public void setAccess(@NotNull final AccessControlList accessControlList) {
    for (AccessControlEntry entry : accessControlList) {
      applyAccessControlEntryForLinux(entry);
    }
  }

  private void applyAccessControlEntryForLinux(final AccessControlEntry entry) {
    final EnumSet<AccessPermissions> permissions = entry.getPermissions();
    if(permissions.size() == 0) {
      return;
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
      }

      execChmod(entry, permissionsList);
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

      execChmod(entry, permissionsList);
    }
  }

  private void execChmod(@NotNull AccessControlEntry entry, @NotNull Iterable<String> chmodPermissions)
  {
    final String chmodPermissionsStr = StringUtil.join("", chmodPermissions);
    if(StringUtil.isEmptyOrSpaces(chmodPermissionsStr)) {
      return;
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
      processResult(result);
    }
    catch (ExecutionException e) {
      LOG.error(e);
    }
  }

  private void processResult(final ExecResult result) {
    if(result != null && result.getExitCode() != 0) {
      final String[] outLines = result.getOutLines();
      if(outLines != null && outLines.length > 0) {
        for (String line: outLines) {
          LOG.warn(line);
        }
      }

      final String stderr = result.getStderr();
      if(stderr.length() > 0) {
        LOG.warn(stderr);
      }
    }
  }
}