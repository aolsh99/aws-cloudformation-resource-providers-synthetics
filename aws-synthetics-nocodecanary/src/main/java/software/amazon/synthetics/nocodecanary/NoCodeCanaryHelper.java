package software.amazon.synthetics.nocodecanary;

import software.amazon.awssdk.services.synthetics.SyntheticsClient;
import software.amazon.awssdk.services.synthetics.model.*;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;

public class NoCodeCanaryHelper {

    public static NoCodeCanary getNoCodeCanaryOrNull(AmazonWebServicesClientProxy proxy,
                                         SyntheticsClient syntheticsClient,
                                         String canaryName) {
        try {
            return getNoCodeCanary(proxy, syntheticsClient, canaryName);
        } catch (ResourceNotFoundException e) {
            return null;
        }
    }
    public static NoCodeCanary getNoCodeCanaryOrThrow(AmazonWebServicesClientProxy proxy,
                                          SyntheticsClient syntheticsClient,
                                          ResourceModel model) {
        return getNoCodeCanaryOrThrow(proxy, syntheticsClient, model.getName());
    }
    public static NoCodeCanary getNoCodeCanaryOrThrow(AmazonWebServicesClientProxy proxy,
                                          SyntheticsClient syntheticsClient,
                                          String canaryName) {
        try {
            return getNoCodeCanary(proxy, syntheticsClient, canaryName);
        } catch (ResourceNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, canaryName, e);
        }
    }

    private static NoCodeCanary getNoCodeCanary(AmazonWebServicesClientProxy proxy,
                                    SyntheticsClient syntheticsClient,
                                    String canaryName) {
        GetNoCodeCanaryResponse response = proxy.injectCredentialsAndInvokeV2(
                GetNoCodeCanaryRequest.builder()
                        .noCodeCanaryIdentifier(canaryName)
                        .build(),
                syntheticsClient::getNoCodeCanary);
        return response.noCodeCanary();
    }
}
