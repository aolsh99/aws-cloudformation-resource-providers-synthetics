package software.amazon.synthetics.nocodecanary;

import software.amazon.awssdk.services.synthetics.model.*;
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

        NoCodeCanary noCodeCanary = getNoCodeCanaryOrThrow();

        if (noCodeCanary.noCodeCanaryState() == NoCodeCanaryState.CREATING) {
            log(Constants.NO_CODE_CANARY_IN_STATE_CREATING_DELETE_MSG);
            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .message(Constants.NO_CODE_CANARY_IN_STATE_CREATING_DELETE_MSG)
                    .errorCode(HandlerErrorCode.ResourceConflict)
                    .status(OperationStatus.FAILED)
                    .build();
        } else if (noCodeCanary.noCodeCanaryState() == NoCodeCanaryState.STARTING) {
            return waitingForNoCodeCanaryStateTransition(Constants.NO_CODE_CANARY_IN_STATE_STARTING_DELETE_MSG, Constants.MAX_RETRY_TIMES, "STARTING");
        } else if (noCodeCanary.noCodeCanaryState() == NoCodeCanaryState.UPDATING) {
            return waitingForNoCodeCanaryStateTransition(Constants.NO_CODE_CANARY_IN_STATE_UPDATING_DELETE_MSG, Constants.MAX_RETRY_TIMES, "UPDATING");
        } else if (noCodeCanary.noCodeCanaryState() == NoCodeCanaryState.STOPPING) {
            return waitingForNoCodeCanaryStateTransition(Constants.NO_CODE_CANARY_IN_STATE_STOPPING_DELETE_MSG, Constants.MAX_RETRY_TIMES, "STOPPING");
        } else if (noCodeCanary.noCodeCanaryState() == NoCodeCanaryState.RUNNING) {
            return handleNoCodeCanaryInStateRunning(noCodeCanary);
        } else if (noCodeCanary.noCodeCanaryState() == NoCodeCanaryState.DELETING) {
            return confirmNoCodeCanaryDeleted();
        } else {
            return deleteNoCodeCanary(noCodeCanary);
        }

    }

    private ProgressEvent<ResourceModel, CallbackContext> handleNoCodeCanaryInStateRunning(NoCodeCanary noCodeCanary) {
        try {
            proxy.injectCredentialsAndInvokeV2(
                    StopNoCodeCanaryRequest.builder()
                            .noCodeCanaryIdentifier(noCodeCanary.name())
                            .build(),
                    syntheticsClient::stopNoCodeCanary);
        } catch (ConflictException e) {
            log(Constants.NO_CODE_CANARY_CONFLICT_STOPPING_MSG);
        }
        return waitingForNoCodeCanaryStateTransition(Constants.NO_CODE_CANARY_IN_STATE_RUNNING_DELETE_MSG, Constants.MAX_RETRY_TIMES, "RUNNING");
    }

    private ProgressEvent<ResourceModel, CallbackContext> deleteNoCodeCanary(NoCodeCanary noCodeCanary) {
        // The canary will be deleted once DeleteCanary returns.
        log(Constants.NO_CODE_CANARY_DELETING_MSG);
        try {
            proxy.injectCredentialsAndInvokeV2(
                    DeleteNoCodeCanaryRequest.builder()
                            .noCodeCanaryIdentifier(noCodeCanary.name())
                            .build(),
                    syntheticsClient::deleteNoCodeCanary);
        } catch (ResourceNotFoundException e) {
            // Handle race condition where an external process calls DeleteCanary before we do.
            return ProgressEvent.defaultSuccessHandler(null);
        } catch (ConflictException e) {
            // Handle race condition where an external process is mutating the canary while we
            // are trying to delete it.
            throw new CfnResourceConflictException(
                    ResourceModel.TYPE_NAME,
                    noCodeCanary.name(),
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
