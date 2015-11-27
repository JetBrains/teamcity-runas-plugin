namespace JetBrains.runAs
{
	using System;

	using JetBrains.runAs.Future;
	using JetBrains.runAs.Sys;

	internal class CmdArgsSettingsProvider : ISettingsProvider
	{
		private readonly IEnvironment _environment;
		private readonly ISettingsFactory _settingsFactory;

		public CmdArgsSettingsProvider(
			[NotNull] IEnvironment environment,
			[NotNull] ISettingsFactory settingsFactory)
		{
			if (environment == null)
			{
				throw new ArgumentNullException("environment");
			}
			if (settingsFactory == null)
			{
				throw new ArgumentNullException("settingsFactory");
			}

			_environment = environment;
			_settingsFactory = settingsFactory;
		}

		public bool IsAvailable {
			get
			{
				return Enumerable.Count(_environment.GetCommandLineArgs()) >= 4;
			}
		}

		public string UsageDescription
		{
			get
			{
				return string.Format(
					"{0}:\t{1} {2}",
					WellknownVars.UsageStr,
					WellknownVars.ToolName,
					string.Join(" ", Enumerable.ToArray(WellknownVars.Args)));
			}
		}

		public bool TryGetSettings(out Settings settings)
		{
			if (!IsAvailable)
			{				
				settings = default(Settings);
				return false;
			}

			settings = _settingsFactory.Create(Enumerable.Skip(_environment.GetCommandLineArgs(), 1));
			return true;
		}
	}
}
