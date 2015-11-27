namespace JetBrains.runAs
{
	using System;
	using System.Diagnostics;

	using Future;

	internal class ProcessStartInfoFactory : IProcessStartInfoFactory
	{
		public ProcessStartInfo Create(Settings settings)
		{
			if (settings == null)
			{
				throw new ArgumentNullException("settings");
			}

			var startInfo = new ProcessStartInfo
			{
				UseShellExecute = false,
				ErrorDialog = false,
				WindowStyle = ProcessWindowStyle.Hidden,
				CreateNoWindow = true,
				RedirectStandardOutput = true,
				RedirectStandardError = true,
				RedirectStandardInput = false,
				FileName = settings.Executable,
				UserName = settings.User,
				Domain = settings.Domain,
				Password = settings.Password,
				Verb = "runas",
				Arguments = string.Join(" ", Enumerable.ToArray(settings.Args))
			};

			if (settings.WorkingDirectory != "")
			{
				startInfo.WorkingDirectory = settings.WorkingDirectory;
			}
			// Materialize environment variables, don't remove !!!
			// ReSharper disable once UnusedVariable
			var count = startInfo.EnvironmentVariables.Count;
			return startInfo;
		}
	}
}
