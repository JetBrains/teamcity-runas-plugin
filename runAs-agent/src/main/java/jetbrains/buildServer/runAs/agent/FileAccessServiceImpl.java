package jetbrains.buildServer.runAs.agent;

import com.intellij.openapi.diagnostic.Logger;
import java.io.File;
import java.io.IOException;
import jetbrains.buildServer.dotNet.buildRunner.agent.RunnerParametersService;
import jetbrains.buildServer.util.TCStreamUtil;
import org.jetbrains.annotations.NotNull;

public class FileAccessServiceImpl implements FileAccessService {
  private static final Logger LOG = Logger.getInstance(FileAccessServiceImpl.class.getName());
  private final RunnerParametersService myRunnerParametersService;

  public FileAccessServiceImpl(
    @NotNull final RunnerParametersService runnerParametersService) {
    myRunnerParametersService = runnerParametersService;
  }

  public void setAccess(@NotNull final AccessControlList accessControlList) {
    if(!myRunnerParametersService.isRunningUnderWindows()) {
      for (AccessControlEntry entry : accessControlList) {
        final StringBuilder sb = new StringBuilder();
        final Boolean isRecursive = entry.isRecursive();
        if (isRecursive != null && isRecursive) {
          sb.append("-R");
        }

        final AccessControlAccount account = entry.getAccount();
        switch (account.getTargetType()) {
          case All:
            sb.append("a+");
            break;

          case User:
            sb.append(account.getTargetName());
            sb.append(" u+");
            break;
        }

        final Boolean isReading = entry.isReading();
        if (isReading != null && isReading) {
          sb.append('r');
        }

        final Boolean isWriting = entry.isWriting();
        if (isWriting != null && isWriting) {
          sb.append('w');
        }

        final Boolean isExecuting = entry.isExecuting();
        if (isExecuting != null && isExecuting) {
          sb.append('x');
        }

        try {
          TCStreamUtil.setFileMode(entry.getFile(), sb.toString());
        } catch (IOException e) {
          LOG.error(e);
        }
      }
    }
  }
}