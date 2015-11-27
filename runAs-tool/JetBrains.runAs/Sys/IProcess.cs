namespace JetBrains.runAs.Sys
{
	using System;

	internal interface IProcess : IDisposable
	{
		int Start(ITextStream standardOutput, ITextStream standardError);		
	}
}