namespace JetBrains.runAs.IoC
{
	internal interface IConfiguration
	{
		[NotNull]
		IContainer Apply([NotNull] IContainer container);
	}
}