namespace JetBrains.runAs.Tests
{
	using System;

	using NUnit.Framework;

	using Shouldly;

	[TestFixture]
	public class SettingsFactoryTest
	{
		private static readonly object[] SettingsCases =
		{
			new object[] { new [] { "/u:Nik", "/p:Aaa", "/w:Wd", "/e:Tool", "/a:Arg1", "/a:Arg2" }, new Settings("Nik", "", TestUtils.CreateSecureString("Aaa"), "Tool") { WorkingDirectory = "Wd", Args = new[] { "Arg1", "Arg2" } }, false },
			new object[] { new [] { "/u:Nik", "/w:Wd", "/e:Tool" }, null, true },
			new object[] { new [] { "/u:Nik", "/p:Aaa", "/w:Wd" }, null, true },
			new object[] { new [] { "/u:Nik", "/p:Aaa", "/e:Tool" }, new Settings("Nik", "", TestUtils.CreateSecureString("Aaa"), "Tool"), false },
			new object[] { new [] { " /u : Nik", "/ p : Aaa", "/ w:Wd", "/e: Tool ", "/a: Arg1", " / a : Arg2 " }, new Settings("Nik", "", TestUtils.CreateSecureString("Aaa"), "Tool") { WorkingDirectory = "Wd", Args = new[] { "Arg1", "Arg2" } }, false },
			new object[] { new [] { "/u:Nik@Domain.ru", "/p:Aaa", "/e:Tool" }, new Settings("Nik", "Domain.ru", TestUtils.CreateSecureString("Aaa"), "Tool"), false },
			new object[] { new [] { @"/u:Domain.ru\Nik", "/p:Aaa", "/e:Tool" }, new Settings("Nik", "Domain.ru", TestUtils.CreateSecureString("Aaa"), "Tool"), false },
		};

		[Test]
		[TestCaseSource("SettingsCases")]
		public void ShouldCreateSettings(string[] args, object expectedSettingsObj, bool expectedThrowInvalidOperationException)
		{
			// Given
			var expectedSettings = (Settings)expectedSettingsObj;
			var instance = CreateInstance();
			var actualThrowInvalidOperationException = false;
			Settings actualSettings = null;

			// When
			try
			{
				actualSettings = instance.Create(args);
			}
			catch (InvalidOperationException)
			{
				actualThrowInvalidOperationException = true;
			}

			// Then
			actualThrowInvalidOperationException.ShouldBe(expectedThrowInvalidOperationException);			
			if (!actualThrowInvalidOperationException)
			{
				actualSettings.ShouldNotBeNull();
				actualSettings.User.ShouldBe(expectedSettings.User);
				actualSettings.Domain.ShouldBe(expectedSettings.Domain);
				actualSettings.WorkingDirectory.ShouldBe(expectedSettings.WorkingDirectory);
				actualSettings.Executable.ShouldBe(expectedSettings.Executable);
				actualSettings.Password.Length.ShouldBe(expectedSettings.Password.Length);
				actualSettings.Args.ShouldBe(expectedSettings.Args);												
			}
		}

		private static ISettingsFactory CreateInstance()
		{
			return new SettingsFactory();
		}		
	}
}
