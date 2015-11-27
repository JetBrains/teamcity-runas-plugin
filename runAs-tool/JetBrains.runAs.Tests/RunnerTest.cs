namespace JetBrains.runAs.Tests
{
	using System;
	using System.Diagnostics;

	using JetBrains.runAs.IoC;
	using JetBrains.runAs.Sys;

	using Moq;

	using NUnit.Framework;

	using Shouldly;
	
	[TestFixture]
	public class RunnerTest
	{
		private Mock<ISettingsProvider> _settingsProvider;
		private Mock<IConsole> _console;
		private Mock<ITeamCity> _teamCity;
		private Settings _settings;
		private Mock<ITextStream> _standardOutput;
		private Mock<ITextStream> _standardError;
		private Mock<IProcessStartInfoFactory> _processStartInfoFactory;
		private Mock<IResolver> _resolver;
		private ProcessStartInfo _processStartInfo;
		private Mock<IProcess> _process;

		[SetUp]
		public void SetUp()
		{
			_settingsProvider = new Mock<ISettingsProvider>();
			_console = new Mock<IConsole>();
			_processStartInfoFactory = new Mock<IProcessStartInfoFactory>();
			_resolver = new Mock<IResolver>();			
			_teamCity = new Mock<ITeamCity>();
			_settings = new Settings("Nik", "JB", TestUtils.CreateSecureString("aaa"), "tool.exe")
			{
				WorkingDirectory = "wd", Args = new[] { "arg1", "arg2 " }
			};

			_standardOutput = new Mock<ITextStream>();
			_standardError = new Mock<ITextStream>();
			_processStartInfo = new ProcessStartInfo("tool", "arg1 arg2") { WorkingDirectory = "wd" };
			_process = new Mock<IProcess>();
		}

	
		[Test]
		public void ShouldRunProcess()
		{
			// Given
			var instance = CreateInstance();

			// When
			// ReSharper disable once RedundantAssignment
			var settings = _settings;
			_settingsProvider.Setup(i => i.TryGetSettings(out settings)).Returns(true);
			_console.Setup(i => i.OpenStandardOutput()).Returns(_standardOutput.Object);
			_console.Setup(i => i.OpenStandardError()).Returns(_standardError.Object);
			_processStartInfoFactory.Setup(i => i.Create(settings)).Returns(_processStartInfo);
			_resolver.Setup(i => i.Resolve<ProcessStartInfo, IProcess>(_processStartInfo)).Returns(_process.Object);
			_process.Setup(i => i.Start(_standardOutput.Object, _standardError.Object)).Returns(5);

			var actualExitCode = instance.RunAs();

			// Then			
			actualExitCode.ShouldBe(5);
			_standardOutput.Verify(i => i.WriteLine(It.Is<string>(s => s == "Starting: tool arg1 arg2")));
			_standardOutput.Verify(i => i.WriteLine(It.Is<string>(s => s == "in directory: wd")));
			_standardOutput.Verify(i => i.Dispose());
			_standardError.Verify(i => i.Dispose());
		}

		[Test]
		public void ShouldWriteUsageDescriptionAndExitWithCodeMinus1WhenSettignsIsNotAvailable()
		{
			// Given
			var instance = CreateInstance();

			// When
			// ReSharper disable once RedundantAssignment
			Settings settings = null;
			_settingsProvider.Setup(i => i.TryGetSettings(out settings)).Returns(false);
			_console.Setup(i => i.OpenStandardOutput()).Returns(_standardOutput.Object);
			_console.Setup(i => i.OpenStandardError()).Returns(_standardError.Object);
			_settingsProvider.SetupGet(i => i.UsageDescription).Returns("usage");

			var actualExitCode = instance.RunAs();

			// Then
			_standardError.Verify(i => i.WriteLine("usage"));
			actualExitCode.ShouldBe(-1);
			_standardOutput.Verify(i => i.Dispose());
			_standardError.Verify(i => i.Dispose());
		}

		[Test]
		public void ShouldSendBuildProblemToTeamCityAndExitWithCodeMinus1WhenExceptionOccured()
		{
			// Given
			var instance = CreateInstance();
			var testException = new Exception("test");

			// When
			// ReSharper disable once RedundantAssignment
			var settings = _settings;
			_settingsProvider.Setup(i => i.TryGetSettings(out settings)).Returns(true);
			_console.Setup(i => i.OpenStandardOutput()).Returns(_standardOutput.Object);
			_console.Setup(i => i.OpenStandardError()).Returns(_standardError.Object);
			_processStartInfoFactory.Setup(i => i.Create(settings)).Returns(_processStartInfo);
			_resolver.Setup(i => i.Resolve<ProcessStartInfo, IProcess>(_processStartInfo)).Returns(_process.Object);
			_process.Setup(i => i.Start(_standardOutput.Object, _standardError.Object)).Throws(testException);

			var actualExitCode = instance.RunAs();

			// Then
			_teamCity.Verify(i => i.SendBuildProblem(testException.Message));
			actualExitCode.ShouldBe(-1);
			_standardOutput.Verify(i => i.Dispose());
			_standardError.Verify(i => i.Dispose());
		}

		private IRunner CreateInstance()
		{
			return new Runner(
				_settingsProvider.Object,
				_console.Object,
				_processStartInfoFactory.Object,
				_resolver.Object,
				_teamCity.Object);
		}
	}
}
