

package jetbrains.buildServer.runAs.common;

public class Constants {
    // Plugin's ids
    public static final String BUILD_FEATURE_TYPE = "runAs-build-feature";
    public static final String RUN_AS_TOOL_NAME = "runAs";

    // Parameter names
    public static final String USER = "teamcity.runAs.username";
    public static final String PASSWORD = "secure:teamcity.runAs.password";
    public static final String CONFIG_PASSWORD = "teamcity.runAs.password";
    public static final String ADDITIONAL_ARGS = "teamcity.runAs.additionalCommandLine";
    public static final String RUN_AS_ENABLED = "teamcity.agent.runAs.enabled";
    public static final String CREDENTIALS_PROFILE_ID = "teamcity.runAs.profileId";
    public static final String WINDOWS_INTEGRITY_LEVEL = "teamcity.runAs.windowsIntegrityLlevel";
    public static final String LOGGING_LEVEL = "teamcity.runAs.loggingLevel";
    public static final String ALLOW_CUSTOM_CREDENTIALS = "teamcity.runAs.allowCustomCredentials";
    public static final String ALLOW_PROFILE_ID_FROM_SERVER = "teamcity.runAs.allowProfileIdFromServer";
    public static final String CREDENTIALS_DIRECTORY = "teamcity.runAs.configDir";
    public static final String RUN_AS_UI_ENABLED = "teamcity.runAs.ui.enabled";
    public static final String RUN_AS_UI_STEPS_ENABLED = "teamcity.runAs.ui.steps.enabled";
    public static final String RUN_AS_LOG_ENABLED = "teamcity.runAs.log.enabled";
    public static final String RUN_AS_ACL = "teamcity.runAs.acl";
    public static final String RUN_AS_ACL_DEFAULTS_ENABLED = "teamcity.runAs.acl.defaults.enabled";
}