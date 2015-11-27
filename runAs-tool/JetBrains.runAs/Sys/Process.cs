namespace JetBrains.runAs.Sys
{
	using System.Diagnostics;

	internal class Process : IProcess
	{
		private readonly System.Diagnostics.Process _process;

		public Process(
			ProcessStartInfo startInfo)
		{
			_process = new System.Diagnostics.Process { StartInfo = startInfo };			
		}

		public int Start(ITextStream standardOutput, ITextStream standardError)
		{
			_process.Start();
			_process.OutputDataReceived += (sender, args) => OnOutputReceived(standardOutput, args.Data);
			_process.BeginOutputReadLine();
			_process.ErrorDataReceived += (sender, args) => OnOutputReceived(standardError, args.Data);
			_process.BeginErrorReadLine();			
			_process.WaitForExit();
			return _process.ExitCode;
		}
		
		public void Dispose()
		{
			_process.Dispose();
		}

		private static void OnOutputReceived(ITextStream textStream, string line)
		{
			if (string.IsNullOrEmpty(line))
			{
				return;
			}

			textStream.WriteLine(line);
		}
	}
}