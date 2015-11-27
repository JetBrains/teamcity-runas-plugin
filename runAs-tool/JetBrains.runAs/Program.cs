namespace JetBrains.runAs
{
	using JetBrains.runAs.IoC;
	using JetBrains.runAs.Sys;

	public static class Program
	{		
		public static void Main()
		{
			var container = new RunAsIoCConfiguration().Apply(new Container());			
			container.Resolve<IEnvironment>().Exit(container.Resolve<IRunner>().RunAs());
		}
	}
}