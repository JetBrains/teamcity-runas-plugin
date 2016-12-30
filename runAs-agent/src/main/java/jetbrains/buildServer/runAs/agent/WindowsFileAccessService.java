package jetbrains.buildServer.runAs.agent;

import com.intellij.execution.ExecutionException;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import java.io.File;
import java.util.*;
import jetbrains.buildServer.ExecResult;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineArgument;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineResource;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineSetup;
import jetbrains.buildServer.dotNet.buildRunner.agent.RunnerParametersService;
import org.jetbrains.annotations.NotNull;

public class WindowsFileAccessService implements FileAccessService {
  public static final String CACLS_TOOL = "ICACLS";
  private static final Logger LOG = Logger.getInstance(WindowsFileAccessService.class.getName());
  private static final int EXECUTION_TIMEOUT_SECONDS = 600;
  private final CommandLineExecutor myCommandLineExecutor;

  public WindowsFileAccessService(
    @NotNull final CommandLineExecutor commandLineExecutor) {
    myCommandLineExecutor = commandLineExecutor;
  }

  public void setAccess(@NotNull final AccessControlList accessControlList) {
    for (AccessControlEntry entry : accessControlList) {
      applyAccessControlEntryForWindows(entry);
    }
  }

  private void applyAccessControlEntryForWindows(final AccessControlEntry entry) {
    final EnumSet<AccessPermissions> permissions = entry.getPermissions();
    if(permissions.size() == 0) {
      return;
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
    }

    if(username == null) {
      return;
    }

    String filePath = entry.getFile().getAbsolutePath();

    final ArrayList<CommandLineArgument> args = new ArrayList<CommandLineArgument>();
    args.add(new CommandLineArgument(filePath, CommandLineArgument.Type.PARAMETER));
    args.add(new CommandLineArgument("/C", CommandLineArgument.Type.PARAMETER));
    args.add(new CommandLineArgument("/Q", CommandLineArgument.Type.PARAMETER));

    if(permissions.contains(AccessPermissions.Revoke)) {
      args.add(new CommandLineArgument("/remove", CommandLineArgument.Type.PARAMETER));
      args.add(new CommandLineArgument(username, CommandLineArgument.Type.PARAMETER));
    }

    List<String> permissionList = new ArrayList<String>();
    if(permissions.contains(AccessPermissions.AllowRead)) {
      permissionList.add("R");
    }

    if(permissions.contains(AccessPermissions.AllowWrite)) {
      permissionList.add("W,D,DC");
    }

    if(permissions.contains(AccessPermissions.AllowExecute)) {
      permissionList.add("RX");
    }

    if(permissionList.size() > 0) {
      args.add(new CommandLineArgument("/grant", CommandLineArgument.Type.PARAMETER));
      final boolean recursive = permissions.contains(AccessPermissions.Recursive);
      final String permissionsStr = username + ":" + (recursive ? "(OI)(CI)" : "") + "(" + StringUtil.join(permissionList, ",") + ")";
      args.add(new CommandLineArgument(permissionsStr, CommandLineArgument.Type.PARAMETER));
    }

    final CommandLineSetup caclsCommandLineSetup = new CommandLineSetup(CACLS_TOOL, args, Collections.<CommandLineResource>emptyList());
    try {
      final ExecResult result = myCommandLineExecutor.runProcess(caclsCommandLineSetup, EXECUTION_TIMEOUT_SECONDS);
      ProcessResult(result);
    }
    catch (ExecutionException e) {
      LOG.error(e);
    }
  }

  private void ProcessResult(final ExecResult result) {
    if(result != null && result.getExitCode() != 0) {
      final String[] outLines = result.getOutLines();
      if(outLines != null && outLines.length > 0) {
        LOG.error(outLines);
      }

      final String stderr = result.getStderr();
      if(stderr.length() > 0) {
        LOG.error(stderr);
      }
    }
  }
}