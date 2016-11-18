package jetbrains.buildServer.runAs.agent;

import com.intellij.openapi.diagnostic.Logger;
import java.io.File;
import java.io.IOException;
import jetbrains.buildServer.util.TCStreamUtil;
import org.jetbrains.annotations.NotNull;

public class FileAccessServiceImpl implements FileAccessService {
  private static final Logger LOG = Logger.getInstance(FileAccessServiceImpl.class.getName());

  @Override
  public void makeExecutableForAll(@NotNull final File executableFile) {
    try {
      TCStreamUtil.setFileMode(executableFile, "a+x");
    } catch (IOException e) {
      LOG.error(e);
    }
  }

  @Override
  public void makeExecutableForMe(@NotNull final File executableFile) {
    try {
      TCStreamUtil.setFileMode(executableFile, "+x");
    } catch (IOException e) {
      LOG.error(e);
    }
  }
}
