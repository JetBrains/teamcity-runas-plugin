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
import org.jetbrains.annotations.NotNull;

import static jetbrains.buildServer.runAs.agent.Constants.CHMOD_TOOL_NAME;

public class LinuxFileAccessService implements FileAccessService {
  private static final Logger LOG = Logger.getInstance(LinuxFileAccessService.class.getName());
  private static final int EXECUTION_TIMEOUT_SECONDS = 600;
  private final CommandLineExecutor myCommandLineExecutor;

  public LinuxFileAccessService(    @NotNull final CommandLineExecutor commandLineExecutor) {
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

    final ArrayList<CommandLineArgument> args = new ArrayList<CommandLineArgument>();
    if (entry.getPermissions().contains(AccessPermissions.Recursive)) {
      args.add(new CommandLineArgument("-R", CommandLineArgument.Type.PARAMETER));
    }

    final StringBuilder permissionsSb = new StringBuilder();
    final AccessControlAccount account = entry.getAccount();
    switch (account.getTargetType()) {
      case User:
      case All:
        permissionsSb.append("a");
        break;
    }

    final StringBuilder allowPermissionsSb = new StringBuilder();
    if (permissions.contains(AccessPermissions.AllowRead)) {
      allowPermissionsSb.append("rX");
    }

    if (permissions.contains(AccessPermissions.AllowWrite)) {
      allowPermissionsSb.append('w');
    }

    if (permissions.contains(AccessPermissions.AllowExecute)) {
      allowPermissionsSb.append('x');
    }

    if(allowPermissionsSb.length() > 0)
    {
      allowPermissionsSb.insert(0, "+");
    }

    final StringBuilder denyPermissionsSb = new StringBuilder();
    if (permissions.contains(AccessPermissions.DenyRead)) {
      denyPermissionsSb.append("r");
    }

    if (permissions.contains(AccessPermissions.DenyWrite)) {
      denyPermissionsSb.append('w');
    }

    if (permissions.contains(AccessPermissions.DenyExecute)) {
      denyPermissionsSb.append('x');
    }

    if(denyPermissionsSb.length() > 0)
    {
      denyPermissionsSb.insert(0, "-");
    }

    if(denyPermissionsSb.length() == 0 && allowPermissionsSb.length() == 0) {
      return;
    }

    permissionsSb.append(allowPermissionsSb.toString());
    permissionsSb.append(denyPermissionsSb.toString());

    args.add(new CommandLineArgument(permissionsSb.toString(), CommandLineArgument.Type.PARAMETER));
    args.add(new CommandLineArgument(entry.getFile().getAbsolutePath(), CommandLineArgument.Type.PARAMETER));
    final CommandLineSetup chmodCommandLineSetup = new CommandLineSetup(CHMOD_TOOL_NAME, args, Collections.<CommandLineResource>emptyList());
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