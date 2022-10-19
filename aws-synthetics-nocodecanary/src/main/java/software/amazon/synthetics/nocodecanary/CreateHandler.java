package software.amazon.synthetics.nocodecanary;


import software.amazon.awssdk.services.synthetics.model.*;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
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

        NoCodeCanary noCodeCanary = getNoCodeCanaryOrThrow();

        if (noCodeCanary.noCodeCanaryState() == NoCodeCanaryState.CREATING) {
            return waitingForNoCodeCanaryStateTransition(
                    Constants.CREATING_NO_CODE_CANARY_MSG,
                    Constants.MAX_RETRY_TIMES,
                    NoCodeCanaryState.CREATING.toString());
        } else if (noCodeCanary.noCodeCanaryState() == NoCodeCanaryState.READY) {
            return handleNoCodeCanaryInStateReady(noCodeCanary);
        } else if (noCodeCanary.noCodeCanaryState() == NoCodeCanaryState.STARTING) {
            return handleNoCodeCanaryInStateStarting(noCodeCanary);
        } else {
            return ProgressEvent.defaultSuccessHandler(Translator.constructModel(noCodeCanary, model));
        }


    }

    private ProgressEvent<ResourceModel, CallbackContext> handleNoCodeCanaryInStateReady(NoCodeCanary noCodeCanary) {
        log(Constants.NO_CODE_CANARY_IN_STATE_READY_MSG);
        if (model.getStartNoCodeCanaryAfterCreation()) {
            proxy.injectCredentialsAndInvokeV2(
                    StartNoCodeCanaryRequest.builder()
                            .noCodeCanaryIdentifier(noCodeCanary.name())
                            .build(),
                    syntheticsClient::startNoCodeCanary);
            return waitingForNoCodeCanaryStateTransition(Constants.STARTING_NO_CODE_CANARY_MSG, Constants.MAX_RETRY_TIMES, NoCodeCanaryState.READY.toString());
        } else {
            return ProgressEvent.defaultSuccessHandler(Translator.constructModel(noCodeCanary, model));
        }
    }

    private ProgressEvent<ResourceModel, CallbackContext> handleNoCodeCanaryInStateStarting(NoCodeCanary noCodeCanary) {
        // If the customer calls StartCanary before we handle the canary in READY state,
        // then we can end up here even when StartCanaryAfterCreation is false.

        if (model.getStartNoCodeCanaryAfterCreation()) {
            return waitingForNoCodeCanaryStateTransition(
                    Constants.STARTING_NO_CODE_CANARY_MSG,
                    Constants.MAX_RETRY_TIMES,
                    CanaryState.STARTING.toString());
        } else {
            log(Constants.NO_CODE_CANARY_IN_STATE_STARTING_ERROR_MSG);
            return ProgressEvent.defaultSuccessHandler(Translator.constructModel(noCodeCanary, model));
        }
    }

    private ProgressEvent<ResourceModel, CallbackContext> createNoCodeCanary() {
        // TODO: Add other fields
        final CreateNoCodeCanaryRequest createNoCodeCanaryRequest = CreateNoCodeCanaryRequest.builder()
                .name(model.getName())
                .build();
        try {
            proxy.injectCredentialsAndInvokeV2(createNoCodeCanaryRequest, syntheticsClient::createNoCodeCanary);
        } catch (final ValidationException e) {
            if ( e.getMessage().contains("Canary name already exists") ) {
                throw new CfnAlreadyExistsException(ResourceModel.TYPE_NAME, e.getMessage(), e);
            } else {
                throw new CfnInvalidRequestException(e.getMessage());
            }
        } catch (final Exception e) {
            throw new CfnGeneralServiceException(e.getMessage());
        }
        callbackContext.setNoCodeCanaryCreateStarted(true);
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .callbackContext(callbackContext)
                .resourceModel(model)
                .status(OperationStatus.IN_PROGRESS)
                .callbackDelaySeconds(Constants.DEFAULT_CALLBACK_DELAY_SECONDS)
                .build();
    }
}
