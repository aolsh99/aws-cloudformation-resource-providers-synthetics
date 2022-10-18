package software.amazon.synthetics.nocodecanary;

import software.amazon.awssdk.services.synthetics.SyntheticsClient;
import software.amazon.awssdk.services.synthetics.model.Canary;
import software.amazon.awssdk.services.synthetics.model.NoCodeCanary;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.proxy.*;
import software.amazon.synthetics.nocodecanary.utils.Constants;

// Placeholder for the functionality that could be shared across Create/Read/Update/Delete/List Handlers

public abstract class BaseHandlerStd extends BaseHandler<CallbackContext> {

  private final Action action;
  private NoCodeCanaryLogger logger;

  protected AmazonWebServicesClientProxy proxy;
  protected ResourceHandlerRequest<ResourceModel> request;
  protected CallbackContext callbackContext;
  protected ResourceModel model;
  protected SyntheticsClient syntheticsClient;

  public BaseHandlerStd(Action action) {
    this.action = action;
  }

  @Override
  public final ProgressEvent<ResourceModel, CallbackContext> handleRequest(
    final AmazonWebServicesClientProxy proxy,
    final ResourceHandlerRequest<ResourceModel> request,
    final CallbackContext callbackContext,
    final Logger logger) {
    this.proxy = proxy;
    this.request = request;
    this.callbackContext = callbackContext != null ? callbackContext : CallbackContext.builder().build();
    this.model = request.getDesiredResourceState();
    this.logger = new NoCodeCanaryLogger(logger, action, request.getAwsAccountId(), callbackContext, model);
    this.syntheticsClient = ClientBuilder.getClient();
    log(Constants.INVOKING_HANDLER_MSG);
    ProgressEvent<ResourceModel, CallbackContext> response;
    try {
      response = handleRequest();
    } catch (Exception e) {
      log(e);
      throw e;
    }
    log(Constants.INVOKING_HANDLER_FINISHED_MSG);
    return response;
  }

  protected NoCodeCanary getNoCodeCanaryOrThrow() {
    return NoCodeCanaryHelper.getNoCodeCanaryOrThrow(proxy, syntheticsClient, model);
  }
  protected NoCodeCanary getNoCodeCanaryOrNull() {
    return NoCodeCanaryHelper.getNoCodeCanaryOrNull(proxy, syntheticsClient, model.getName());
  }

  /**
   * Overridden in every handler based on the action
   * @return
   */
  protected abstract ProgressEvent<ResourceModel, CallbackContext> handleRequest();

  protected void log(String message) {
    logger.log(message);
  }

  protected void log(Exception exception) {
    logger.log(exception);
  }

  protected void throwIfRetryLimitExceeded(int retryCount, String retryKey) {
    callbackContext.throwIfRetryLimitExceeded(retryCount, retryKey, model);
  }
  protected ProgressEvent<ResourceModel, CallbackContext> waitingForNoCodeCanaryStateTransition(String message, int retryCount, String retryKey) {
    throwIfRetryLimitExceeded(retryCount, retryKey);
    log(message);
    return ProgressEvent.<ResourceModel, CallbackContext>builder()
            .resourceModel(model)
            .callbackContext(callbackContext)
            .message(message)
            .status(OperationStatus.IN_PROGRESS)
            .callbackDelaySeconds(5)
            .build();
  }

}
