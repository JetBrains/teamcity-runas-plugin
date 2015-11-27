namespace JetBrains.runAs.Sys
{
	using System;
	using System.IO;

	using JetBrains.runAs.IoC;

	internal class Console : IConsole
	{
		private readonly IResolver _iioCResolver;

		public Console([NotNull] IResolver iioCResolver)
		{
			if (iioCResolver == null)
			{
				throw new ArgumentNullException("iioCResolver");
			}

			_iioCResolver = iioCResolver;
		}

		public void WriteLine(string line)
		{
			if (string.IsNullOrEmpty(line))
			{
				return;
			}

			System.Console.WriteLine(line);			
		}

		public ITextStream OpenStandardOutput()
		{
			return _iioCResolver.Resolve<Stream, ITextStream>(System.Console.OpenStandardOutput());
		}

		public ITextStream OpenStandardError()
		{
			return _iioCResolver.Resolve<Stream, ITextStream>(System.Console.OpenStandardError());
		}

		public void Dispose()
		{			
		}
	}
}
