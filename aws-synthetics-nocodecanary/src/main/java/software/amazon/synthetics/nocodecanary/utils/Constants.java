package software.amazon.synthetics.nocodecanary.utils;

public class Constants {

    public static final int DEFAULT_CALLBACK_DELAY_SECONDS = 10;
    public static final int MAX_RETRY_TIMES = 120;
    public static final String INVOKING_HANDLER_MSG = "Invoking No-code Canary handler";
    public static final String INVOKING_HANDLER_FINISHED_MSG = "Handler No-code Canary executed";
    public static final String CREATING_NO_CODE_CANARY_MSG = "Creating no-code canary";
    public static final String STARTING_NO_CODE_CANARY_MSG = "Starting no-code canary";
    public static final String STOPPING_NO_CODE_CANARY_MSG = "Stopping no-code canary";
    public static final String NO_CODE_CANARY_ERROR_STATE_MSG = "No-code canary is in state ERROR";
    public static final String NO_CODE_CANARY_IN_STATE_READY_MSG = "No-code canary is in state READY";
    public static final String NO_CODE_CANARY_IN_STATE_STARTING_ERROR_MSG = "No-code canary is in STARTING state even though StartNoCodeCanaryAfterCreation was false";
    public static final String NO_CODE_CANARY_IN_STATE_CREATING_DELETE_MSG = "No-code canary is in state CREATING and cannot be deleted";
    public static final String NO_CODE_CANARY_IN_STATE_STARTING_DELETE_MSG = "No-code canary is in state STARTING. It must finish starting before it can be stopped and deleted";
    public static final String NO_CODE_CANARY_IN_STATE_UPDATING_DELETE_MSG = "No-code canary is in state UPDATING. It must finish updating before it can be deleted";
    public static final String NO_CODE_CANARY_IN_STATE_STOPPING_DELETE_MSG = "No-code canary is in state STOPPING. It must finish stopping before it can be deleted";
    public static final String NO_CODE_CANARY_IN_STATE_RUNNING_DELETE_MSG = "No-code canary is in state RUNNING. It must be stopped before it can be deleted.";
    public static final String NO_CODE_CANARY_DELETING_MSG = "Deleting no-code canary";
    public static final String NO_CODE_CANARY_DELETED_MSG = "Deleted no-code canary";
    public static final String NO_CODE_CANARY_STATE_CHANGED_MSG = "The no-code canary state changed unexpectedly";
    public static final String NO_CODE_CANARY_CONFIRM_DELETE_MSG = "Confirming that no-code canary was deleted";
    public static final String NO_CODE_CANARY_CONFLICT_STOPPING_MSG = "Caught ConflictException when trying to stop canary";
    public static final String NO_CODE_CANARY_ALREADY_UPDATING_MSG = "No-code canary is already updating";
    public static final String NO_CODE_CANARY_STATE_CREATING_UPDATE_MSG = "No-code canary is in state CREATING and cannot be updated";

    public static final String NO_CODE_CANARY_STATE_DELETING_UPDATE_MSG = "No-code canary is in state DELETING and cannot be updated";
    public static final String NO_CODE_CANARY_STATE_STARTING_UPDATE_MSG = "No-code canary is in state STARTING. It must finish starting before it can be updated.";
    public static final String NO_CODE_CANARY_STATE_STOPPING_UPDATE_MSG = "No-code canary is in state STOPPING. It must finish stopping before it can be updated.";
    public static final String NO_CODE_CANARY_UPDATE_IN_PROGRESS_MSG = "Update in progress";
    public static final String NO_CODE_CANARY_IN_STATE_RUNNING_MSG = "No-code canary is in state RUNNING";
    public static final String ADD_TAGS = "AddTags";
    public static final String REMOVE_TAGS = "RemoveTags";
    public static final String TAG_RESOURCE_CALL = "Making tag no-code canary resource call";
    public static final String UNTAG_RESOURCE_CALL = "Making untag group resource call";
}
