package software.amazon.synthetics.nocodecanary;

import jdk.internal.joptsimple.internal.Strings;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.services.synthetics.model.*;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnResourceConflictException;
import software.amazon.cloudformation.proxy.*;
import software.amazon.synthetics.nocodecanary.utils.Constants;

import java.util.Map;

public class UpdateHandler extends BaseHandlerStd {
    public UpdateHandler() {
        super(Action.UPDATE);
    }

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest() {
        Canary canary = getNoCodeCanaryOrThrow();
        if (!callbackContext.isNoCodeCanaryUpdateStarted()) {
            if (canary.status().state() == CanaryState.CREATING) {
                log(Constants.NO_CODE_CANARY_STATE_CREATING_UPDATE_MSG);
                return ProgressEvent.<ResourceModel, CallbackContext>builder()
                        .message(Constants.NO_CODE_CANARY_STATE_CREATING_UPDATE_MSG)
                        .resourceModel(model)
                        .errorCode(HandlerErrorCode.ResourceConflict)
                        .status(OperationStatus.FAILED)
                        .build();
            } else if (canary.status().state() == CanaryState.DELETING) {
                log(Constants.NO_CODE_CANARY_STATE_DELETING_UPDATE_MSG);
                return ProgressEvent.<ResourceModel, CallbackContext>builder()
                        .message(Constants.NO_CODE_CANARY_STATE_DELETING_UPDATE_MSG)
                        .resourceModel(model)
                        .errorCode(HandlerErrorCode.ResourceConflict)
                        .status(OperationStatus.FAILED)
                        .build();
            } else if (canary.status().state() == CanaryState.UPDATING) {
                log(Constants.NO_CODE_CANARY_ALREADY_UPDATING_MSG);
                return ProgressEvent.<ResourceModel, CallbackContext>builder()
                        .message(Constants.NO_CODE_CANARY_ALREADY_UPDATING_MSG)
                        .resourceModel(model)
                        .errorCode(HandlerErrorCode.ResourceConflict)
                        .status(OperationStatus.FAILED)
                        .build();
            } else if (canary.status().state() == CanaryState.STARTING) {
                return waitingForNoCodeCanaryStateTransition(Constants.NO_CODE_CANARY_STATE_STARTING_UPDATE_MSG, Constants.MAX_RETRY_TIMES, CanaryState.STARTING.toString());
            } else if (canary.status().state() == CanaryState.STOPPING) {
                return waitingForNoCodeCanaryStateTransition(Constants.NO_CODE_CANARY_STATE_STOPPING_UPDATE_MSG, Constants.MAX_RETRY_TIMES, CanaryState.STOPPING.toString());
            } else {
                callbackContext.setInitialNoCodeCanaryState(canary.status().state());
                callbackContext.setNoCodeCanaryUpdateStarted(true);
                return updateNoCodeCanary(canary);
            }
        }

        if (canary.status().state() == CanaryState.UPDATING) {
            return waitingForNoCodeCanaryStateTransition(Constants.NO_CODE_CANARY_UPDATE_IN_PROGRESS_MSG, Constants.MAX_RETRY_TIMES, CanaryState.UPDATING.toString());
        } else if (canary.status().state() == CanaryState.READY) { //|| canary.status().state() == CanaryState.STOPPED) {
            return handleNoCodeCanaryInStateReady(canary);
            // return handleNoCodeCanaryInStateReadyOrStopped(canary);
        } else if (canary.status().state() == CanaryState.STARTING) {
            return handleNoCodeCanaryInStateStarting(canary);
        } else if (canary.status().state() == CanaryState.RUNNING || canary.status().state() == CanaryState.STOPPED ) {
            return ProgressEvent.defaultSuccessHandler(Translator.constructModel(canary, model));
            //return handleNoCodeCanaryInStateRunning(canary);
        } else if (canary.status().state() == CanaryState.STOPPING) {
            return waitingForNoCodeCanaryStateTransition(Constants.STOPPING_NO_CODE_CANARY_MSG, Constants.MAX_RETRY_TIMES, CanaryState.STOPPING.toString());
        }

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(model)
                .status(OperationStatus.FAILED)
                .build();
    }

    private ProgressEvent<ResourceModel, CallbackContext> handleNoCodeCanaryInStateReady(Canary canary) {
        log(String.format("Canary is in state %s.", canary.status().stateAsString()));

        if (model.getStartNoCodeCanaryAfterCreation()) {


            // TODO: Call start no-code canary

            return waitingForNoCodeCanaryStateTransition(Constants.STARTING_NO_CODE_CANARY_MSG, Constants.MAX_RETRY_TIMES, CanaryState.READY.toString());
        } else {
            return ProgressEvent.defaultSuccessHandler(Translator.constructModel(canary, model));
        }
    }

    private ProgressEvent<ResourceModel, CallbackContext> handleNoCodeCanaryInStateReadyOrStopped(Canary canary) {
        log(String.format("Canary is in state %s.", canary.status().stateAsString()));

        // After an update, If canary is in READY or STOPPED state with stateReason message, it indicates that update has failed.
        // 1. If the canary was initially in READY or STOPPED state and there was an error during provisioning,
        // then it will be set to READY or STOPPED state again and the message
        // will be in the StateReason field.
        // 2. A canary initially in Running state can also be set to state STOPPED if it was a run once canary and update failed but meanwhile canary execution has come to an end.
        // TODO: Check this logic
        if (!Strings.isNullOrEmpty(canary.status().stateReason())) {
            log(String.format("Update failed: %s", canary.status().stateReason()));
            return ProgressEvent.failed(
                    model,
                    callbackContext,
                    HandlerErrorCode.GeneralServiceException,
                    canary.status().stateReason());
        }

        if (model.getStartNoCodeCanaryAfterCreation()) {


            // TODO: Call start no-code canary

            return waitingForNoCodeCanaryStateTransition(Constants.STARTING_NO_CODE_CANARY_MSG, Constants.MAX_RETRY_TIMES, CanaryState.READY.toString());
        } else {
            return ProgressEvent.defaultSuccessHandler(Translator.constructModel(canary, model));
        }
    }

    private ProgressEvent<ResourceModel, CallbackContext> handleNoCodeCanaryInStateStarting(Canary canary) {
        // If the customer calls StartCanary before we handle the canary in READY or
        // STOPPED state, then we can end up here even when StartCanaryAfterCreation is false.

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

    private ProgressEvent<ResourceModel, CallbackContext> handleNoCodeCanaryInStateRunning(Canary canary) {
        log(Constants.NO_CODE_CANARY_IN_STATE_RUNNING_MSG);

        if (callbackContext.getInitialNoCodeCanaryState() == CanaryState.RUNNING) {
            // If the canary was initially in state RUNNING and there was an error
            // during provisioning, then it will be set to RUNNING again and the message
            // will be in the StateReason field.
            // TODO: Come back to this code
            if (!Strings.isNullOrEmpty(canary.status().stateReason())) {
                log(String.format("Update failed: %s", canary.status().stateReason()));
                return ProgressEvent.failed(
                        model,
                        callbackContext,
                        HandlerErrorCode.GeneralServiceException,
                        canary.status().stateReason());
            }

            // If the canary was initially in state RUNNING and StartCanaryAfterCreation is
            // false, we should stop the canary.
            if (!model.getStartNoCodeCanaryAfterCreation()) {
                // There is a race condition here. We will get an exception if someone calls
                // DeleteCanary, StopCanary, or UpdateCanary before we call StopCanary.
                // TODO: Send request to stop no-code canary

                return waitingForNoCodeCanaryStateTransition(Constants.STOPPING_NO_CODE_CANARY_MSG, Constants.MAX_RETRY_TIMES, CanaryState.RUNNING.toString());
            }
        }

        return ProgressEvent.defaultSuccessHandler(Translator.constructModel(canary, model));
    }

    private ProgressEvent<ResourceModel, CallbackContext> updateNoCodeCanary(Canary canary) {
        // TODO: Build no-code canary update request
        // TODO: Check what fields need to be updated by comparing no-code canary and model

        try {
            // TODO: Send update canary request
            //if (model.getTags() != null) {
            Map<String, Map<String, String>> tagResourceMap = TagHelper.updateTags(model, canary.tags());
            String noCodeCanaryArn = ""; // TODO: get the arn
            if (!tagResourceMap.get(Constants.ADD_TAGS).isEmpty()) {
                addTags(tagResourceMap, noCodeCanaryArn);
            }

            if (!tagResourceMap.get(Constants.REMOVE_TAGS).isEmpty()) {
                removeTags(tagResourceMap, noCodeCanaryArn);
            }
            //}
        }
        catch (final ValidationException e) {
            throw new CfnInvalidRequestException(e);
        } catch (final Exception e) {
            throw new CfnGeneralServiceException(e);
        }
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .callbackContext(callbackContext)
                .resourceModel(model)
                .status(OperationStatus.IN_PROGRESS)
                .callbackDelaySeconds(Constants.DEFAULT_CALLBACK_DELAY_SECONDS)
                .build();
    }

    /**
     * Wrapper around tagResource call for Synthetics api and handle response/ error
     * @param tagResourceMap
     * @param noCodeCanaryArn
     */
    private void addTags(Map<String, Map<String, String>> tagResourceMap, String noCodeCanaryArn) {
        try {
            log(Constants.TAG_RESOURCE_CALL);
            TagResourceRequest tagResourceRequest = TagResourceRequest.builder()
                    .resourceArn(noCodeCanaryArn)
                    .tags(tagResourceMap.get(Constants.ADD_TAGS))
                    .build();
            proxy.injectCredentialsAndInvokeV2(tagResourceRequest, syntheticsClient::tagResource);
        } catch (BadRequestException | TooManyRequestsException | ConflictException | InternalFailureException e) {
            throw new CfnGeneralServiceException(e);
        } catch (NotFoundException e) {
            throw new CfnResourceConflictException(e);
        }
    }

    /**
     * Wrapper around untagResource call for Synthetics api and handle response/ error
     * @param tagResourceMap
     * @param noCodeCanaryArn
     */
    private void removeTags(Map<String, Map<String, String>> tagResourceMap, String noCodeCanaryArn) {
        try {
            log(Constants.UNTAG_RESOURCE_CALL);
            UntagResourceRequest untagResourceRequest = UntagResourceRequest.builder()
                    .resourceArn(noCodeCanaryArn)
                    .tagKeys(tagResourceMap.get(Constants.REMOVE_TAGS).keySet())
                    .build();
            proxy.injectCredentialsAndInvokeV2(untagResourceRequest, syntheticsClient::untagResource);
        } catch (BadRequestException | TooManyRequestsException | ConflictException | InternalFailureException e) {
            throw new CfnGeneralServiceException(e);
        } catch (NotFoundException e) {
            throw new CfnResourceConflictException(e);
        }
    }


}
