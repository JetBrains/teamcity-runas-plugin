namespace JetBrains.runAs.IoC
{
	internal interface IResolver
	{
		T Resolve<T>();

		T Resolve<TArg, T>([NotNull] TArg arg);

		T Resolve<T>([NotNull] string name);

		T Resolve<TArg, T>([NotNull] TArg arg, [NotNull] string name);		
	}
}