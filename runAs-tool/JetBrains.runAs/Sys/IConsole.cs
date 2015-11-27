namespace JetBrains.runAs.Sys
{
	internal interface IConsole: ITextStream
	{		
		[NotNull]
		ITextStream OpenStandardOutput();

		[NotNull]
		ITextStream OpenStandardError();
	}
}