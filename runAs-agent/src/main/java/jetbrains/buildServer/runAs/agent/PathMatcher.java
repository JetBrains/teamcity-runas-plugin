package jetbrains.buildServer.runAs.agent;

import java.io.File;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public interface PathMatcher {
  @NotNull
  List<File> scanFiles(@NotNull final File baseDir, @NotNull final String[] includeRules, @NotNull final String[] excludeRules);
}
