package jetbrains.buildServer.runAs.common;

public class Constants {
    // Plugin's ids
    public static final String BUILD_FEATURE_TYPE = "runAs-build-feature";
    public static final String RUN_AS_TOOL_NAME = "runAs";

    // Build's configuration parameters
    public static final String RUN_AS_ENABLED = "teamcity.agent.runAs.enabled";
    public static final String USER = "run_as_user";
    public static final String PASSWORD = "secure:run_as_password";
    public static final String CREDENTIALS = "run_as_credentials";
    public static final String ADDITIONAL_ARGS = "run_as_additional_args";
    public static final String WINDOWS_INTEGRITY_LEVEL = "run_as_windows_integrity_level";
    public static final String WINDOWS_LOGGING_LEVEL = "run_as_windows_logging_level";

    // Agent's configuration parameters
    public static final String RUN_AS_MODE = "run_as_mode";
    public static final String CREDENTIALS_DIRECTORY = "runAsDir";

    // Server's configuration parameters
    public static final String RUN_AS_UI_ENABLED = "teamcity.runas.ui.enabled";
}