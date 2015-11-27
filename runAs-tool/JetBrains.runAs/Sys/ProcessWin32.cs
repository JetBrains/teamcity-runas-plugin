namespace JetBrains.runAs.Sys
{
	using System;
	using System.ComponentModel;
	using System.Diagnostics;
	using System.Runtime.InteropServices;
	using System.Security;

	using JetBrains.runAs.Win32;

	/// <summary>
	/// Add the following policies to the Administrator user:
	/// - Log on as a service
	/// - Act as part of the operating system
	/// - Adjust memory quotas for a process
	/// - eplace a process level token
	/// These policies can be added by opening Control Panel/Administrative Tools/Local Security Policy/User Rights Assignment.
	/// Once they are set, the policies don't take effect until next login.
	/// </summary>
	internal class ProcessWin32: IProcess
	{
		private readonly ProcessStartInfo _startInfo;

		public ProcessWin32(
			ProcessStartInfo startInfo)
		{
			_startInfo = startInfo;
		}

		public int Start(ITextStream standardOutput, ITextStream standardError)
		{
			var username = _startInfo.UserName;
			var domain = _startInfo.Domain;
			var password = SecureStringToString(_startInfo.Password);
			var applicationName = _startInfo.FileName;
			var currentDirectory = _startInfo.WorkingDirectory != string.Empty ? _startInfo.WorkingDirectory : null;
			var commandLine = _startInfo.FileName + " " + _startInfo.Arguments;

			var securityToken = IntPtr.Zero;
			var primarySecurityToken = IntPtr.Zero;
			try
			{
				// Attempt to log a user on to the local computer
				var result = Win32.Process.LogonUser(username, domain, password, (int)Win32.Process.LOGON_TYPE.LOGON32_LOGON_NETWORK, (int)Win32.Process.LOGON_PROVIDER.LOGON32_PROVIDER_DEFAULT, out securityToken);
				if (!result)
				{
					throw new Win32Exception(Marshal.GetLastWin32Error());
				}

				// Initialize a new security descriptor
				var processAttributes = new Win32.Process.SECURITY_ATTRIBUTES();
				var securityDescriptor = new Win32.Process.SECURITY_DESCRIPTOR();
				var securityDescriptorPtr = Marshal.AllocCoTaskMem(Marshal.SizeOf(securityDescriptor));
				Marshal.StructureToPtr(securityDescriptor, securityDescriptorPtr, false);
				Win32.Process.InitializeSecurityDescriptor(securityDescriptorPtr, Win32.Process.SECURITY_DESCRIPTOR_REVISION);
				securityDescriptor = (Win32.Process.SECURITY_DESCRIPTOR)Marshal.PtrToStructure(securityDescriptorPtr, typeof(Win32.Process.SECURITY_DESCRIPTOR));

				// Set information in a discretionary access control list (DACL). If a DACL is already present in the security descriptor, the DACL is replaced.
				result = Win32.Process.SetSecurityDescriptorDacl(ref securityDescriptor, true, IntPtr.Zero, false);
				if (!result)
				{
					throw new Win32Exception(Marshal.GetLastWin32Error());
				}

				// Creates a new access token that duplicates an existing token
				primarySecurityToken = new IntPtr();
				result = Win32.Process.DuplicateTokenEx(securityToken, 0, ref processAttributes, Win32.Process.SECURITY_IMPERSONATION_LEVEL.SecurityImpersonation, Win32.Process.TOKEN_TYPE.TokenPrimary, out primarySecurityToken);
				if (!result)
				{
					throw new Win32Exception(Marshal.GetLastWin32Error());
				}

				// Create a new process and its primary thread. The new process runs in the security context of the user represented by the specified token.
				processAttributes.SecurityDescriptor = securityDescriptorPtr;
				processAttributes.Length = (uint)Marshal.SizeOf(securityDescriptor);
				processAttributes.InheritHandle = true;

				var threadAttributes = new Win32.Process.SECURITY_ATTRIBUTES
				{
					SecurityDescriptor = IntPtr.Zero,
					Length = 0,
					InheritHandle = false
				};

				// CreationFlags creationFlags = CreationFlags.CREATE_DEFAULT_ERROR_MODE;
				var environment = IntPtr.Zero;
				var startupInfo = new Win32.Process.STARTUPINFO
				{
					Desktop = ""
				};

				Win32.Process.PROCESS_INFORMATION processInformation;
				result = Win32.Process.CreateProcessAsUser(primarySecurityToken, applicationName, commandLine, ref processAttributes, ref threadAttributes, true, 16, environment, currentDirectory, ref startupInfo, out processInformation);
				if (!result)
				{
					throw new Win32Exception(Marshal.GetLastWin32Error());
				}
			}
			catch
			{
				throw new Win32Exception(Marshal.GetLastWin32Error());
			}
			finally
			{
				if (securityToken != IntPtr.Zero)
				{					
					var result = Common.CloseHandle(securityToken);
					if (!result)
					{
						throw new Win32Exception(Marshal.GetLastWin32Error());
					}

					result = Common.CloseHandle(primarySecurityToken);
					if (!result)
					{
						throw new Win32Exception(Marshal.GetLastWin32Error());
					}
				}
			}

			return 0;
		}

		public void Dispose()
		{
		}

		private static string SecureStringToString(SecureString value)
		{
			var bstr = Marshal.SecureStringToBSTR(value);
			try
			{
				return Marshal.PtrToStringBSTR(bstr);
			}
			finally
			{
				Marshal.FreeBSTR(bstr);
			}
		}
	}
}
