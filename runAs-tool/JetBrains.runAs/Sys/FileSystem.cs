namespace JetBrains.runAs.Sys
{
	using System;
	using System.Collections.Generic;
	using System.IO;

	internal class FileSystem : IFileSystem
	{
		public IEnumerable<string> ReadAllLines(string fileName)
		{
			if (fileName == null)
			{
				throw new ArgumentNullException("fileName");
			}

			return File.ReadAllLines(fileName);
		}

		public bool Exists(string fileName)
		{
			if (fileName == null)
			{
				throw new ArgumentNullException("fileName");
			}

			return File.Exists(fileName);
		}
	}
}
