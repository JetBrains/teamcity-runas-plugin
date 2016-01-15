# JetBrains runAs plugin for TeamCity #

This plugin provides an ability to run builds under the specified windows user account.

## How to Use It ##

All you need is to: <br/>
- Specify the configuration parameter "teamcity.agent.run_as_user" in the format: user_name or user_name@domain or domain\\user_name.
- Specify the configuration parameter "teamcity.agent.run_as_password".

The service's account of agent should have the foollowing privileges:
- Read/write access to "temp" directories for the agent
- Read/write access to the "checkout" directory for the agent
- ... will be specified

When a TeamCity agent is running as a windows service the following requirements must be met:
- The service's account of agent should have administrative privileges.
- The service's account of agent should have the ability to adjust memory quotas for a process and the ability to replace a process level token.
These privileges can be added via \"Control Panel\\Administrative Tools\\Local Security Policy\\User Rights Assignment\". See [page "Edit local security settings"](https://technet.microsoft.com/en-us/library/cc758168(v=ws.10).aspx). The changes don't take effect until the next login.

That's it! Once the build is run, the plugin runs all commands under the specified windows user account.

<!--
## Install ##

To install the plugin, put the [zip archive](https://cloud.mail.ru/public/3AwZ/DnD49rkpB) to 'plugins' direrctory under TeamCity data directory. Restart the server.
https://teamcity.jetbrains.com/httpAuth/app/rest/builds/buildType:TeamCityPluginsByJetBrains_RunAs_Build,pinned:true,status:SUCCESS,tags:deploy/artifacts/content/runAs.zip-->

## Implemention ##

This project contains 3 TeamCity's modules 'runAs-server', 'runAs-agent', 'runAs-common' and several windows tools. TeamCity's modules contain code for server and agent parts and a common part, available for both (agent and server). When implementing components for server and agent parts, do not forget to update spring context files under 'main/resources/META-INF'. See [TeamCity documentation](https://confluence.jetbrains.com/display/TCD9/Developing+Plugins+Using+Maven) for details on plugin development.

<!--
## Build ![](http://teamcity.jetbrains.com/app/rest/builds/buildType:TeamCityPluginsByJetBrains_RunAs_Build,tags:deploy,pinned:true/statusIcon.svg) ##
-->

To build this plugin you should have following:
- [Visual Studio 2015](https://www.visualstudio.com/) or [Microsoft Build Tools 2015](https://www.microsoft.com/en-us/download/details.aspx?id=48159)

Use 'build.cmd' to create your plugin. Resulting package 'runAs.zip' will be placed in root directory. 
<!--
The build is configured on the [JetBrains TeamCity build server](https://teamcity.jetbrains.com/viewLog.html?buildTypeId=TeamCityPluginsByJetBrains_RunAs_Build&buildId=lastPinned&buildBranch=%3Cdefault%3E).
-->
## License ##

JetBrains runAs plugin for TeamCity is under the [Apache License](https://github.com/JetBrains/teamcity-runas/blob/master/LICENSE).

## Contributors ##

- [Nikolay Pianikov](https://github.com/NikolayPianikov)
