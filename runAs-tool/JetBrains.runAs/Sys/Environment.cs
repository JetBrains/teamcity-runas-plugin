namespace JetBrains.runAs.Sys
{
	using System.Collections.Generic;

	internal class Environment : IEnvironment
	{
		public string NewLine
		{
			get
			{
				return System.Environment.NewLine;
			}
		}

		public IEnumerable<string> GetCommandLineArgs()
		{
			return System.Environment.GetCommandLineArgs();
		}

		public void Exit(int exitCode)
		{
			System.Environment.Exit(exitCode);
		}
	}
}
