package jetbrains.buildServer.runAs.agent;

import com.intellij.openapi.diagnostic.Logger;
import java.io.File;
import java.io.IOException;
import jetbrains.buildServer.util.TCStreamUtil;
import org.jetbrains.annotations.NotNull;

public class FileAccessServiceImpl implements FileAccessService {
  private static final Logger LOG = Logger.getInstance(FileAccessServiceImpl.class.getName());
  
  public void setAccess(@NotNull final AccessControlList accessControlList) {
    for (AccessControlEntry entry: accessControlList)
    {
      final StringBuilder sb = new StringBuilder();
      if(entry.isRecursive()) {
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

      if(entry.isReading()) {
        sb.append('r');
      }

      if(entry.isWriting()) {
        sb.append('w');
      }

      if(entry.isExecuting()) {
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