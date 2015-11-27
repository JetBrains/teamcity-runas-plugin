namespace JetBrains.runAs
{
	using System;
	using System.Collections.Generic;
	using System.Security;
	using System.Text.RegularExpressions;

	using JetBrains.runAs.Future;

	internal class SettingsFactory : ISettingsFactory
	{
		private static readonly Regex ArgRegex = new Regex(@"\s*/\s*(?<name>\w)+\s*:\s*(?<value>.+)\s*$", RegexOptions.Compiled | RegexOptions.CultureInvariant | RegexOptions.IgnoreCase | RegexOptions.Singleline);
		private static readonly Regex UserRegex = new Regex(@"^(?<name>[^@\\]+)@(?<domain>[^@\\]+)$|^(?<domain>[^@\\]+)\\(?<name>[^@\\]+)$|(?<name>^[^@\\]+$)", RegexOptions.Compiled | RegexOptions.CultureInvariant | RegexOptions.IgnoreCase | RegexOptions.Singleline);

		public Settings Create(IEnumerable<string> settings)
		{
			if (settings == null)
			{
				throw new ArgumentNullException("settings");
			}

			string user = null;
			var domain = "";
			var password = new SecureString();
			var workingDirectory = "";
			string executable = null;
			var arguments = new List<string>();
			var launchDebugger = false;

			foreach (var setting in settings)
			{
				var argMatch = ArgRegex.Match(setting);
				if (!argMatch.Success)
				{
					throw new InvalidOperationException(string.Format("Invalid argument \"{0}\"", setting));
                }

				var name = argMatch.Groups["name"].Value.Trim().ToLower();
				var value = argMatch.Groups["value"].Value.Trim();
				switch (name)
				{
					case "u":
						var userMatch = UserRegex.Match(value);
						if (!userMatch.Success)
						{
							throw new InvalidOperationException(string.Format("Invalid argument \"{0}\"", setting));
						}

						var userCapture = userMatch.Groups["name"];
						if (userCapture.Success)
						{
							user = userCapture.Value;
						}

						var domainCapture = userMatch.Groups["domain"];
						if (domainCapture.Success)
						{
							domain = domainCapture.Value;
						}

						break;

					case "p":					
						password.Clear();
						Enumerable.ToList(value.ToCharArray()).ForEach(i => password.AppendChar(i));
						break;

					case "w":					
						workingDirectory = value;
						break;

					case "e":					
						executable = value;
						break;

					case "a":
						arguments.Add(value);
						break;

					case "d":
						if (bool.TryParse(value, out launchDebugger))
						{
							launchDebugger = false;
						}

						break;

					default:
						throw new InvalidOperationException(string.Format("Unknown argument \"{0}\"", name));						
				}
			}

			CheckRequiredArg(WellknownVars.UserStr, !string.IsNullOrEmpty(user));
			CheckRequiredArg(WellknownVars.PasswordStr, password.Length != 0);
			CheckRequiredArg(WellknownVars.ExecutableFileStr, !string.IsNullOrEmpty(executable));

			password.MakeReadOnly();

			return new Settings(
				user,
				domain,
				password,
				executable)
			{
				WorkingDirectory = workingDirectory,
				Args = arguments,
				LaunchDebugger = launchDebugger
			};
		}
		
		// ReSharper disable once UnusedParameter.Local
		[AssertionMethod]
		private static void CheckRequiredArg(string name, [AssertionCondition(AssertionConditionType.IS_TRUE)] bool exists)
		{
			if (!exists)
			{
				throw new InvalidOperationException(string.Format("The required argument \"{0}\" is not exist", name));
            }
		}
	}
}
