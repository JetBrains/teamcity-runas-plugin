namespace JetBrains.runAs
{
	using System;
	using System.Collections.Generic;
	using System.Security;

	using JetBrains.runAs.Future;

	internal class Settings
	{
		private string _workingDirectory = string.Empty;
		private IEnumerable<string> _args = Enumerable.Empty<string>();

		public Settings(
			[NotNull] string user,
			[NotNull] string domain,
			[NotNull] SecureString password,
			[NotNull] string executable)
		{			
			if (user == null)
			{
				throw new ArgumentNullException("user");
			}

			if (domain == null)
			{
				throw new ArgumentNullException("domain");
			}

			if (password == null)
			{
				throw new ArgumentNullException("password");
			}

			if (executable == null)
			{
				throw new ArgumentNullException("executable");
			}			

			User = user;
			Domain = domain;
			Password = password;
			Executable = executable;			
		}

		public string User { [NotNull] get; private set; }

		public string Domain { [NotNull]get; private set; }

		public SecureString Password { [NotNull] get; private set; }

		[NotNull]
		public string WorkingDirectory
		{			
			get
			{
				return _workingDirectory;
			}

			set
			{
				if (value == null)
				{
					throw new ArgumentNullException("value");
				}
			
				_workingDirectory = value;
			}
		}

		public string Executable { [NotNull] get; private set; }

		[NotNull]
		public IEnumerable<string> Args
		{			
			get
			{
				return _args;
			}

			set
			{
				if (value == null)
				{
					throw new ArgumentNullException("value");
				}

				_args = Enumerable.ToList(value).AsReadOnly();
			}
		}

		public bool LaunchDebugger { get; set; }

		public override string ToString()
		{
			return string.Format(
				"User={0}, Domain={1}, WorkingDirectory={2}, Executable={3}",
				User,
				Domain,
				WorkingDirectory,
				Executable);
		}
	}
}
