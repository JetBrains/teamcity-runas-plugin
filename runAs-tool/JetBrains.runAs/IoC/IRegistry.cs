namespace JetBrains.runAs.IoC
{
	using JetBrains.runAs.Future;

	internal interface IRegistry
	{
		IRegistry Register<T>(Func<T> factory);

		IRegistry Register<TArg, T>(Func<TArg, T> factory);

		IRegistry Register<T>(Func<T> factory, [NotNull] string name);

		IRegistry Register<TArg, T>(Func<TArg, T> factory, [NotNull] string name);
	}
}