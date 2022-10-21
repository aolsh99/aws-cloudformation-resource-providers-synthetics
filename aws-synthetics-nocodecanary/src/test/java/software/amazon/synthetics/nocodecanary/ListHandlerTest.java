package software.amazon.synthetics.nocodecanary;

import software.amazon.awssdk.services.synthetics.model.ListNoCodeCanariesResponse;
import software.amazon.awssdk.services.synthetics.model.NoCodeCanary;
import software.amazon.awssdk.services.synthetics.model.NoCodeCanarySummary;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class ListHandlerTest extends AbstractTestBase{

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final ListHandler handler = new ListHandler();

        final ResourceModel model = ResourceModel.builder().name("listNoCodeCanary").build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final List<NoCodeCanarySummary> noCodeCanarySummaries = new ArrayList<>();
        noCodeCanarySummaries.add(noCodeCanarySummaryResponseObjectForTesting("no-code-canary-1"));
        noCodeCanarySummaries.add(noCodeCanarySummaryResponseObjectForTesting("no-code-canary-2"));

        final ListNoCodeCanariesResponse listNoCodeCanariesResponse = ListNoCodeCanariesResponse.builder()
                .noCodeCanaries(noCodeCanarySummaries)
                .nextToken("test-next-token")
                .build();

        doReturn(listNoCodeCanariesResponse)
                .when(proxy)
                .injectCredentialsAndInvokeV2(
                        any(),
                        any()
                );

        final ProgressEvent<ResourceModel, CallbackContext> response =
            handler.handleRequest(proxy, request, CallbackContext.builder().build(), logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNotNull();
        assertThat(response.getResourceModels().size()).isEqualTo(2);
        assertThat(response.getResourceModels().get(0)).isEqualTo(Translator.constructModelFromSummary(noCodeCanarySummaries.get(0), ResourceModel.builder().build()));
        assertThat(response.getResourceModels().get(1)).isEqualTo(Translator.constructModelFromSummary(noCodeCanarySummaries.get(1), ResourceModel.builder().build()));
        assertThat(response.getNextToken()).isEqualTo("test-next-token");
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_EmptyList() {
        final ListHandler handler = new ListHandler();

        final ResourceModel model = ResourceModel.builder().name("listNoCodeCanary").build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final List<NoCodeCanarySummary> noCodeCanarySummaries = new ArrayList<>();

        final ListNoCodeCanariesResponse listNoCodeCanariesResponse = ListNoCodeCanariesResponse.builder()
                .noCodeCanaries(noCodeCanarySummaries)
                .nextToken("test-next-token")
                .build();

        doReturn(listNoCodeCanariesResponse)
                .when(proxy)
                .injectCredentialsAndInvokeV2(
                        any(),
                        any()
                );

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, CallbackContext.builder().build(), logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNotNull();
        assertThat(response.getResourceModels().size()).isEqualTo(0);
        assertThat(response.getNextToken()).isEqualTo("test-next-token");
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }
}
