namespace JetBrains.runAs.Sys
{
	using System.Collections.Generic;

	internal interface IEnvironment
	{
		[NotNull]
		string NewLine { get; }

		[NotNull]
		IEnumerable<string> GetCommandLineArgs();

		void Exit(int exitCode);
	}
}