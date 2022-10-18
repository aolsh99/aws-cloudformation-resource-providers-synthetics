package software.amazon.synthetics.nocodecanary;

import software.amazon.awssdk.services.synthetics.model.Canary;
import software.amazon.awssdk.services.synthetics.model.NoCodeCanary;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.proxy.*;

public class ReadHandler extends BaseHandlerStd {

    public ReadHandler() {
        super(Action.READ);
    }

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest() {
        NoCodeCanary noCodeCanary = getNoCodeCanaryOrThrow();
        ResourceModel outputModel = Translator.constructModel(noCodeCanary, model);
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(outputModel)
                .status(OperationStatus.SUCCESS)
                .build();
    }
}
