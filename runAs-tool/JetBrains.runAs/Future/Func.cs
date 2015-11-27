namespace JetBrains.runAs.Future
{
	[CanBeNull]
	internal delegate T Func<T>();

	[CanBeNull]
	internal delegate T Func<T1, T>([NotNull] T1 arg1);
}
