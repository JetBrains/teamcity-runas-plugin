package jetbrains.buildServer.runAs.agent;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import jetbrains.buildServer.dotNet.buildRunner.agent.FileService;
import org.jetbrains.annotations.NotNull;

public class VirtualFileService implements FileService {
  private final HashMap<File, VirtualEntry> myEntries = new HashMap<File, VirtualEntry>();

  public VirtualFileService(@NotNull final VirtualEntry ... entries) {
    for(VirtualEntry entry: entries) {
      myEntries.put(entry.getPath(), entry);
    }
  }

  @Override
  public void validatePath(@NotNull final File file) {
  }

  @NotNull
  @Override
  public File getCheckoutDirectory() {
    return null;
  }

  @NotNull
  @Override
  public File getTempDirectory() {
    return null;
  }

  @NotNull
  @Override
  public File getTempFileName(@NotNull final String s) {
    return null;
  }

  @NotNull
  @Override
  public File getRelativePath(@NotNull final File file, @NotNull final File file1) {
    return null;
  }

  @Override
  public boolean isDirectory(@NotNull final File file) {
    return myEntries.get(file) instanceof VirtualDirectory;
  }

  @Override
  public boolean exists(@NotNull final File file) {
    return myEntries.containsKey(file);
  }

  @Override
  public boolean isAbsolute(@NotNull final File file) {
    return false;
  }

  @NotNull
  @Override
  public File[] listFiles(@NotNull final File file) {
    return new File[0];
  }

  @Override
  public String readAllTextFile(@NotNull final File file) throws IOException {
    return null;
  }

  @Override
  public void writeAllTextFile(@NotNull final String s, @NotNull final File file) throws IOException {
  }

  public interface VirtualEntry {
    public File getPath();
  }

  public static class VirtualDirectory implements VirtualEntry {
    private final File myPath;

    public VirtualDirectory(
      @NotNull final File path) {
      myPath = path;
    }

    public VirtualDirectory(
      @NotNull final String path) {
      this(new File(path));
    }

    @Override
    public File getPath() {
      return myPath;
    }
  }

  public static class VirtualFile implements VirtualEntry {
    private final File myPath;
    private final String myContent;

    public VirtualFile(
      @NotNull final File path,
      @NotNull final String content) {
      myPath = path;
      myContent = content;
    }

    public VirtualFile(
      @NotNull final String path,
      @NotNull final String content) {
      this(new File(path), content);
    }

    @Override
    public File getPath() {
      return myPath;
    }

    public String getContent() {
      return myContent;
    }
  }
}
