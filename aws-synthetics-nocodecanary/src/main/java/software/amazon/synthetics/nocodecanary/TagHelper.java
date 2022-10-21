package software.amazon.synthetics.nocodecanary;

import java.util.*;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.services.synthetics.model.*;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnResourceConflictException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.synthetics.nocodecanary.utils.Constants;

public class TagHelper {
    /**
     * convertToMap
     *
     * Converts a collection of Tag objects to a tag-name -> tag-value map.
     *
     * Note: Tag objects with null tag values will not be included in the output
     * map.
     *
     * @param tags Collection of tags to convert
     * @return Converted Map of tags
     */
    public static Map<String, String> convertToMap(final Collection<Tag> tags) {
        if (CollectionUtils.isEmpty(tags)) {
            return Collections.emptyMap();
        }
        return tags.stream()
            .filter(tag -> tag.getValue() != null)
            .collect(Collectors.toMap(
                Tag::getKey,
                Tag::getValue,
                (oldValue, newValue) -> newValue));
    }

    /**
     * convertToSet
     *
     * Converts a tag map to a set of Tag objects.
     *
     * Note: Like convertToMap, convertToSet filters out value-less tag entries.
     *
     * @param tagMap Map of tags to convert
     * @return Set of Tag objects
     */
    public static Set<Tag> convertToSet(final Map<String, String> tagMap) {
        if (MapUtils.isEmpty(tagMap)) {
            return Collections.emptySet();
        }
        return tagMap.entrySet().stream()
            .filter(tag -> tag.getValue() != null)
            .map(tag -> Tag.builder()
                .key(tag.getKey())
                .value(tag.getValue())
                .build())
            .collect(Collectors.toSet());
    }

    /**
     * generateTagsForCreate
     *
     * Generate tags to put into resource creation request.
     * This includes user defined tags and system tags as well.
     */
    public static Map<String, String> generateTagsForCreate(final ResourceModel resourceModel, final ResourceHandlerRequest<ResourceModel> handlerRequest) {
        final Map<String, String> tagMap = new HashMap<>();

        // merge system tags with desired resource tags if your service supports CloudFormation system tags
        tagMap.putAll(handlerRequest.getSystemTags());

        if (handlerRequest.getDesiredResourceTags() != null) {
            tagMap.putAll(handlerRequest.getDesiredResourceTags());
        }
        tagMap.putAll(convertToMap(resourceModel.getTags())); // getting tags from model
        return Collections.unmodifiableMap(tagMap);
    }

    /**
     * shouldUpdateTags
     *
     * Determines whether user defined tags have been changed during update.
     */
    public static boolean shouldUpdateTags(final ResourceModel resourceModel, final ResourceHandlerRequest<ResourceModel> handlerRequest) {
        final Map<String, String> previousTags = getPreviouslyAttachedTags(handlerRequest);
        final Map<String, String> desiredTags = getNewDesiredTags(resourceModel, handlerRequest);
        return ObjectUtils.notEqual(previousTags, desiredTags);
    }

    /**
     * getPreviouslyAttachedTags
     *
     * If stack tags and resource tags are not merged together in Configuration class,
     * we will get previous attached user defined tags from both handlerRequest.getPreviousResourceTags (stack tags)
     * and handlerRequest.getPreviousResourceState (resource tags).
     */
    public static Map<String, String> getPreviouslyAttachedTags(final ResourceHandlerRequest<ResourceModel> handlerRequest) {
        // get previous stack level tags from handlerRequest
        final Map<String, String> previousTags = handlerRequest.getPreviousResourceTags() != null ?
            handlerRequest.getPreviousResourceTags() : Collections.emptyMap();
        // resource tags
        previousTags.putAll(convertToMap(handlerRequest.getPreviousResourceState().getTags()));
        return previousTags;
    }

    /**
     * getNewDesiredTags
     *
     * If stack tags and resource tags are not merged together in Configuration class,
     * we will get new user defined tags from both resource model and previous stack tags.
     */
    public static Map<String, String> getNewDesiredTags(final ResourceModel resourceModel, final ResourceHandlerRequest<ResourceModel> handlerRequest) {
        // get new stack level tags from handlerRequest
        final Map<String, String> desiredTags = handlerRequest.getDesiredResourceTags() != null ?
            handlerRequest.getDesiredResourceTags() : Collections.emptyMap();

        desiredTags.putAll(convertToMap(resourceModel.getTags()));
        return desiredTags;
    }

    /**
     * generateTagsToAdd
     *
     * Determines the tags the customer desired to define or redefine.
     */
    public static Map<String, String> generateTagsToAdd(final Map<String, String> previousTags, final Map<String, String> desiredTags) {
        return desiredTags.entrySet().stream()
            .filter(e -> !previousTags.containsKey(e.getKey()) || !Objects.equals(previousTags.get(e.getKey()), e.getValue()))
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue));
    }

    /**
     * getTagsToRemove
     *
     * Determines the tags the customer desired to remove from the function.
     */
    public static Map<String, String> generateTagsToRemove(final Map<String, String> previousTags, final Map<String, String> desiredTags) {
        final Set<String> desiredTagNames = desiredTags.keySet();

        return previousTags.entrySet().stream()
            .filter(e -> !desiredTagNames.contains(e.getKey()))
            .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue));
    }

    /**
     * generateTagsToAdd
     *
     * Determines the tags the customer desired to define or redefine.
     */
    public Set<Tag> generateTagsToAdd(final Set<Tag> previousTags, final Set<Tag> desiredTags) {
        return Sets.difference(new HashSet<>(desiredTags), new HashSet<>(previousTags));
    }

    /**
     * getTagsToRemove
     *
     * Determines the tags the customer desired to remove from the function.
     */
    public Set<Tag> generateTagsToRemove(final Set<Tag> previousTags, final Set<Tag> desiredTags) {
        return Sets.difference(new HashSet<>(previousTags), new HashSet<>(desiredTags));
    }






    /**
     * Method to determine if the tags changed and return the tags to add and remove
     * @param model: To get the new tags
     * @param existingTags: Map of existing tags
     * @return Map of map of Tags to be added and removed
     */
    public static Map<String, Map<String, String>> updateTags(ResourceModel model, Map<String, String> existingTags) {
        Map<String, String> modelTagMap = new HashMap<>();
        List<Tag> modelTagList = model.getTags() == null ? new ArrayList<>() : model.getTags();
        Set<Map.Entry<String, String>> modelTagsES = null;
        Set<Map.Entry<String, String>> noCodeCanaryTags = null;
        Set<Map.Entry<String, String>> modelTagsCopyES = null;
        Map<String, Map<String, String>> store = new HashMap<String, Map<String, String>>();
        Map<String, String> copyExistingTags = new HashMap<>(existingTags);

        if (modelTagList != null) {
            for (Tag tag : modelTagList) {
                modelTagMap.put(tag.getKey(), tag.getValue());
            }
            modelTagsES = modelTagMap.entrySet();
            modelTagsCopyES = new HashSet<Map.Entry<String, String>>(modelTagMap.entrySet());
        }

        noCodeCanaryTags = copyExistingTags.entrySet();

        if (modelTagList == null) {
            return null;
        }
        Set<Map.Entry<String, String>> finalNoCodeCanaryTags = noCodeCanaryTags;
        // Get an iterator
        Iterator<Map.Entry<String, String>> modelIterator = modelTagsES.iterator();
        while (modelIterator.hasNext()) {
            Map.Entry<String, String> modelEntry = modelIterator.next();
            if (finalNoCodeCanaryTags.contains(modelEntry)) {
                modelIterator.remove();
            }
        }
        // Store all the tags that need to be added to the group
        store.put(Constants.ADD_TAGS, modelTagMap);

        Iterator<Map.Entry<String, String>> noCodeCanaryTagIterator = finalNoCodeCanaryTags.iterator();
        while (noCodeCanaryTagIterator.hasNext()) {
            Map.Entry<String, String> canaryEntry = noCodeCanaryTagIterator.next();
            try {
                if (modelTagsCopyES.contains(canaryEntry)) {
                    noCodeCanaryTagIterator.remove();
                }
                if (canaryEntry.getKey().toString().startsWith("aws:")) {
                    noCodeCanaryTagIterator.remove();
                }
                if (!modelTagMap.isEmpty() && modelTagMap.containsKey(canaryEntry.getKey())) {
                    noCodeCanaryTagIterator.remove();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // Store all the tags that need to be removed from the no code canary
        store.put(Constants.REMOVE_TAGS, copyExistingTags);
        return store;
    }

}
