namespace JetBrains.runAs.Sys
{
	using System.Collections.Generic;

	internal interface IFileSystem
	{
		[NotNull]
		IEnumerable<string> ReadAllLines([NotNull] string fileName);

		bool Exists([NotNull] string fileName);
	}
}