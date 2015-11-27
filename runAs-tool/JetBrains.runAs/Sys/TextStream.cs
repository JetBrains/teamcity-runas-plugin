namespace JetBrains.runAs.Sys
{
	using System;
	using System.IO;

	internal class TextStream: ITextStream
	{
		private readonly StreamWriter _streamWriter;

		public TextStream([NotNull] Stream stream)
		{
			if (stream == null)
			{
				throw new ArgumentNullException("stream");
			}

			_streamWriter = new StreamWriter(stream);
		}

		public void WriteLine(string line)
		{
			_streamWriter.WriteLine(line);
		}

		public void Dispose()
		{
			_streamWriter.Dispose();
		}
	}
}
