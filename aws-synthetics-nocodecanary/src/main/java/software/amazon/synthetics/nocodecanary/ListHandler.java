package software.amazon.synthetics.nocodecanary;

import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.services.synthetics.model.SyntheticsException;
import software.amazon.awssdk.services.synthetics.model.ValidationException;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.proxy.*;

import java.util.ArrayList;
import java.util.List;

public class ListHandler extends BaseHandlerStd {

    public ListHandler() {
        super(Action.LIST);
    }

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest() {
        List<ResourceModel> models = listAllNoCodeCanaries();
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModels(models)
                .nextToken(request.getNextToken())
                .status(OperationStatus.SUCCESS)
                .build();
    }

    private List<ResourceModel> listAllNoCodeCanaries() {
        List<ResourceModel> models = new ArrayList<>();
        // construct a body of a request
        final AwsRequest listNoCodeCanariesRequest = Translator.translateToListRequest(request.getNextToken());

        try {
            // TODO: Get all no-code canaries summaries
            // TODO: Build models
        } catch (ValidationException ex) {
            log(ex);
            throw new CfnInvalidRequestException(ex);
        } catch (SyntheticsException ex) {
            log(ex);
            throw new CfnGeneralServiceException(ex);
        }
        // TODO: Set the next token
        return models;
    }
}
