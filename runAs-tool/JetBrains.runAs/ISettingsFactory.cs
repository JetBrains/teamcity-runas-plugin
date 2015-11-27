namespace JetBrains.runAs
{
	using System.Collections.Generic;

	internal interface ISettingsFactory
	{
		[NotNull]
		Settings Create([NotNull] IEnumerable<string> settings);
	}
}