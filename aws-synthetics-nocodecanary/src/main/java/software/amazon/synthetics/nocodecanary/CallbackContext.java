package software.amazon.synthetics.nocodecanary;

import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import software.amazon.cloudformation.exceptions.CfnNotStabilizedException;
import software.amazon.cloudformation.proxy.StdCallbackContext;
import software.amazon.awssdk.services.synthetics.model.*;

import lombok.Builder;
import lombok.Data;

import java.util.Objects;

@lombok.Getter
@lombok.Setter
@lombok.ToString
@lombok.EqualsAndHashCode(callSuper = true)
@Builder
@Data
@JsonDeserialize(builder = CallbackContext.CallbackContextBuilder.class)
public class CallbackContext extends StdCallbackContext {
    private boolean noCodeCanaryCreateStarted;
    private boolean noCodeCanaryUpdateStarted;
    private boolean noCodeCanaryDeleteStarted;
    private String retryKey;
    private int remainingRetryCount;
    private CanaryState initialNoCodeCanaryState;

    @JsonPOJOBuilder(withPrefix = "")
    public static class CallbackContextBuilder {
    }

    public void throwIfRetryLimitExceeded(int retryCount, String retryKey, ResourceModel model) {
        if (!Objects.equals(this.retryKey, retryKey)) {
            this.retryKey = retryKey;
            remainingRetryCount = retryCount;
        }

        --remainingRetryCount;
        if (remainingRetryCount == 0) {
            throw new CfnNotStabilizedException(ResourceModel.TYPE_NAME, model.getName());
        }
    }
}
