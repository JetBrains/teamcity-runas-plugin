namespace JetBrains.runAs
{
	using System.Diagnostics;

	internal interface IProcessStartInfoFactory
	{
		[NotNull]
		ProcessStartInfo Create([NotNull] Settings settings);
	}
}