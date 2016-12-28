package jetbrains.buildServer.runAs.agent;

import com.intellij.execution.ExecutionException;
import com.intellij.openapi.diagnostic.Logger;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import jetbrains.buildServer.ExecResult;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineArgument;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineResource;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineSetup;
import jetbrains.buildServer.dotNet.buildRunner.agent.RunnerParametersService;
import org.jetbrains.annotations.NotNull;

public class WindowsFileAccessService implements FileAccessService {
  public static final String CACLS_TOOL = "CACLS";
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
    String filePath = entry.getFile().getAbsolutePath();
    applyAccessControlEntryForWindows(entry, filePath);

    if(entry.getPermissions().contains(AccessPermissions.Recursive)) {
      filePath = new File(entry.getFile().getName() + "\\*").getAbsolutePath();
      applyAccessControlEntryForWindows(entry, filePath);
    }
  }

  private void applyAccessControlEntryForWindows(final AccessControlEntry entry, final String filePath) {
    final EnumSet<AccessPermissions> permissions = entry.getPermissions();
    if(permissions.size() == 0) {
      return;
    }

    final AccessControlAccount account = entry.getAccount();
    if(account.getTargetType() != AccessControlAccountType.User) {
      return;
    }

    final ArrayList<CommandLineArgument> args = new ArrayList<CommandLineArgument>();
    args.add(new CommandLineArgument(filePath, CommandLineArgument.Type.PARAMETER));
    args.add(new CommandLineArgument("/C", CommandLineArgument.Type.PARAMETER));
    args.add(new CommandLineArgument("/E", CommandLineArgument.Type.PARAMETER));

    Character allowPermissionChar = null;
    final boolean revoke = permissions.contains(AccessPermissions.Revoke);
    final boolean allowRead = permissions.contains(AccessPermissions.AllowRead) || permissions.contains(AccessPermissions.AllowExecute);
    final boolean allowWrite = permissions.contains(AccessPermissions.AllowWrite);

    if(allowRead && allowWrite) {
      allowPermissionChar = 'F';
    }
    else {
      if(allowRead) {
        allowPermissionChar = 'R';
      }

      if(allowWrite) {
        allowPermissionChar = 'C';
      }
    }

    if(revoke && allowPermissionChar != null) {
      // replace
      args.add(new CommandLineArgument("/P", CommandLineArgument.Type.PARAMETER));
    } else {
      if(revoke) {
        // revoke
        args.add(new CommandLineArgument("/R", CommandLineArgument.Type.PARAMETER));
      }

      if(allowPermissionChar != null) {
        // grant
        args.add(new CommandLineArgument("/G", CommandLineArgument.Type.PARAMETER));
      }
    }

    StringBuilder permissionsSb = new StringBuilder();
    permissionsSb.append('"');
    permissionsSb.append(account.getUserName());
    permissionsSb.append('"');
    if( allowPermissionChar != null) {
      permissionsSb.append(':');
      permissionsSb.append(allowPermissionChar);
    }

    args.add(new CommandLineArgument(permissionsSb.toString(), CommandLineArgument.Type.PARAMETER));

    final CommandLineSetup chmodCommandLineSetup = new CommandLineSetup(CACLS_TOOL, args, Collections.<CommandLineResource>emptyList());
    try {
      final ExecResult result = myCommandLineExecutor.runProcess(chmodCommandLineSetup, EXECUTION_TIMEOUT_SECONDS);
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