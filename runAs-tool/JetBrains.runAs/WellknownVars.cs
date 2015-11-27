namespace JetBrains.runAs
{
	using System.Collections.Generic;
	using System.Reflection;

	internal static class WellknownVars
	{
		public const string TeamCityBuildProblemId = "RunAsError";

		public const string UsageStr = "Usage";
		public const string ConfigStr = "configuration_file";
		public const string UserStr = "user";
		public const string PasswordStr = "password";
		public const string ExecutableFileStr = "executable_file";
		public static readonly IEnumerable<string> Args = new List<string>
		{
			string.Format("/u:{0}", UserStr),
			string.Format("/p:{0}", PasswordStr),
			string.Format("/e:{0}", ExecutableFileStr),
			"[/w:working_directory]",			
			"[/a:arg1]", "[/a:arg2]", "...", "[/a:argN]"
		}.AsReadOnly();

		public static string ToolName
		{
			get
			{
				return Assembly.GetExecutingAssembly().GetName().Name;
			}
		}
	}
}
