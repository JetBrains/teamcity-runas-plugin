namespace JetBrains.runAs.IntegrationTests.Dsl
{
	using System;
	using System.Diagnostics;
	using System.IO;
	using System.Linq;
	using System.Text;

	internal class RunAsRunner
	{
		public TestSession Run(TestContext ctx, CommandLineSetup setup)
		{
			var cmd = Path.Combine(ctx.SandboxPath, "run.cmd");
			File.WriteAllText(
				cmd,
				$"@pushd \"{ctx.CurrentDirectory}\""
				+ Environment.NewLine + $"@\"{setup.ToolName}\" " + string.Join(" ", setup.Arguments)
				+ Environment.NewLine + "@set exitCode=%errorlevel%"
				+ Environment.NewLine + "@popd"
				+ Environment.NewLine + "@exit /b %exitCode%");

			var output = new StringBuilder();
			var errors = new StringBuilder();
			var process = new Process();
			process.StartInfo.FileName = cmd;
			process.StartInfo.UseShellExecute = false;
			process.StartInfo.WindowStyle = ProcessWindowStyle.Hidden;
			process.StartInfo.CreateNoWindow = true;
			process.StartInfo.RedirectStandardOutput = true;
			process.StartInfo.RedirectStandardError = true;
			process.OutputDataReceived += (sender, args) => { output.AppendLine(args.Data); };
			process.ErrorDataReceived += (sender, args) => { errors.AppendLine(args.Data); };

			foreach (var envVariable in setup.EnvVariables)
			{
				process.StartInfo.EnvironmentVariables.Add(envVariable.Key, envVariable.Value);
			}

			process.Start();
			process.BeginOutputReadLine();
			process.BeginErrorReadLine();
			process.WaitForExit();			
			return new TestSession(ctx, process.ExitCode, output.ToString(), errors.ToString());
		}
	}
}
