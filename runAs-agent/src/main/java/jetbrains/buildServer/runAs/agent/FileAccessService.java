package jetbrains.buildServer.runAs.agent;

import java.io.File;
import org.jetbrains.annotations.NotNull;

public interface FileAccessService {
  public void makeExecutableForAll(@NotNull final File executableFile);

  public void makeExecutableForMe(@NotNull final File executableFile);
}
