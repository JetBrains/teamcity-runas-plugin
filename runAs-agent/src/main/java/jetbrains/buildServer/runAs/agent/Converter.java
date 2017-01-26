package jetbrains.buildServer.runAs.agent;

import org.jetbrains.annotations.NotNull;

public interface Converter<TSource, TDestination> {
  @NotNull TDestination convert(final @NotNull TSource source);
}
