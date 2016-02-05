namespace JetBrains.runAs.IntegrationTests
{
	using System;
	using System.Linq;
	using System.Text.RegularExpressions;

	using JetBrains.runAs.IntegrationTests.Dsl;

	using NUnit.Framework;

	using TechTalk.SpecFlow;
	using System.Collections.Generic;

	using TestContext = JetBrains.runAs.IntegrationTests.Dsl.TestContext;

	[Binding]
    public class RunSteps
    {		
		[Given(@"I've added the argument (.+)")]
		public void AddArg(string arg)
		{
			var ctx = ScenarioContext.Current.GetTestContext();
			ctx.CommandLineSetup.Arguments.Add(arg);
		}

		[Given(@"I've defined the (.+) environment variable by the value (.+)")]
		public void AddEnvVar(string name, string value)
		{
			var ctx = ScenarioContext.Current.GetTestContext();
			ctx.CommandLineSetup.EnvVariables[name] = value;
		}

		[When(@"I run RunAs tool")]
        public void RunRunAsTool()
        {
			var ctx = ScenarioContext.Current.GetTestContext();
			var runner = new RunAsRunner();
			var testSession = runner.Run(ctx, ctx.CommandLineSetup);
			ctx.TestSession = testSession;
		}

		[Then(@"the exit code should be (-?\d+)")]
		public void VerifyExitCode(int expectedExitCode)
		{
			var ctx = ScenarioContext.Current.GetTestContext();
			Assert.AreEqual(expectedExitCode, ctx.TestSession.ExitCode, $"Invalid exit code.\nSee {ctx}");
		}

		[Then(@"the output should contain:")]
        public void CheckOutput(Table table)
        {
			var ctx = ScenarioContext.Current.GetTestContext();
			var testSession = ctx.TestSession;
			CheckText(ctx, testSession.Output, table);
        }

		[Then(@"the errors should contain:")]
		public void CheckErrors(Table table)
		{
			var ctx = ScenarioContext.Current.GetTestContext();
			var testSession = ctx.TestSession;
			CheckText(ctx, testSession.Errors, table);
		}

		private static void CheckText(TestContext ctx, string text, Table table)
		{
			var separator = new[] { Environment.NewLine };
			var lines = new List<string>(text.Split(separator, StringSplitOptions.None));
			var parrents = new List<Regex>(table.Rows.Select(i => new Regex(i[""], RegexOptions.IgnoreCase | RegexOptions.Singleline | RegexOptions.CultureInvariant | RegexOptions.Compiled)));

			while (lines.Count > 0 && parrents.Count > 0)
			{
				var line = lines[0];
				lines.RemoveAt(0);
				if (parrents[0].IsMatch(line))
				{
					parrents.RemoveAt(0);
				}
			}

			if (parrents.Any())
			{
				Assert.Fail($"Patterns are not matched:\n{string.Join(Environment.NewLine, parrents)}\nOutput:\n{text}\n\nSee {ctx}");
			}
		}
	}
}