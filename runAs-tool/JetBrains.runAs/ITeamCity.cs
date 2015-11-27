namespace JetBrains.runAs
{
	internal interface ITeamCity
	{
		void SendBuildProblem([NotNull] string description);
	}
}
