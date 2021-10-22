package software.amazon.batch.schedulingpolicy;

import java.util.Collections;

import lombok.NoArgsConstructor;
import software.amazon.awssdk.services.batch.BatchClient;
import software.amazon.awssdk.services.batch.model.BatchException;
import software.amazon.awssdk.services.batch.model.DeleteSchedulingPolicyResponse;
import software.amazon.awssdk.services.batch.model.DescribeSchedulingPoliciesResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.HandlerErrorCode;


/**
 * Delete Handler for Scheduling Policy Resource.
 */
@NoArgsConstructor
public class DeleteHandler extends BaseHandler<CallbackContext> {

    private BatchClient client;

    public DeleteHandler(final BatchClient client) {
        this.client = client;
    }

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        logger.log("Delete handler for SchedulingPolicy invoked.");

        if (this.client == null) {
            this.client = ClientBuilder.getClient();
        }

        final ResourceModel model = request.getDesiredResourceState();
        logger.log("SchedulingPolicy model for delete request: " + model.toString());

        final DescribeSchedulingPoliciesResponse describeSchedulingPoliciesResponse;
        try {
            describeSchedulingPoliciesResponse = proxy.injectCredentialsAndInvokeV2(
                    Translator.toDescribeSchedulingPoliciesRequest(Collections.singletonList(model.getArn())),
                    this.client::describeSchedulingPolicies);

            if (describeSchedulingPoliciesResponse.schedulingPolicies().size() == 0) {
                return ProgressEvent.failed(request.getDesiredResourceState(), callbackContext,
                        HandlerErrorCode.NotFound,
                        String.format("Resource of type '%s' with identifier '%s' was not found.",
                                ResourceModel.TYPE_NAME,
                                model.getArn()));
            }

            DeleteSchedulingPolicyResponse deleteSchedulingPolicyResponse =
                    proxy.injectCredentialsAndInvokeV2(Translator.toDeleteSchedulingPolicyRequest(model),
                    this.client::deleteSchedulingPolicy);
            logger.log(String.format("%s [%s] deleted successfully", ResourceModel.TYPE_NAME, model.getArn()));
            logger.log("Delete scheduling policy handler final result: " + deleteSchedulingPolicyResponse.toString());
        } catch (final BatchException e) {
            Exceptions.handleBatchExceptions(request, callbackContext, e);
        }

        return ProgressEvent.defaultSuccessHandler(null);
    }
}
