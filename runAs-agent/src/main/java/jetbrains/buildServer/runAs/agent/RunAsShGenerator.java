package jetbrains.buildServer.runAs.agent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import jetbrains.buildServer.dotNet.buildRunner.agent.ResourceGenerator;
import jetbrains.buildServer.messages.serviceMessages.Message;
import org.jetbrains.annotations.NotNull;

public class RunAsShGenerator implements ResourceGenerator<RunAsCmdSettings> {
  private static final String LINE_SEPARATOR = System.getProperty("line.separator");
  private static final String NORMAL_STATUS = "NORMAL";
  private static final List<Replacement> OurReplacements = Collections.unmodifiableList(Arrays.asList(
    new Replacement("^", "^^"),
    new Replacement("\\", "^\\"),
    new Replacement("&", "^&"),
    new Replacement("|", "^|"),
    new Replacement("'", "^'"),
    new Replacement(">", "^>"),
    new Replacement("<", "^<")));

  @NotNull
  @Override
  public String create(@NotNull final RunAsCmdSettings settings) {
    final StringBuilder sb = new StringBuilder();
    sb.append("@ECHO OFF");

    sb.append(LINE_SEPARATOR);
    sb.append("ECHO ");
    sb.append(ApplyCmdStringReplacements(new Message("Starting: " + settings.getCommandLine(), NORMAL_STATUS, null).toString()));

    sb.append(LINE_SEPARATOR);
    sb.append(settings.getCommandLine());

    return sb.toString();
  }

  @NotNull
  private static String ApplyCmdStringReplacements(@NotNull final String text)
  {
    String curText = text;
    for (Replacement replacement: OurReplacements) {
      curText = curText.replace(replacement.getTargetString(), replacement.getReplacementString());
    }

    return curText;
  }

  private static class Replacement
  {
    private final String myTargetString;
    private final String myReplacementString;

    Replacement(@NotNull final String targetString, @NotNull final String replacementString) {
      myTargetString = targetString;
      myReplacementString = replacementString;
    }

    @NotNull
    String getTargetString() {
      return myTargetString;
    }

    @NotNull
    String getReplacementString() {
      return myReplacementString;
    }
  }
}
