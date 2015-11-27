namespace JetBrains.runAs.Tests
{
	using System.Collections.Generic;

	using JetBrains.runAs.Future;
	using JetBrains.runAs.Sys;

	using Moq;

	using NUnit.Framework;

	using Shouldly;

	[TestFixture]
	public class CmdArgsSettingsProviderTest
	{
		private Mock<IEnvironment> _env;
		private Mock<ISettingsFactory> _settingsFactory;
		private Settings _settings;

		[SetUp]
		public void SetUp()
		{
			_env = new Mock<IEnvironment>();
			_settingsFactory = new Mock<ISettingsFactory>();			 

			_settings = new Settings("Nik", "JB", TestUtils.CreateSecureString("aaa"), "tool.exe")
			{
				WorkingDirectory = "wd",
				Args = new[] { "arg1", "arg2 " }
			};
		}

		[Test]
		public void ShouldGetUsageDescription()
		{
			// Given
			var instance = CreateInstance();

			// When						
			
			// Then
			instance.UsageDescription.ShouldNotBeNullOrEmpty();
		}

		private static readonly object[] IsAvailableCases =
		{
			new object[] { new [] {"tool", "arg1", "arg2", "arg3" }, true },
			new object[] { new [] {"tool", "arg1", "arg2", "arg3", "arg4" }, true },
			new object[] { new [] {"tool", "arg1", "arg2" }, false },
			new object[] { new [] {"tool" }, false },
			new object[] { Enumerable.Empty<string>(), false },
		};		

		[Test]
		[TestCaseSource("IsAvailableCases")]
		public void ShouldGetIsAvailable(IEnumerable<string> args, bool expectedIsAvailable)
		{
			// Given
			var instance = CreateInstance();

			// When			
			_env.Setup(i => i.GetCommandLineArgs()).Returns(args);
			var actualIsAvailable = instance.IsAvailable;

			// Then
			actualIsAvailable.ShouldBe(expectedIsAvailable);
		}

		[Test]
		public void ShouldGetSettings()
		{
			// Given
			var instance = CreateInstance();			

			// When
			_env.Setup(i => i.GetCommandLineArgs()).Returns(new [] {"tool", "arg1", "arg2" , "arg3" });
			_settingsFactory.Setup(i => i.Create(new[] { "arg1", "arg2", "arg3" })).Returns(_settings);

			// ReSharper disable once RedundantAssignment
			var settings = _settings;
			var actualIsAvailable = instance.TryGetSettings(out settings);

			// Then			
			actualIsAvailable.ShouldBeTrue();
			settings.ShouldBe(_settings);
		}

		[Test]
		public void ShouldNotGetSettingsWheneNoAvailableSettings()
		{
			// Given
			var instance = CreateInstance();

			// When
			_env.Setup(i => i.GetCommandLineArgs()).Returns(new[] { "tool", "arg1" });

			// ReSharper disable once RedundantAssignment
			var settings = _settings;
			var actualIsAvailable = instance.TryGetSettings(out settings);

			// Then			
			actualIsAvailable.ShouldBeFalse();			
		}

		private ISettingsProvider CreateInstance()
		{
			return new CmdArgsSettingsProvider(_env.Object, _settingsFactory.Object);
		}		
	}
}
