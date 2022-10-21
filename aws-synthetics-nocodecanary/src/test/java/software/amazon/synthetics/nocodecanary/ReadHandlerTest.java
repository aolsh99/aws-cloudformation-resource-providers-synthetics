package software.amazon.synthetics.nocodecanary;

import software.amazon.awssdk.services.synthetics.model.GetNoCodeCanaryResponse;
import software.amazon.awssdk.services.synthetics.model.NotFoundException;
import software.amazon.awssdk.services.synthetics.model.ResourceNotFoundException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest extends AbstractTestBase {


    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final ReadHandler handler = new ReadHandler();

        final ResourceModel model = ResourceModel.builder().name(NO_CODE_CANARY_NAME).build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        GetNoCodeCanaryResponse getNoCodeCanaryResponse = GetNoCodeCanaryResponse.builder()
                .noCodeCanary(noCodeCanaryResponseObjectForTesting(NO_CODE_CANARY_NAME))
                .build();
        doReturn(getNoCodeCanaryResponse).when(proxy).injectCredentialsAndInvokeV2(any(), any());

        ResourceModel outputModel = buildModelForRead(NO_CODE_CANARY_NAME);

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, CallbackContext.builder().build(), logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(outputModel);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_NotFoundException() {
        configureGetNoCodeCanaryResponse(ResourceNotFoundException.builder().build());
        final UpdateHandler handler = new UpdateHandler();

        final ResourceModel model = ResourceModel.builder().name(NO_CODE_CANARY_NAME).build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThatThrownBy(() -> handler.handleRequest(proxy, request, CallbackContext.builder().build(), logger))
                .isInstanceOf(CfnNotFoundException.class);
    }
}
