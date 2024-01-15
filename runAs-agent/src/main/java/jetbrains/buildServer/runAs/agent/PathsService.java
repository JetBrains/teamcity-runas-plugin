

package jetbrains.buildServer.runAs.agent;

import java.io.File;
import org.jetbrains.annotations.NotNull;

public interface PathsService {
  @NotNull
  File getPath(WellKnownPaths wellKnownPath);
}