namespace JetBrains.runAs
{
	using System;
	using System.Diagnostics;

	using JetBrains.runAs.IoC;
	using JetBrains.runAs.Sys;

	
	internal class Runner : IRunner
	{
		private readonly ISettingsProvider _settingsProvider;
		private readonly IConsole _console;
		private readonly IProcessStartInfoFactory _processStartInfoFactory;
		private readonly IResolver _resolver;
		private readonly ITeamCity _teamCity;

		public Runner(
			[NotNull] ISettingsProvider settingsProvider,
			[NotNull] IConsole console,
			[NotNull] IProcessStartInfoFactory processStartInfoFactory,
			[NotNull] IResolver resolver,
			[NotNull] ITeamCity teamCity)
		{
			if (settingsProvider == null)
			{
				throw new ArgumentNullException("settingsProvider");
			}

			if (console == null)
			{
				throw new ArgumentNullException("console");
			}

			if (processStartInfoFactory == null)
			{
				throw new ArgumentNullException("processStartInfoFactory");
			}

			if (resolver == null)
			{
				throw new ArgumentNullException("resolver");
			}

			if (teamCity == null)
			{
				throw new ArgumentNullException("teamCity");
			}

			_settingsProvider = settingsProvider;
			_console = console;
			_processStartInfoFactory = processStartInfoFactory;
			_resolver = resolver;
			_teamCity = teamCity;
		}

		public int RunAs()
		{
			try
			{
				using (var standardOutput = _console.OpenStandardOutput())
				{
					using (var standardError = _console.OpenStandardError())
					{
						Settings settings;
						if (!_settingsProvider.TryGetSettings(out settings))
						{
							standardError.WriteLine(_settingsProvider.UsageDescription);
							return -1;
						}

						if (settings.LaunchDebugger)
						{
							Debugger.Launch();
						}

						var startInfo = _processStartInfoFactory.Create(settings);
						standardOutput.WriteLine(string.Format("Starting: {0} {1}", startInfo.FileName, startInfo.Arguments));
						standardOutput.WriteLine(string.Format("in directory: {0}", startInfo.WorkingDirectory));
						using (var process = _resolver.Resolve<ProcessStartInfo, IProcess>(startInfo))
						{
							return process.Start(standardOutput, standardError);
						}						
					}
				}
			}
			catch (Exception ex)
			{
				_teamCity.SendBuildProblem(ex.Message);
			}

			return -1;
		}		
	}
}