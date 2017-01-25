package jetbrains.buildServer.runAs.agent;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import jetbrains.buildServer.dotNet.buildRunner.agent.FileService;
import org.jetbrains.annotations.NotNull;
import org.jmock.util.NotImplementedException;

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
    throw new NotImplementedException();
  }

  @NotNull
  @Override
  public File getTempDirectory() {
    throw new NotImplementedException();
  }

  @NotNull
  @Override
  public File getTempFileName(@NotNull final String s) {
    throw new NotImplementedException();
  }

  @NotNull
  @Override
  public File getRelativePath(@NotNull final File file, @NotNull final File file1) {
    throw new NotImplementedException();
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
    return myEntries.get(file).getIsAbsolute();
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

    public boolean getIsAbsolute();
  }

  public static class VirtualDirectory implements VirtualEntry {
    private final File myPath;
    private boolean myIsAbsolute = true;

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

    @Override
    public boolean getIsAbsolute() {
      return myIsAbsolute;
    }

    public void setIsAbsolute(boolean isAbsolute) {
      myIsAbsolute = isAbsolute;
    }
  }

  public static class VirtualFile implements VirtualEntry {
    private final File myPath;
    private final String myContent;
    private boolean myIsAbsolute = true;

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

    @Override
    public boolean getIsAbsolute() {
      return myIsAbsolute;
    }

    public void setIsAbsolute(boolean isAbsolute) {
      myIsAbsolute = isAbsolute;
    }

    public String getContent() {
      return myContent;
    }
  }
}
