package jetbrains.buildServer.runAs.common;

public class Constants {
    // Plugin's ids
    public static final String BUILD_FEATURE_TYPE = "runAs-build-feature";
    public static final String RUN_AS_TOOL_NAME = "runAs";

    // Build's configuration parameters
    public static final String RUN_AS_READY_VAR = "run_as_ready";
    public static final String USER_VAR = "run_as_user";
    public static final String PASSWORD_VAR = "secure:run_as_password";
    public static final String CREDENTIALS_VAR = "run_as_credentials";
    public static final String ADDITIONAL_ARGS_VAR = "run_as_additional_args";
    public static final String WINDOWS_INTEGRITY_LEVEL_VAR = "run_as_windows_integrity_level";
    public static final String WINDOWS_LOGGING_LEVEL_VAR = "run_as_windows_logging_level";

    // Agent's configuration parameters
    public static final String CREDENTIALS_MODE_VAR = "run_as_credentials_mode";
    public static final String CREDENTIALS_DIRECTORY_VAR = "run_as_credentials_directory";

    // Server's configuration parameters
    public static final String RUN_AS_UI_ENABLED_VAR = "teamcity.runas.ui.enabled";
}