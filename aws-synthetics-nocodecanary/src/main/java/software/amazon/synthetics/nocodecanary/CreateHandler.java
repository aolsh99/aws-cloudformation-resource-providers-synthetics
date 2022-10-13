package software.amazon.synthetics.nocodecanary;


import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.services.synthetics.model.Canary;
import software.amazon.awssdk.services.synthetics.model.CanaryState;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.proxy.*;
import software.amazon.cloudformation.Action;
import software.amazon.synthetics.nocodecanary.utils.Constants;

public class CreateHandler extends BaseHandlerStd {
    public CreateHandler() {
        super(Action.CREATE);
    }

    @Override
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest() {

        if (!callbackContext.isNoCodeCanaryCreateStarted()) {
            log(Constants.CREATING_NO_CODE_CANARY_MSG);
            callbackContext.setNoCodeCanaryCreateStarted(true);
            return createNoCodeCanary();
        }

        Canary canary = getNoCodeCanaryOrThrow();

        if (canary.status().state() == CanaryState.CREATING) {
            return waitingForNoCodeCanaryStateTransition(
                    Constants.CREATING_NO_CODE_CANARY_MSG,
                    Constants.MAX_RETRY_TIMES,
                    CanaryState.CREATING.toString());
        } else if (canary.status().state() == CanaryState.ERROR) {
            log(Constants.NO_CODE_CANARY_ERROR_STATE_MSG);
            return ProgressEvent.failed(
                    model,
                    callbackContext,
                    HandlerErrorCode.GeneralServiceException,
                    Constants.NO_CODE_CANARY_ERROR_STATE_MSG);
        } else if (canary.status().state() == CanaryState.READY) {
            return handleNoCodeCanaryInStateReady(canary);
        } else if (canary.status().state() == CanaryState.STARTING) {
            return handleNoCodeCanaryInStateStarting(canary);
        } else {
            return ProgressEvent.defaultSuccessHandler(Translator.constructModel(canary, model));
        }


    }

    private ProgressEvent<ResourceModel, CallbackContext> handleNoCodeCanaryInStateReady(Canary canary) {
        log(Constants.NO_CODE_CANARY_IN_STATE_READY_MSG);
        if (model.getStartNoCodeCanaryAfterCreation()) {
            // TODO: Send start no-code canary request
            return waitingForNoCodeCanaryStateTransition(Constants.STARTING_NO_CODE_CANARY_MSG, Constants.MAX_RETRY_TIMES, "READY");
        } else {
            return ProgressEvent.defaultSuccessHandler(Translator.constructModel(canary, model));
        }
    }

    private ProgressEvent<ResourceModel, CallbackContext> handleNoCodeCanaryInStateStarting(Canary canary) {
        // If the customer calls StartCanary before we handle the canary in READY state,
        // then we can end up here even when StartCanaryAfterCreation is false.

        if (model.getStartNoCodeCanaryAfterCreation()) {
            return waitingForNoCodeCanaryStateTransition(
                    Constants.STARTING_NO_CODE_CANARY_MSG,
                    Constants.MAX_RETRY_TIMES,
                    CanaryState.STARTING.toString());
        } else {
            log(Constants.NO_CODE_CANARY_IN_STATE_STARTING_ERROR_MSG);
            return ProgressEvent.defaultSuccessHandler(Translator.constructModel(canary, model));
        }
    }

    private ProgressEvent<ResourceModel, CallbackContext> createNoCodeCanary() {
        // TODO: build request from model
        // TODO: send api call
        callbackContext.setNoCodeCanaryCreateStarted(true);
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .callbackContext(callbackContext)
                .resourceModel(model)
                .status(OperationStatus.IN_PROGRESS)
                .callbackDelaySeconds(Constants.DEFAULT_CALLBACK_DELAY_SECONDS)
                .build();
    }
}
