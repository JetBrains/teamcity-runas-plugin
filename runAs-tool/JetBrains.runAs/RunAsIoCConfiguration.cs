namespace JetBrains.runAs
{
	using System;
	using System.Diagnostics;
	using System.IO;

	using Future;
	using IoC;
	using Sys;

	using Console = Sys.Console;
	using Environment = Sys.Environment;

	internal class RunAsIoCConfiguration : IConfiguration
	{
		public IContainer Apply(IContainer container)
		{
			if (container == null)
			{
				throw new ArgumentNullException("container");
			}

			var env = new Lazy<IEnvironment>(() => new Environment());

			var fleSystem = new Lazy<IFileSystem>(() => new FileSystem());								

			var console = new Lazy<IConsole>(() => new Console(container));

			var teamCity = new Lazy<ITeamCity>(() => new TeamCity(container.Resolve<IConsole>()));

			var settingsFactory = new Lazy<ISettingsFactory>(() => new SettingsFactory());

			var cmdArgsSettingsProvider = new Lazy<ISettingsProvider>(() => new CmdArgsSettingsProvider(container.Resolve<IEnvironment>(), container.Resolve<ISettingsFactory>()));

			var fileSettingsProvider = new Lazy<ISettingsProvider>(() => new FileSettingsProvider(container.Resolve<IEnvironment>(), container.Resolve<IFileSystem>(), container.Resolve<ISettingsFactory>()));

			var settingsProvider = new Lazy<ISettingsProvider>(() => new SettingsProvider(
				container.Resolve<IEnvironment>(),
				new[] { container.Resolve<ISettingsProvider>("cmd"), container.Resolve<ISettingsProvider>("file") }));

			var processStartInfoFactory = new Lazy<IProcessStartInfoFactory>(() => new ProcessStartInfoFactory());			

			var runAsManager = new Lazy<IRunner>(() => new Runner(
				container.Resolve<ISettingsProvider>(),
				container.Resolve<IConsole>(),
				container.Resolve<IProcessStartInfoFactory>(),
				container,
				container.Resolve<ITeamCity>()));

			container
				.Register(() => env.Value)
				.Register(() => fleSystem.Value)
				.Register<Stream, ITextStream>(stream => new TextStream(stream))
				.Register<ProcessStartInfo, IProcess>(startInfo => new ProcessWin32(startInfo))
				.Register(() => console.Value)
				.Register(() => teamCity.Value)
				.Register(() => settingsFactory.Value)
				.Register(() => cmdArgsSettingsProvider.Value, "cmd")
				.Register(() => fileSettingsProvider.Value, "file")
				.Register(() => settingsProvider.Value)
				.Register(() => processStartInfoFactory.Value)				
				.Register(() => runAsManager.Value);

			return container;
		}

	}
}