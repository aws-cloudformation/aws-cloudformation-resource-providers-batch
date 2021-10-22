package software.amazon.batch.schedulingpolicy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import software.amazon.awssdk.services.batch.BatchClient;
import software.amazon.awssdk.services.batch.model.TagResourceResponse;
import software.amazon.awssdk.services.batch.model.UntagResourceResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;


/**
 * Tagging utils for Scheduling Policy Resource.
 */
public class TagUtils {

    /**
     * Method for handling the propagation of stack tags to the resource during an update operation.
     * Resource tags in Batch are immutable.
    */
    static void handleTagging(
            final ResourceModel model,
            final Map<String, String> desiredResourceTags,
            final Map<String, String> previousResourceTags,
            final AmazonWebServicesClientProxy proxy,
            final BatchClient client,
            final Logger logger) {
        final Map<String, String> tagsToRemove = getTagsToDelete(desiredResourceTags, previousResourceTags);
        final Map<String, String> tagsToAdd = getTagsToCreate(desiredResourceTags, previousResourceTags);

        final List<String> tagKeysToRemove = new ArrayList<>(tagsToRemove.keySet());

        // Deletes tags only if tagsToRemove is not empty.
        if (!tagKeysToRemove.isEmpty()) {
            logger.log(String.format("Tags to remove for %s are %s", model.getArn(), tagKeysToRemove));
            UntagResourceResponse untagResourceResponse = proxy.injectCredentialsAndInvokeV2(
                    Translator.toUntagResourceRequest(model, tagKeysToRemove),
                    client::untagResource);
            logger.log(String.format("Untag API result for %s is %s", model.getArn(), untagResourceResponse));
        }

        // Adds tags only if tagsToAdd is not empty.
        if (!tagsToAdd.isEmpty()) {
            logger.log(String.format("Tags to add for %s are %s", model.getArn(), tagsToAdd));
            TagResourceResponse tagResourceResponse = proxy.injectCredentialsAndInvokeV2(
                    Translator.toTagResourceRequest(model, tagsToAdd),
                    client::tagResource);
            logger.log(String.format("Tag API result for %s is %s", model.getArn(), tagResourceResponse));
        }
    }

    /**
     * @param newTags current stack tags
     * @param oldTags previous stack tags
     * @return the tags to delete
     */
    static Map<String, String> getTagsToDelete(final Map<String, String> newTags, final Map<String, String> oldTags) {
        final Map<String, String> tags = new HashMap<>();
        final Set<String> removedKeys = Sets.difference(oldTags.keySet(), newTags.keySet());
        for (String key : removedKeys) {
            tags.put(key, oldTags.get(key));
        }
        return tags;
    }

    /**
     * @param newTags current stack tags
     * @param oldTags previous stack tags
     * @return the tags to create
     */
    static Map<String, String> getTagsToCreate(final Map<String, String> newTags, final Map<String, String> oldTags) {
        final Map<String, String> tags = new HashMap<>();
        final Set<Map.Entry<String, String>> entriesToCreate = Sets.difference(newTags.entrySet(), oldTags.entrySet());
        for (Map.Entry<String, String> entry : entriesToCreate) {
            tags.put(entry.getKey(), entry.getValue());
        }
        return tags;
    }
}
