namespace JetBrains.runAs.Tests
{
	using JetBrains.runAs.Sys;

	using Moq;

	using NUnit.Framework;

	[TestFixture]
	public class TeamCityFixture
	{
		private Mock<IConsole> _console;
		
		[SetUp]
		public void SetUp()
		{
			_console = new Mock<IConsole>();
		}

	
		[Test]
		public void ShouldWriteToConsoleWhenSendBuildProblem()
		{
			// Given
			var instance = CreateInstance();

			// When
			instance.SendBuildProblem("details");

			// Then
			_console.Verify(i => i.WriteLine("##teamcity[buildProblem description='details' identity='RunAsError']"));
		}
	
		private ITeamCity CreateInstance()
		{
			return new TeamCity(_console.Object);
		}
	}
}
