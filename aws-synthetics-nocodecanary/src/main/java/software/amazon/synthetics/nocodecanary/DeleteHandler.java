package software.amazon.synthetics.nocodecanary;

import software.amazon.awssdk.services.synthetics.model.Canary;
import software.amazon.awssdk.services.synthetics.model.CanaryState;
import software.amazon.awssdk.services.synthetics.model.ConflictException;
import software.amazon.awssdk.services.synthetics.model.ResourceNotFoundException;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.CfnResourceConflictException;
import software.amazon.cloudformation.proxy.*;
import software.amazon.synthetics.nocodecanary.utils.Constants;

public class DeleteHandler extends BaseHandlerStd {
    public DeleteHandler() {
        super(Action.DELETE);
    }

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest() {
        if (callbackContext.isNoCodeCanaryDeleteStarted()) {
            return confirmNoCodeCanaryDeleted();
        }

        Canary canary = getNoCodeCanaryOrThrow();

        if (canary.status().state() == CanaryState.CREATING) {
            log(Constants.NO_CODE_CANARY_IN_STATE_CREATING_DELETE_MSG);
            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .message(Constants.NO_CODE_CANARY_IN_STATE_CREATING_DELETE_MSG)
                    .errorCode(HandlerErrorCode.ResourceConflict)
                    .status(OperationStatus.FAILED)
                    .build();
        } else if (canary.status().state() == CanaryState.STARTING) {
            return waitingForNoCodeCanaryStateTransition(Constants.NO_CODE_CANARY_IN_STATE_STARTING_DELETE_MSG, Constants.MAX_RETRY_TIMES, "STARTING");
        } else if (canary.status().state() == CanaryState.UPDATING) {
            return waitingForNoCodeCanaryStateTransition(Constants.NO_CODE_CANARY_IN_STATE_UPDATING_DELETE_MSG, Constants.MAX_RETRY_TIMES, "UPDATING");
        } else if (canary.status().state() == CanaryState.STOPPING) {
            return waitingForNoCodeCanaryStateTransition(Constants.NO_CODE_CANARY_IN_STATE_STOPPING_DELETE_MSG, Constants.MAX_RETRY_TIMES, "STOPPING");
        } else if (canary.status().state() == CanaryState.RUNNING) {
            return handleNoCodeCanaryInStateRunning(canary);
        } else if (canary.status().state() == CanaryState.DELETING) {
            return confirmNoCodeCanaryDeleted();
        } else {
            return deleteNoCodeCanary(canary);
        }

    }

    private ProgressEvent<ResourceModel, CallbackContext> handleNoCodeCanaryInStateRunning(Canary canary) {
        try {
            // TODO: Make a call to stop no-code canary

        } catch (ConflictException e) {
            log(Constants.NO_CODE_CANARY_CONFLICT_STOPPING_MSG);
        }
        return waitingForNoCodeCanaryStateTransition(Constants.NO_CODE_CANARY_IN_STATE_RUNNING_DELETE_MSG, Constants.MAX_RETRY_TIMES, "RUNNING");
    }

    private ProgressEvent<ResourceModel, CallbackContext> deleteNoCodeCanary(Canary canary) {
        // The canary will be deleted once DeleteCanary returns.
        log(Constants.NO_CODE_CANARY_DELETING_MSG);
        try {
            // TODO: Make a call to delete no-code canary
        } catch (ResourceNotFoundException e) {
            // Handle race condition where an external process calls DeleteCanary before we do.
            return ProgressEvent.defaultSuccessHandler(null);
        } catch (ConflictException e) {
            // Handle race condition where an external process is mutating the canary while we
            // are trying to delete it.
            throw new CfnResourceConflictException(
                    ResourceModel.TYPE_NAME,
                    canary.name(),
                    Constants.NO_CODE_CANARY_STATE_CHANGED_MSG,
                    e);
        }

        callbackContext.setNoCodeCanaryDeleteStarted(true);
        log(Constants.NO_CODE_CANARY_DELETED_MSG);
        return confirmNoCodeCanaryDeleted();
    }

    private ProgressEvent<ResourceModel, CallbackContext> confirmNoCodeCanaryDeleted() {
        if (getNoCodeCanaryOrNull() != null) {
            return waitingForNoCodeCanaryStateTransition(Constants.NO_CODE_CANARY_CONFIRM_DELETE_MSG, Constants.MAX_RETRY_TIMES, "DELETING");
        } else {
            return ProgressEvent.defaultSuccessHandler(null);
        }
    }
}
