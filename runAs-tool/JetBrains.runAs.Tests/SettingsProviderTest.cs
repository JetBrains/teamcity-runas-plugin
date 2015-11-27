namespace JetBrains.runAs.Tests
{
	using System.Collections.Generic;

	using JetBrains.runAs.Sys;

	using Moq;

	using NUnit.Framework;

	using Shouldly;

	[TestFixture]
	public class SettingsProviderTest
	{
		private Mock<IEnvironment> _env;
		private Mock<ISettingsProvider> _settingsProvider1;
		private Mock<ISettingsProvider> _settingsProvider2;
		private Settings _settings;

		[SetUp]
		public void SetUp()
		{
			_env = new Mock<IEnvironment>();
			_settingsProvider1 = new Mock<ISettingsProvider>();
			_settingsProvider2 = new Mock<ISettingsProvider>();

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
			_env.SetupGet(i => i.NewLine).Returns("<br/>");
			_settingsProvider1.SetupGet(i => i.UsageDescription).Returns("desc1");
			_settingsProvider2.SetupGet(i => i.UsageDescription).Returns("desc2");

			// Then
			instance.UsageDescription.ShouldBe("desc1<br/>desc2");
		}

		private static readonly object[] IsAvailableCases =
		{
			new object[] { true, true, true },
			new object[] { true, false, true },
			new object[] { false, true, true },
			new object[] { false, false, false },
		};		

		[Test]
		[TestCaseSource("IsAvailableCases")]
		public void ShouldGetIsAvailable(bool isAvailable1, bool isAvailable2, bool expectedIsAvailable)
		{
			// Given
			var instance = CreateInstance();

			// When			
			_settingsProvider1.SetupGet(i => i.IsAvailable).Returns(isAvailable1);
			_settingsProvider2.SetupGet(i => i.IsAvailable).Returns(isAvailable2);
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
			_settingsProvider1.SetupGet(i => i.IsAvailable).Returns(false);
			_settingsProvider2.SetupGet(i => i.IsAvailable).Returns(true);
			var settings = _settings;
			_settingsProvider2.Setup(i => i.TryGetSettings(out settings)).Returns(true);
			Settings actualSettings;
			var actualHasSettings = instance.TryGetSettings(out actualSettings);

			// Then
			actualHasSettings.ShouldBeTrue();
			actualSettings.ShouldBe(settings);
			_settingsProvider1.Verify(i => i.TryGetSettings(out settings), Times.Never);
		}

		[Test]
		public void ShouldNotGetSettingsWheneNoAvailableSettings()
		{
			// Given
			var instance = CreateInstance();

			// When			
			_settingsProvider1.SetupGet(i => i.IsAvailable).Returns(false);
			_settingsProvider2.SetupGet(i => i.IsAvailable).Returns(false);
			Settings actualSettings;
			var actualHasSettings = instance.TryGetSettings(out actualSettings);

			// Then
			actualHasSettings.ShouldBeFalse();
			Settings settings;
			_settingsProvider1.Verify(i => i.TryGetSettings(out settings), Times.Never);
			_settingsProvider2.Verify(i => i.TryGetSettings(out settings), Times.Never);
		}

		private ISettingsProvider CreateInstance()
		{
			return new SettingsProvider(_env.Object, new List <ISettingsProvider> { _settingsProvider1.Object, _settingsProvider2.Object });
		}		
	}
}
