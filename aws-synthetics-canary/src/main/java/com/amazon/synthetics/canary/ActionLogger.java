package com.amazon.synthetics.canary;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.proxy.Logger;

public class ActionLogger {
    private final Logger logger;
    private final Action action;
    private final String awsAccountId;
    private final CallbackContext context;
    private final ResourceModel model;
    private final ObjectMapper mapper = new ObjectMapper();

    public ActionLogger(Logger logger, Action action, String awsAccountId, CallbackContext context, ResourceModel model) {
        this.logger = logger;
        this.action = action;
        this.awsAccountId = awsAccountId;
        this.context = context;
        this.model = model;
    }

    public void log(String message) {
        Payload payload = new Payload(
            action,
            awsAccountId,
            context.getRetryKey(),
            context.getRemainingRetryCount(),
            model.getName(),
            message,
            null);
        try {
            String json = mapper.writeValueAsString(payload);
            logger.log(json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public void log(Exception exception) {
        Payload payload = new Payload(
            action,
            awsAccountId,
            context.getRetryKey(),
            context.getRemainingRetryCount(),
            model.getName(),
            null,
            exception);
        try {
            String json = mapper.writeValueAsString(payload);
            logger.log(json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Data
    @AllArgsConstructor
    private static class Payload {
        private final Action action;
        private final String awsAccountId;
        private final String retryKey;
        private final Integer remainingRetryCount;
        private final String resourceName;
        private final String message;
        private final Exception exception;
    }
}
