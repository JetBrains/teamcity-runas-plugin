namespace JetBrains.runAs.Sys
{
	using System;

	internal interface ITextStream: IDisposable
	{
		void WriteLine([CanBeNull] string line);
	}
}
