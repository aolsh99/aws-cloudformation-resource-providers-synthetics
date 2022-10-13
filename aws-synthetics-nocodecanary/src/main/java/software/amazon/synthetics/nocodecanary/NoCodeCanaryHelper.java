package software.amazon.synthetics.nocodecanary;

import software.amazon.awssdk.services.synthetics.SyntheticsClient;
import software.amazon.awssdk.services.synthetics.model.Canary;
import software.amazon.awssdk.services.synthetics.model.GetCanaryRequest;
import software.amazon.awssdk.services.synthetics.model.GetCanaryResponse;
import software.amazon.awssdk.services.synthetics.model.ResourceNotFoundException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;

public class NoCodeCanaryHelper {

    public static Canary getNoCodeCanaryOrNull(AmazonWebServicesClientProxy proxy,
                                         SyntheticsClient syntheticsClient,
                                         String canaryName) {
        try {
            return getNoCodeCanary(proxy, syntheticsClient, canaryName);
        } catch (ResourceNotFoundException e) {
            return null;
        }
    }
    public static Canary getNoCodeCanaryOrThrow(AmazonWebServicesClientProxy proxy,
                                          SyntheticsClient syntheticsClient,
                                          ResourceModel model) {
        return getNoCodeCanaryOrThrow(proxy, syntheticsClient, model.getName());
    }
    public static Canary getNoCodeCanaryOrThrow(AmazonWebServicesClientProxy proxy,
                                          SyntheticsClient syntheticsClient,
                                          String canaryName) {
        try {
            return getNoCodeCanary(proxy, syntheticsClient, canaryName);
        } catch (ResourceNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, canaryName, e);
        }
    }

    private static Canary getNoCodeCanary(AmazonWebServicesClientProxy proxy,
                                    SyntheticsClient syntheticsClient,
                                    String canaryName) {
        GetCanaryResponse response = proxy.injectCredentialsAndInvokeV2(
                GetCanaryRequest.builder()
                        .name(canaryName)
                        .build(),
                syntheticsClient::getCanary);  // TODO: change to no code canary api
        return response.canary();
    }
}
