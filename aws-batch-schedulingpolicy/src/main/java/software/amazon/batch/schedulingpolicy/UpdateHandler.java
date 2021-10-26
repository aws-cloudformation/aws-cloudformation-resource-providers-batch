package software.amazon.batch.schedulingpolicy;

import lombok.NoArgsConstructor;
import software.amazon.awssdk.services.batch.BatchClient;
import software.amazon.awssdk.services.batch.model.BatchException;
import software.amazon.awssdk.services.batch.model.UpdateSchedulingPolicyResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;


/**
 * Update Handler for Scheduling Policy Resource.
 */
@NoArgsConstructor
public class UpdateHandler extends BaseHandler<CallbackContext> {

    private BatchClient client;

    public UpdateHandler(final BatchClient client) {
        this.client = client;
    }

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        logger.log("Update handler for SchedulingPolicy invoked.");

        if (this.client == null) {
            this.client = ClientBuilder.getClient();
        }

        final ResourceModel model = request.getDesiredResourceState();
        logger.log("SchedulingPolicy model for update request: " + model.toString());

        try {
            UpdateSchedulingPolicyResponse updateSchedulingPolicyResponse =
                    proxy.injectCredentialsAndInvokeV2(Translator.toUpdateSchedulingPolicyRequest(model),
                            this.client::updateSchedulingPolicy);

            TagUtils.handleTagging(model, request.getDesiredResourceTags(),
                    request.getPreviousResourceTags(), proxy, this.client, logger);

            logger.log(String.format("%s [%s] updated successfully", ResourceModel.TYPE_NAME, model.getArn()));
            logger.log("Update scheduling policy final result: " + updateSchedulingPolicyResponse.toString());
        } catch (final BatchException e) {
            Exceptions.handleBatchExceptions(request, callbackContext, e);
        }

        return ProgressEvent.defaultSuccessHandler(model);
    }
}
