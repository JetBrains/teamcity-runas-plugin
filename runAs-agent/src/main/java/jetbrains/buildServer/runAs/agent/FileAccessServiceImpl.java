package jetbrains.buildServer.runAs.agent;

import com.intellij.execution.ExecutionException;
import com.intellij.openapi.diagnostic.Logger;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import jetbrains.buildServer.ExecResult;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineArgument;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineResource;
import jetbrains.buildServer.dotNet.buildRunner.agent.CommandLineSetup;
import jetbrains.buildServer.dotNet.buildRunner.agent.RunnerParametersService;
import jetbrains.buildServer.util.TCStreamUtil;
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

        final StringBuilder permissionsStr = new StringBuilder();
        final AccessControlAccount account = entry.getAccount();
        switch (account.getTargetType()) {
          case All:
            permissionsStr.append("a+");
            break;
        }

        if (permissions.contains(AccessPermissions.Read)) {
          permissionsStr.append('r');
        }

        if (permissions.contains(AccessPermissions.Write)) {
          permissionsStr.append('w');
        }

        if (permissions.contains(AccessPermissions.Execute)) {
          permissionsStr.append('x');
        }

        args.add(new CommandLineArgument(permissionsStr.toString(), CommandLineArgument.Type.PARAMETER));
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