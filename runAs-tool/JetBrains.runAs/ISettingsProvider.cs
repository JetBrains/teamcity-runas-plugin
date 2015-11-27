namespace JetBrains.runAs
{
	internal interface ISettingsProvider
	{
		bool IsAvailable { get; }

		[NotNull]
		string UsageDescription { get; }

		[MustUseReturnValueAttribute]
		bool TryGetSettings(out Settings settings);
	}
}