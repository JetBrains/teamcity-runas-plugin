namespace JetBrains.runAs
{
	using System;
	using System.Collections.Generic;

	using JetBrains.runAs.Future;
	using JetBrains.runAs.Sys;

	internal class SettingsProvider: ISettingsProvider
	{
		private readonly IEnvironment _environment;
		private readonly IEnumerable<ISettingsProvider> _settingsProviders;

		public SettingsProvider(
			[NotNull] IEnvironment environment,
			[NotNull] IEnumerable<ISettingsProvider> settingsProviders)
		{
			if (environment == null)
			{
				throw new ArgumentNullException("environment");
			}
			
			if (settingsProviders == null)
			{
				throw new ArgumentNullException("settingsProviders");
			}

			_environment = environment;
			_settingsProviders = Enumerable.ToList(settingsProviders);
		}

		public bool IsAvailable {
			get
			{
				return GetSettingsProvider() != null;
			}
		}

		public string UsageDescription {
			get
			{
				return string.Join(
					_environment.NewLine,
					Enumerable.ToArray(Enumerable.Select(_settingsProviders, i => i.UsageDescription)));
			}
		}

		public bool TryGetSettings(out Settings settings)
		{
			var settingsProvider = GetSettingsProvider();
			if (settingsProvider == null)
			{
				settings = default(Settings);
				return false;
			}

			return settingsProvider.TryGetSettings(out settings);
		}

		private ISettingsProvider GetSettingsProvider()
		{
			return Enumerable.FirstOrDefault(_settingsProviders, i => i.IsAvailable);			
		}
	}
}
