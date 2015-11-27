// ReSharper disable MemberCanBePrivate.Global
// ReSharper disable BuiltInTypeReferenceStyle
// ReSharper disable FieldCanBeMadeReadOnly.Global
// ReSharper disable UnusedMember.Local
// ReSharper disable InconsistentNaming
// ReSharper disable UnusedMember.Global
#pragma warning disable 169
namespace JetBrains.runAs.Win32
{
	using System;
	using System.Runtime.InteropServices;

	internal static class Process
	{
		[StructLayout(LayoutKind.Sequential)]
		public struct LUID
		{
			public UInt32 LowPart;
			public Int32 HighPart;
		}

		[StructLayout(LayoutKind.Sequential)]
		public struct LUID_AND_ATTRIBUTES
		{
			public LUID Luid;
			public UInt32 Attributes;
		}

		public struct TOKEN_PRIVILEGES
		{
			public UInt32 PrivilegeCount;
			[MarshalAs(UnmanagedType.ByValArray, SizeConst = 1)]
			public LUID_AND_ATTRIBUTES[] Privileges;
		}

		public enum TOKEN_INFORMATION_CLASS
		{
			TokenUser = 1,
			TokenGroups,
			TokenPrivileges,
			TokenOwner,
			TokenPrimaryGroup,
			TokenDefaultDacl,
			TokenSource,
			TokenType,
			TokenImpersonationLevel,
			TokenStatistics,
			TokenRestrictedSids,
			TokenSessionId,
			TokenGroupsAndPrivileges,
			TokenSessionReference,
			TokenSandBoxInert,
			TokenAuditPolicy,
			TokenOrigin,
			TokenElevationType,
			TokenLinkedToken,
			TokenElevation,
			TokenHasRestrictions,
			TokenAccessInformation,
			TokenVirtualizationAllowed,
			TokenVirtualizationEnabled,
			TokenIntegrityLevel,
			TokenUIAccess,
			TokenMandatoryPolicy,
			TokenLogonSid,
			MaxTokenInfoClass
		}

		[Flags]
		public enum CreationFlags : uint
		{
			CREATE_BREAKAWAY_FROM_JOB = 0x01000000,
			CREATE_DEFAULT_ERROR_MODE = 0x04000000,
			CREATE_NEW_CONSOLE = 0x00000010,
			CREATE_NEW_PROCESS_GROUP = 0x00000200,
			CREATE_NO_WINDOW = 0x08000000,
			CREATE_PROTECTED_PROCESS = 0x00040000,
			CREATE_PRESERVE_CODE_AUTHZ_LEVEL = 0x02000000,
			CREATE_SEPARATE_WOW_VDM = 0x00001000,
			CREATE_SUSPENDED = 0x00000004,
			CREATE_UNICODE_ENVIRONMENT = 0x00000400,
			DEBUG_ONLY_THIS_PROCESS = 0x00000002,
			DEBUG_PROCESS = 0x00000001,
			DETACHED_PROCESS = 0x00000008,
			EXTENDED_STARTUPINFO_PRESENT = 0x00080000
		}

		public enum TOKEN_TYPE
		{
			TokenPrimary = 1,
			TokenImpersonation
		}

		public enum SECURITY_IMPERSONATION_LEVEL
		{
			SecurityAnonymous,
			SecurityIdentification,
			SecurityImpersonation,
			SecurityDelegation
		}

		[Flags]
		public enum LogonFlags
		{
			LOGON_NETCREDENTIALS_ONLY = 2,
			LOGON_WITH_PROFILE = 1
		}
	
		public enum LOGON_TYPE
		{
			LOGON32_LOGON_INTERACTIVE = 2,
			LOGON32_LOGON_NETWORK,
			LOGON32_LOGON_BATCH,
			LOGON32_LOGON_SERVICE,
			LOGON32_LOGON_UNLOCK = 7,
			LOGON32_LOGON_NETWORK_CLEARTEXT,
			LOGON32_LOGON_NEW_CREDENTIALS
		}

		public enum LOGON_PROVIDER
		{
			LOGON32_PROVIDER_DEFAULT,
			LOGON32_PROVIDER_WINNT35,
			LOGON32_PROVIDER_WINNT40,
			LOGON32_PROVIDER_WINNT50
		}

		public struct SECURITY_ATTRIBUTES
		{
			public uint Length;
			public IntPtr SecurityDescriptor;
			public bool InheritHandle;
		}

		[Flags]
		public enum SECURITY_INFORMATION : uint
		{
			OWNER_SECURITY_INFORMATION = 0x00000001,
			GROUP_SECURITY_INFORMATION = 0x00000002,
			DACL_SECURITY_INFORMATION = 0x00000004,
			SACL_SECURITY_INFORMATION = 0x00000008,
			UNPROTECTED_SACL_SECURITY_INFORMATION = 0x10000000,
			UNPROTECTED_DACL_SECURITY_INFORMATION = 0x20000000,
			PROTECTED_SACL_SECURITY_INFORMATION = 0x40000000,
			PROTECTED_DACL_SECURITY_INFORMATION = 0x80000000
		}

		[StructLayoutAttribute(LayoutKind.Sequential)]
		public struct SECURITY_DESCRIPTOR
		{
			public byte revision;
			public byte size;
			public short control; // public SECURITY_DESCRIPTOR_CONTROL control;
			public IntPtr owner;
			public IntPtr group;
			public IntPtr sacl;
			public IntPtr dacl;
		}

		public struct STARTUPINFO
		{
			public uint cb;
			[MarshalAs(UnmanagedType.LPTStr)]
			public string Reserved;
			[MarshalAs(UnmanagedType.LPTStr)]
			public string Desktop;
			[MarshalAs(UnmanagedType.LPTStr)]
			public string Title;
			public uint X;
			public uint Y;
			public uint XSize;
			public uint YSize;
			public uint XCountChars;
			public uint YCountChars;
			public uint FillAttribute;
			public uint Flags;
			public ushort ShowWindow;
			public ushort Reserverd2;
			public byte bReserverd2;
			public IntPtr StdInput;
			public IntPtr StdOutput;
			public IntPtr StdError;
		}

		[StructLayout(LayoutKind.Sequential)]
		public struct PROCESS_INFORMATION
		{
			public IntPtr Process;
			public IntPtr Thread;
			public uint ProcessId;
			public uint ThreadId;
		}

		[DllImport("advapi32.dll", SetLastError = true)]
		public static extern bool InitializeSecurityDescriptor(IntPtr pSecurityDescriptor, uint dwRevision);

		public const uint SECURITY_DESCRIPTOR_REVISION = 1;

		[DllImport("advapi32.dll", SetLastError = true)]
		public static extern bool SetSecurityDescriptorDacl(ref SECURITY_DESCRIPTOR sd, bool daclPresent, IntPtr dacl, bool daclDefaulted);

		[DllImport("advapi32.dll", CharSet = CharSet.Auto, SetLastError = true)]
		public static extern bool DuplicateTokenEx(
			IntPtr hExistingToken,
			uint dwDesiredAccess,
			ref SECURITY_ATTRIBUTES lpTokenAttributes,
			SECURITY_IMPERSONATION_LEVEL ImpersonationLevel,
			TOKEN_TYPE TokenType,
			out IntPtr phNewToken);

		[DllImport("advapi32.dll", SetLastError = true)]
		public static extern bool LogonUser(
			string lpszUsername,
			string lpszDomain,
			string lpszPassword,
			int dwLogonType,
			int dwLogonProvider,
			out IntPtr phToken
			);

		[DllImport("advapi32.dll", SetLastError = true)]
		public static extern bool GetTokenInformation(
			IntPtr TokenHandle,
			TOKEN_INFORMATION_CLASS TokenInformationClass,
			IntPtr TokenInformation,
			int TokenInformationLength,
			out int ReturnLength
			);

		[DllImport("advapi32.dll", CharSet = CharSet.Unicode, SetLastError = true)]
		public static extern bool CreateProcessAsUser(
			IntPtr Token,
			[MarshalAs(UnmanagedType.LPTStr)] string ApplicationName,
			[MarshalAs(UnmanagedType.LPTStr)] string CommandLine,
			ref SECURITY_ATTRIBUTES ProcessAttributes,
			ref SECURITY_ATTRIBUTES ThreadAttributes,
			bool InheritHandles,
			uint CreationFlags,
			IntPtr Environment,
			[MarshalAs(UnmanagedType.LPTStr)] string CurrentDirectory,
			ref STARTUPINFO StartupInfo,
			out PROCESS_INFORMATION ProcessInformation);

		[DllImport("advapi32.dll", ExactSpelling = true, SetLastError = true)]
		public static extern bool AdjustTokenPrivileges(IntPtr htok, bool disall, ref TokPriv1Luid newst, int len, IntPtr prev, IntPtr relen);

		[DllImport("advapi32.dll", SetLastError = true)]
		public static extern bool LookupPrivilegeValue(string host, string name, ref long pluid);

		[StructLayout(LayoutKind.Sequential, Pack = 1)]
		public struct TokPriv1Luid
		{
			public int Count;
			public long Luid;
			public int Attr;
		}

		//static internal const int TOKEN_QUERY = 0x00000008;
		internal const int SE_PRIVILEGE_ENABLED = 0x00000002;
		//static internal const int TOKEN_ADJUST_PRIVILEGES = 0x00000020;

		public const int TOKEN_QUERY = 0x00000008;
		public const int TOKEN_DUPLICATE = 0x0002;
		public const int TOKEN_ASSIGN_PRIMARY = 0x0001;
	}
}
