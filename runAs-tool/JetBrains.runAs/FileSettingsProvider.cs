namespace JetBrains.runAs
{
	using System;

	using JetBrains.runAs.Future;
	using JetBrains.runAs.Sys;

	internal class FileSettingsProvider : ISettingsProvider
	{
		private readonly IEnvironment _environment;
		private readonly IFileSystem _fileSystem;
		private readonly ISettingsFactory _settingsFactory;

		public FileSettingsProvider(
			[NotNull] IEnvironment environment,
			[NotNull] IFileSystem fileSystem,
			[NotNull] ISettingsFactory settingsFactory)
		{
			if (environment == null)
			{
				throw new ArgumentNullException("environment");
			}

			if (fileSystem == null)
			{
				throw new ArgumentNullException("fileSystem");
			}

			if (settingsFactory == null)
			{
				throw new ArgumentNullException("settingsFactory");
			}

			_environment = environment;
			_fileSystem = fileSystem;
			_settingsFactory = settingsFactory;
		}

		public bool IsAvailable
		{
			get
			{
				var args = Enumerable.ToArray(_environment.GetCommandLineArgs());
				if (args.Length != 2)
				{
					return false;
				}

				return _fileSystem.Exists(args[1]);
			}
		}

		public string UsageDescription
		{
			get
			{
				return string.Format(
					"{0}:\t{1} {2}\n\twhere {3} is a text file containing the following lines:\n\t\t{4}",
					WellknownVars.UsageStr,
					WellknownVars.ToolName,
					WellknownVars.ConfigStr,
					WellknownVars.ConfigStr,
					string.Join("\n\t\t", Enumerable.ToArray(WellknownVars.Args)));
			}
		}

		public bool TryGetSettings(out Settings settings)
		{
			if (!IsAvailable)
			{
				settings = default(Settings);
				return false;
			}

			var args = Enumerable.ToArray(_environment.GetCommandLineArgs());
			settings = _settingsFactory.Create(_fileSystem.ReadAllLines(args[1]));
			return true;
		}
	}
}