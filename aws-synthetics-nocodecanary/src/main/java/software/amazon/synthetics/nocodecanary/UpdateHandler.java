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

import javax.swing.text.html.HTML;
import java.util.Map;

public class UpdateHandler extends BaseHandlerStd {
    public UpdateHandler() {
        super(Action.UPDATE);
    }

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest() {
        NoCodeCanary noCodeCanary = getNoCodeCanaryOrThrow();
        if (!callbackContext.isNoCodeCanaryUpdateStarted()) {
            if (noCodeCanary.noCodeCanaryState() == NoCodeCanaryState.CREATING) {
                log(Constants.NO_CODE_CANARY_STATE_CREATING_UPDATE_MSG);
                return ProgressEvent.<ResourceModel, CallbackContext>builder()
                        .message(Constants.NO_CODE_CANARY_STATE_CREATING_UPDATE_MSG)
                        .resourceModel(model)
                        .errorCode(HandlerErrorCode.ResourceConflict)
                        .status(OperationStatus.FAILED)
                        .build();
            } else if (noCodeCanary.noCodeCanaryState() == NoCodeCanaryState.DELETING) {
                log(Constants.NO_CODE_CANARY_STATE_DELETING_UPDATE_MSG);
                return ProgressEvent.<ResourceModel, CallbackContext>builder()
                        .message(Constants.NO_CODE_CANARY_STATE_DELETING_UPDATE_MSG)
                        .resourceModel(model)
                        .errorCode(HandlerErrorCode.ResourceConflict)
                        .status(OperationStatus.FAILED)
                        .build();
            } else if (noCodeCanary.noCodeCanaryState() == NoCodeCanaryState.UPDATING) {
                log(Constants.NO_CODE_CANARY_ALREADY_UPDATING_MSG);
                return ProgressEvent.<ResourceModel, CallbackContext>builder()
                        .message(Constants.NO_CODE_CANARY_ALREADY_UPDATING_MSG)
                        .resourceModel(model)
                        .errorCode(HandlerErrorCode.ResourceConflict)
                        .status(OperationStatus.FAILED)
                        .build();
            } else if (noCodeCanary.noCodeCanaryState() == NoCodeCanaryState.STARTING) {
                return waitingForNoCodeCanaryStateTransition(Constants.NO_CODE_CANARY_STATE_STARTING_UPDATE_MSG, Constants.MAX_RETRY_TIMES, NoCodeCanaryState.STARTING.toString());
            } else if (noCodeCanary.noCodeCanaryState() == NoCodeCanaryState.STOPPING) {
                return waitingForNoCodeCanaryStateTransition(Constants.NO_CODE_CANARY_STATE_STOPPING_UPDATE_MSG, Constants.MAX_RETRY_TIMES, NoCodeCanaryState.STOPPING.toString());
            } else {
                callbackContext.setInitialNoCodeCanaryState(noCodeCanary.noCodeCanaryState());
                callbackContext.setNoCodeCanaryUpdateStarted(true);
                return updateNoCodeCanary(noCodeCanary);
            }
        }

        if (noCodeCanary.noCodeCanaryState() == NoCodeCanaryState.UPDATING) {
            return waitingForNoCodeCanaryStateTransition(Constants.NO_CODE_CANARY_UPDATE_IN_PROGRESS_MSG, Constants.MAX_RETRY_TIMES, NoCodeCanaryState.UPDATING.toString());
        } else if (noCodeCanary.noCodeCanaryState() == NoCodeCanaryState.READY) { //|| canary.status().state() == CanaryState.STOPPED) {
            return handleNoCodeCanaryInStateReady(noCodeCanary);
        } else if (noCodeCanary.noCodeCanaryState() == NoCodeCanaryState.STARTING) {
            return handleNoCodeCanaryInStateStarting(noCodeCanary);
        } else if (noCodeCanary.noCodeCanaryState() == NoCodeCanaryState.RUNNING || noCodeCanary.noCodeCanaryState() == NoCodeCanaryState.STOPPED ) {
            return ProgressEvent.defaultSuccessHandler(Translator.constructModel(noCodeCanary, model));
        } else if (noCodeCanary.noCodeCanaryState() == NoCodeCanaryState.STOPPING) {
            return waitingForNoCodeCanaryStateTransition(Constants.STOPPING_NO_CODE_CANARY_MSG, Constants.MAX_RETRY_TIMES, NoCodeCanaryState.STOPPING.toString());
        }

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(model)
                .status(OperationStatus.FAILED)
                .build();
    }

    private ProgressEvent<ResourceModel, CallbackContext> handleNoCodeCanaryInStateReady(NoCodeCanary noCodeCanary) {
        log(String.format("Canary is in state %s.", noCodeCanary.noCodeCanaryStateAsString()));

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
        // If the customer calls StartCanary before we handle the canary in READY or
        // STOPPED state, then we can end up here even when StartCanaryAfterCreation is false.

        if (model.getStartNoCodeCanaryAfterCreation()) {
            return waitingForNoCodeCanaryStateTransition(
                    Constants.STARTING_NO_CODE_CANARY_MSG,
                    Constants.MAX_RETRY_TIMES,
                    NoCodeCanaryState.STARTING.toString());
        } else {
            log(Constants.NO_CODE_CANARY_IN_STATE_STARTING_ERROR_MSG);
            return ProgressEvent.defaultSuccessHandler(Translator.constructModel(noCodeCanary, model));
        }
    }

    private ProgressEvent<ResourceModel, CallbackContext> updateNoCodeCanary(NoCodeCanary noCodeCanary) {
        // TODO: Build no-code canary update request
        // TODO: Check what fields need to be updated by comparing no-code canary and model
        final UpdateNoCodeCanaryRequest updateNoCodeCanaryRequest = UpdateNoCodeCanaryRequest.builder()
                .noCodeCanaryIdentifier(noCodeCanary.name())
                .build();
        try {
            proxy.injectCredentialsAndInvokeV2(updateNoCodeCanaryRequest, syntheticsClient::updateNoCodeCanary);
            if (TagHelper.shouldUpdateTags(model, request)) {

                // get previous and new tags
                Map<String, String> newTags = TagHelper.getNewDesiredTags(model, request);
                Map<String, String> prevTags = TagHelper.getPreviouslyAttachedTags(request);

                // add nd remove tags
                addTags(TagHelper.generateTagsToAdd(prevTags, newTags), model.getArn());
                removeTags(TagHelper.generateTagsToRemove(prevTags, newTags), model.getArn());
            }
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
     * @param tagsToAdd
     * @param noCodeCanaryArn
     */
    private void addTags(Map<String, String> tagsToAdd, String noCodeCanaryArn) {
        try {
            log(Constants.TAG_RESOURCE_CALL);
            TagResourceRequest tagResourceRequest = TagResourceRequest.builder()
                    .resourceArn(noCodeCanaryArn)
                    .tags(tagsToAdd)
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
     * @param tagsToRemove
     * @param noCodeCanaryArn
     */
    private void removeTags(Map<String, String> tagsToRemove, String noCodeCanaryArn) {
        try {
            log(Constants.UNTAG_RESOURCE_CALL);
            UntagResourceRequest untagResourceRequest = UntagResourceRequest.builder()
                    .resourceArn(noCodeCanaryArn)
                    .tagKeys(tagsToRemove.keySet())
                    .build();
            proxy.injectCredentialsAndInvokeV2(untagResourceRequest, syntheticsClient::untagResource);
        } catch (BadRequestException | TooManyRequestsException | ConflictException | InternalFailureException e) {
            throw new CfnGeneralServiceException(e);
        } catch (NotFoundException e) {
            throw new CfnResourceConflictException(e);
        }
    }


}
