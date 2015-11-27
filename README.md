# JetBrains runAs plugin for TeamCity #

This plugin provides the ability to run a build step under the specific windows user account.

## How to Use It ##
The main purpose of the plugin is to provide isolation based on access rights of windows account.

All you need is to: <br/>
- Specify the configuration parameter "teamcity.agent.run_as_user" in the format: user_name or user_name@domain or domain\\user_name.
- Specify the configuration parameter "teamcity.agent.run_as_password".
- The user account should have the foollowing list of rights:
  - Read/write access to "temp" directories on the each agent
  - Read/write access to the "checkout" directory on the each agent
  - ... will be specified

That's it! Once the build is run, the plugin runs all commands under the specific account.

## Install ##

To install the plugin, put the [zip archive](https://teamcity.jetbrains.com/httpAuth/app/rest/builds/buildType:TeamCityPluginsByJetBrains_RunAs_Build,pinned:true,status:SUCCESS,tags:deploy/artifacts/content/runAs.zip) to 'plugins' direrctory under TeamCity data directory. Restart the server.

## Implemention ##

This project contains 3 modules: 'runAs-server', 'runAs-agent' and 'runAs-common'. They contain code for server and agent parts and a common part, available for both (agent and server). When implementing components for server and agent parts, do not forget to update spring context files under 'main/resources/META-INF'. See [TeamCity documentation](https://confluence.jetbrains.com/display/TCD9/Developing+Plugins+Using+Maven) for details on plugin development.

## Build ![](http://teamcity.jetbrains.com/app/rest/builds/buildType:TeamCityPluginsByJetBrains_RunAs_Build,tags:deploy,pinned:true/statusIcon.svg) ##

To build this plugin you should have following:
- [Visual Studio 2015](https://www.visualstudio.com/) or [Microsoft Build Tools 2015](https://www.microsoft.com/en-us/download/details.aspx?id=48159)
- [Mictosoft .Net framework 2](https://www.microsoft.com/en-us/download/details.aspx?id=1639)
- [Mictosoft .Net framework 4 or above](https://msdn.microsoft.com/en-us/vstudio/dn250998.aspx)

Use 'build.cmd' to build your plugin. Resulting package 'runAs.zip' will be placed in root directory. The build is configured on the [JetBrains TeamCity build server](https://teamcity.jetbrains.com/viewLog.html?buildTypeId=TeamCityPluginsByJetBrains_RunAs_Build&buildId=lastPinned&buildBranch=%3Cdefault%3E).

## License ##

JetBrains runAs plugin for TeamCity is under the [Apache License](https://github.com/JetBrains/teamcity-runas/blob/master/LICENSE).

## Contributors ##

- [Nikolay Pianikov](https://github.com/NikolayPianikov)
