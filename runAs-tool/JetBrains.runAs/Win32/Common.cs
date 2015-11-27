namespace JetBrains.runAs.Win32
{
	using System;
	using System.Runtime.InteropServices;

	internal static class Common
	{
		[DllImport("kernel32", SetLastError = true)]
		internal static extern bool CloseHandle(IntPtr handle);

		[DllImport("kernel32.dll", SetLastError = true)]
		internal static extern IntPtr LocalFree(IntPtr hMem);
	}
}
