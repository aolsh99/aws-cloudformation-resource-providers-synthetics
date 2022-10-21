package software.amazon.synthetics.nocodecanary;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

import org.mockito.Mock;
import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.services.synthetics.model.*;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Credentials;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.proxy.ProxyClient;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class AbstractTestBase {
  protected static final Credentials MOCK_CREDENTIALS;
  protected static final LoggerProxy logger;
  @Mock
  protected static AmazonWebServicesClientProxy proxy;

  protected static final String NO_CODE_CANARY_NAME = "test-no-code-canary";

  static {
    MOCK_CREDENTIALS = new Credentials("accessKey", "secretKey", "token");
    logger = new LoggerProxy();
  }
  static ProxyClient<SdkClient> MOCK_PROXY(
    final AmazonWebServicesClientProxy proxy,
    final SdkClient sdkClient) {
    return new ProxyClient<SdkClient>() {
      @Override
      public <RequestT extends AwsRequest, ResponseT extends AwsResponse> ResponseT
      injectCredentialsAndInvokeV2(RequestT request, Function<RequestT, ResponseT> requestFunction) {
        return proxy.injectCredentialsAndInvokeV2(request, requestFunction);
      }

      @Override
      public <RequestT extends AwsRequest, ResponseT extends AwsResponse>
      CompletableFuture<ResponseT>
      injectCredentialsAndInvokeV2Async(RequestT request, Function<RequestT, CompletableFuture<ResponseT>> requestFunction) {
        throw new UnsupportedOperationException();
      }

      @Override
      public <RequestT extends AwsRequest, ResponseT extends AwsResponse, IterableT extends SdkIterable<ResponseT>>
      IterableT
      injectCredentialsAndInvokeIterableV2(RequestT request, Function<RequestT, IterableT> requestFunction) {
        return proxy.injectCredentialsAndInvokeIterableV2(request, requestFunction);
      }

      @Override
      public <RequestT extends AwsRequest, ResponseT extends AwsResponse> ResponseInputStream<ResponseT>
      injectCredentialsAndInvokeV2InputStream(RequestT requestT, Function<RequestT, ResponseInputStream<ResponseT>> function) {
        throw new UnsupportedOperationException();
      }

      @Override
      public <RequestT extends AwsRequest, ResponseT extends AwsResponse> ResponseBytes<ResponseT>
      injectCredentialsAndInvokeV2Bytes(RequestT requestT, Function<RequestT, ResponseBytes<ResponseT>> function) {
        throw new UnsupportedOperationException();
      }

      @Override
      public SdkClient client() {
        return sdkClient;
      }


    };
  }

  public static ResourceModel buildModelForRead(String noCodeCanaryName) {
    List<String> endpoints = new ArrayList<>();
    endpoints.add("https://amazon.com");
    ResourceModel model = ResourceModel.builder()
            .name(noCodeCanaryName)
            .endpointList(endpoints)
            .state(NoCodeCanaryState.RUNNING.toString())
            .schedule(Schedule.builder().durationInSeconds("10").expression("rate(10 seconds)").build())
            .build();
    return model;
  }

  protected void configureGetNoCodeCanaryResponse(Throwable throwable) {
    when(proxy.injectCredentialsAndInvokeV2(eq(GetNoCodeCanaryRequest.builder().noCodeCanaryIdentifier(NO_CODE_CANARY_NAME).build()), any()))
            .thenThrow(throwable);
  }
  /*
   **********************  Test Outputs ******************************
   */
  public static NoCodeCanary noCodeCanaryResponseObjectForTesting(String noCodeCanaryName) {
    return NoCodeCanary.builder()
            .name(noCodeCanaryName)
            .endpoint("https://amazon.com")
            .noCodeCanaryState(NoCodeCanaryState.RUNNING.toString())
            .schedule(NoCodeCanarySchedule.builder().durationInSeconds(10L).expression("rate(10 seconds)").build())
            .build();
  }

  public static NoCodeCanarySummary noCodeCanarySummaryResponseObjectForTesting(String noCodeCanaryName) {
    return NoCodeCanarySummary.builder()
            .name(noCodeCanaryName)
            .endpoint("https://amazon.com")
            .schedule(NoCodeCanarySchedule.builder().durationInSeconds(10L).expression("rate(10 seconds)").build())
            .build();
  }
}
