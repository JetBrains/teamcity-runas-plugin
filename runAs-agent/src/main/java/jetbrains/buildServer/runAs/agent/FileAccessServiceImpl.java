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
import jetbrains.buildServer.dotNet.buildRunner.agent.RunnerParametersService;
import org.jetbrains.annotations.NotNull;

public class FileAccessServiceImpl implements FileAccessService {
  private static final Logger LOG = Logger.getInstance(FileAccessServiceImpl.class.getName());
  private final RunnerParametersService myRunnerParametersService;
  private final CommandLineExecutor myCommandLineExecutor;

  public FileAccessServiceImpl(
    @NotNull final RunnerParametersService runnerParametersService,
    @NotNull final CommandLineExecutor commandLineExecutor) {
    myRunnerParametersService = runnerParametersService;
    myCommandLineExecutor = commandLineExecutor;
  }

  public void setAccess(@NotNull final AccessControlList accessControlList) {
    if(!myRunnerParametersService.isRunningUnderWindows()) {
      for (AccessControlEntry entry : accessControlList) {
        final EnumSet<AccessPermissions> permissions = entry.getPermissions();
        if(permissions.size() == 0) {
          continue;
        }

        final ArrayList<CommandLineArgument> args = new ArrayList<CommandLineArgument>();
        if (entry.isRecursive()) {
          args.add(new CommandLineArgument("-R", CommandLineArgument.Type.PARAMETER));
        }

        final StringBuilder permissionsSb = new StringBuilder();
        final AccessControlAccount account = entry.getAccount();
        switch (account.getTargetType()) {
          case All:
            permissionsSb.append("a");
            break;
        }

        final StringBuilder allowPermissionsSb = new StringBuilder();
        if (permissions.contains(AccessPermissions.AllowRead)) {
          allowPermissionsSb.append('r');
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
          denyPermissionsSb.append('r');
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

        permissionsSb.append(allowPermissionsSb.toString());
        permissionsSb.append(denyPermissionsSb.toString());

        args.add(new CommandLineArgument(permissionsSb.toString(), CommandLineArgument.Type.PARAMETER));
        args.add(new CommandLineArgument(entry.getFile().getAbsolutePath(), CommandLineArgument.Type.PARAMETER));
        final CommandLineSetup chmodCommandLineSetup = new CommandLineSetup("chmod", args, Collections.<CommandLineResource>emptyList());
        try {
          final ExecResult result = myCommandLineExecutor.runProcess(chmodCommandLineSetup, 600);
        }
        catch (ExecutionException e) {
          LOG.error(e);
        }
      }
    }
  }
}