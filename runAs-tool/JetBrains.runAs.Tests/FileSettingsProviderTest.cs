namespace JetBrains.runAs.Tests
{
	using System.Collections.Generic;

	using JetBrains.runAs.Future;
	using JetBrains.runAs.Sys;

	using Moq;

	using NUnit.Framework;

	using Shouldly;

	[TestFixture]
	public class FileSettingsProviderTest
	{
		private Mock<IEnvironment> _env;
		private Mock<IFileSystem> _fileSystem;
		private Mock<ISettingsFactory> _settingsFactory;
		private Settings _settings;

		[SetUp]
		public void SetUp()
		{
			_env = new Mock<IEnvironment>();
			_fileSystem = new Mock<IFileSystem>();
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
			new object[] { new [] {"tool", "file" }, true, true },
			new object[] { new [] {"tool", "file" }, false, false },
			new object[] { new [] {"tool" }, true, false },
			new object[] { new [] {"tool", "file", "arg" }, true, false },
			new object[] { new [] {"tool" }, false, false },
			new object[] { Enumerable.Empty<string>(), false, false },
		};		

		[Test]
		[TestCaseSource("IsAvailableCases")]
		public void ShouldGetIsAvailable(IEnumerable<string> args, bool isFileExists, bool expectedIsAvailable)
		{
			// Given
			var instance = CreateInstance();

			// When			
			_env.Setup(i => i.GetCommandLineArgs()).Returns(args);
			_fileSystem.Setup(i => i.Exists(It.IsAny<string>())).Returns(isFileExists);
			var actualIsAvailable = instance.IsAvailable;

			// Then
			actualIsAvailable.ShouldBe(expectedIsAvailable);
		}

		[Test]
		public void ShouldGetSettings()
		{
			// Given
			var instance = CreateInstance();
			var fileContent = new[] { "line1", "line2" };

			// When
			_env.Setup(i => i.GetCommandLineArgs()).Returns(new [] {"tool", "file"});
			_fileSystem.Setup(i => i.Exists(It.IsAny<string>())).Returns(true);
			_fileSystem.Setup(i => i.ReadAllLines("file")).Returns(fileContent);
			_settingsFactory.Setup(i => i.Create(fileContent)).Returns(_settings);
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
			_env.Setup(i => i.GetCommandLineArgs()).Returns(new [] {"tool", "file"});
			_fileSystem.Setup(i => i.Exists(It.IsAny<string>())).Returns(false);
			
			// ReSharper disable once RedundantAssignment
			var settings = _settings;
			var actualIsAvailable = instance.TryGetSettings(out settings);

			// Then			
			actualIsAvailable.ShouldBeFalse();			
		}

		private ISettingsProvider CreateInstance()
		{
			return new FileSettingsProvider(_env.Object, _fileSystem.Object, _settingsFactory.Object);
		}		
	}
}
