package jetbrains.buildServer.runAs.agent;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import jetbrains.buildServer.util.pathMatcher.AntPatternFileCollector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AntPathMatcher implements PathMatcher {
  private static final AntPatternFileCollector.ScanOption[] ourScanOptions = new AntPatternFileCollector.ScanOption[] { AntPatternFileCollector.ScanOption.PRIORITIZE_EXCLUDES, AntPatternFileCollector.ScanOption.ALLOW_EXTERNAL_SCAN };

  @Override
  @NotNull
  public List<File> scanFiles(@NotNull final File baseDir, @NotNull final String[] includeRules, @NotNull final String[] excludeRules)
  {
    return AntPatternFileCollector.scanDir(baseDir, includeRules, excludeRules, ourScanOptions);
  }
}
