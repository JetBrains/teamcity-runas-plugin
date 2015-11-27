namespace JetBrains.runAs
{
	using System;

	using JetBrains.runAs.Sys;

	internal class TeamCity : ITeamCity
	{
		private readonly IConsole _console;

		public TeamCity([NotNull] IConsole console)
		{
			if (console == null)
			{
				throw new ArgumentNullException("console");
			}

			_console = console;
		}

		public void SendBuildProblem(string description)
		{
			if (description == null)
			{
				throw new ArgumentNullException("description");
			}

			_console.WriteLine(
				string.Format(
					"##teamcity[buildProblem description='{0}' identity='{1}']",
					description,
					WellknownVars.TeamCityBuildProblemId));
		}
	}
}