namespace JetBrains.runAs.Tests
{
	using System.Security;

	using JetBrains.runAs.Future;

	internal static class TestUtils
	{
		public static SecureString CreateSecureString(string str)
		{
			var secureStr = new SecureString();
			Enumerable.ToList(str.ToCharArray()).ForEach(i => secureStr.AppendChar(i));
			return secureStr;
		}
	}
}
