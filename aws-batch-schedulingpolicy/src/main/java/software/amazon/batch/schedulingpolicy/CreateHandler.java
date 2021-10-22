package software.amazon.batch.schedulingpolicy;

import lombok.NoArgsConstructor;
import software.amazon.awssdk.services.batch.BatchClient;
import software.amazon.awssdk.services.batch.model.BatchException;
import software.amazon.awssdk.services.batch.model.CreateSchedulingPolicyResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.HashMap;
import java.util.Random;
import java.util.Map;
import org.apache.commons.lang3.RandomStringUtils;


/**
 * Create Handler for Scheduling Policy Resource.
 */
@NoArgsConstructor
public class CreateHandler extends BaseHandler<CallbackContext> {

    private BatchClient client;

    public CreateHandler(final BatchClient client) {
        this.client = client;
    }

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        logger.log("Create handler for SchedulingPolicy invoked.");

        if (this.client == null) {
            this.client = ClientBuilder.getClient();
        }

        final ResourceModel model = request.getDesiredResourceState();
        if (model.getName() == null) {
            model.setName(generateParameterName(
                    request.getLogicalResourceIdentifier(),
                    request.getClientRequestToken()
            ));
        }
        logger.log("SchedulingPolicy model for create request: " + model.toString());

        Map<String, String> tags = new HashMap<>();
        if (model.getTags() != null && !model.getTags().isEmpty()) {
            tags = model.getTags();
        }

        if (request.getDesiredResourceTags() != null && !request.getDesiredResourceTags().isEmpty()) {
            tags.putAll(request.getDesiredResourceTags());
        }
        logger.log("Tags to add for" + model.getArn() + " are : " + tags);

        try {
            CreateSchedulingPolicyResponse createSchedulingPolicyResponse =
                    proxy.injectCredentialsAndInvokeV2(
                            Translator.toCreateSchedulingPolicyRequest(model, tags),
                            this.client::createSchedulingPolicy);
            logger.log(String.format("%s [%s] created successfully", ResourceModel.TYPE_NAME, model.getName()));
            model.setArn(createSchedulingPolicyResponse.arn());
            logger.log("Create scheduling policy handler final result: " + createSchedulingPolicyResponse.toString());

        } catch (final BatchException e) {
            Exceptions.handleBatchExceptions(request, callbackContext, e);
        }

        return ProgressEvent.defaultSuccessHandler(model);
    }

    private String generateParameterName(final String logicalResourceId, final String clientRequestToken) {
        StringBuilder sb = new StringBuilder();
        int endIndex = Math.min(logicalResourceId.length(), Constants.ALLOWED_LOGICAL_RESOURCE_ID_LENGTH);

        sb.append(logicalResourceId.substring(0, endIndex));
        sb.append("-");

        sb.append(RandomStringUtils.random(
                Constants.GUID_LENGTH,
                0,
                0,
                true,
                true,
                null,
                new Random(clientRequestToken.hashCode())));
        return sb.toString();

    }
}
