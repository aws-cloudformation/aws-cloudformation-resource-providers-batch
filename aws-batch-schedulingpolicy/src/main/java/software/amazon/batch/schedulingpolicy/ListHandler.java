package software.amazon.batch.schedulingpolicy;

import lombok.NoArgsConstructor;
import com.google.common.base.Strings;
import software.amazon.awssdk.services.batch.BatchClient;
import software.amazon.awssdk.services.batch.model.BatchException;
import software.amazon.awssdk.services.batch.model.DescribeSchedulingPoliciesResponse;
import software.amazon.awssdk.services.batch.model.ListSchedulingPoliciesResponse;
import software.amazon.awssdk.services.batch.model.SchedulingPolicyListingDetail;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * List Handler for Scheduling Policy Resource.
 */
@NoArgsConstructor
public class ListHandler extends BaseHandler<CallbackContext> {

    private BatchClient client;

    public ListHandler(final BatchClient client) {
        this.client = client;
    }

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        logger.log("List handler for SchedulingPolicy invoked.");

        if (this.client == null) {
            this.client = ClientBuilder.getClient();
        }
        final List<ResourceModel> models = listSchedulingPolicies(request, callbackContext, logger, proxy);

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
            .resourceModels(models)
            .status(OperationStatus.SUCCESS)
            .build();
    }

    private List<ResourceModel> listSchedulingPolicies(final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext, final Logger logger, final AmazonWebServicesClientProxy proxy) {
        List<ResourceModel> schedulingPolicies = new ArrayList<>();
        String nextToken = null;

        do {
            try {
                // List API call returns the arns of the Scheduling Policies.
                ListSchedulingPoliciesResponse listSchedulingPoliciesResponse =
                        proxy.injectCredentialsAndInvokeV2(Translator.toListSchedulingPoliciesRequest(nextToken),
                                this.client::listSchedulingPolicies);

                List<String> arns = listSchedulingPoliciesResponse.schedulingPolicies()
                                                                  .stream()
                                                                  .map(SchedulingPolicyListingDetail::arn)
                                                                  .collect(Collectors.toList());

                if (!arns.isEmpty()) {
                    // Get specific information about all the Scheduling Policies with Describe API call.
                    DescribeSchedulingPoliciesResponse describeSchedulingPoliciesResponse =
                            proxy.injectCredentialsAndInvokeV2(Translator.toDescribeSchedulingPoliciesRequest(arns),
                                    this.client::describeSchedulingPolicies);

                    List<ResourceModel> models =
                            describeSchedulingPoliciesResponse.schedulingPolicies()
                                                              .stream()
                                                              .map(Translator::toModelSchedulingPolicy)
                                                              .collect(Collectors.toList());

                    schedulingPolicies.addAll(models);
                }
                nextToken = listSchedulingPoliciesResponse.nextToken();
            } catch (final BatchException e) {
                Exceptions.handleBatchExceptions(request, callbackContext, e);
            }
        } while (!Strings.isNullOrEmpty(nextToken));
        logger.log("List scheduling policy handler final result: " + schedulingPolicies.toString());

        return schedulingPolicies;
    }
}
