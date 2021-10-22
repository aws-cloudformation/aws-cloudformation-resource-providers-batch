package software.amazon.batch.schedulingpolicy;

import lombok.NoArgsConstructor;
import software.amazon.awssdk.services.batch.BatchClient;
import software.amazon.awssdk.services.batch.model.BatchException;
import software.amazon.awssdk.services.batch.model.DescribeSchedulingPoliciesResponse;
import software.amazon.awssdk.services.batch.model.SchedulingPolicyDetail;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.HandlerErrorCode;

import java.util.Collections;


/**
 * Read Handler for Scheduling Policy Resource.
 */
@NoArgsConstructor
public class ReadHandler extends BaseHandler<CallbackContext> {

    private BatchClient client;

    public ReadHandler(final BatchClient client) {
        this.client = client;
    }


    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        logger.log("Read handler for SchedulingPolicy invoked.");

        if (this.client == null) {
            this.client = ClientBuilder.getClient();
        }

        final ResourceModel model = request.getDesiredResourceState();
        logger.log("SchedulingPolicy model for delete request: " + model.toString());

        try {
            final DescribeSchedulingPoliciesResponse response =
                    proxy.injectCredentialsAndInvokeV2(Translator.toDescribeSchedulingPoliciesRequest(
                            Collections.singletonList(model.getArn())),
                    this.client::describeSchedulingPolicies);

            if (response.schedulingPolicies().size() != 0) {
                SchedulingPolicyDetail schedulingPolicyDetail = response.schedulingPolicies().get(0);
                logger.log("Read scheduling policy final result: " + schedulingPolicyDetail.toString());

                return ProgressEvent.<ResourceModel, CallbackContext>builder()
                        .resourceModel(Translator.toModelSchedulingPolicy(schedulingPolicyDetail))
                        .status(OperationStatus.SUCCESS)
                        .build();
            }
        } catch (final BatchException e) {
            Exceptions.handleBatchExceptions(request, callbackContext, e);
        }

        return ProgressEvent.failed(request.getDesiredResourceState(), callbackContext,  HandlerErrorCode.NotFound,
                String.format("Resource %s not found.", model.getArn()));
    }
}
