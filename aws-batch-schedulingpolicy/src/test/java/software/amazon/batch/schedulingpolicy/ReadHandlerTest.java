package software.amazon.batch.schedulingpolicy;

import software.amazon.awssdk.services.batch.BatchClient;
import software.amazon.awssdk.services.batch.model.BatchException;
import software.amazon.awssdk.services.batch.model.DescribeSchedulingPoliciesResponse;
import software.amazon.awssdk.services.batch.model.SchedulingPolicyDetail;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;

@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    @Mock
    private BatchClient batchClient;

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
        batchClient = BatchClientBuilderTestHelper.getClient();
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final ReadHandler handler = new ReadHandler(batchClient);

        final ResourceModel model = ResourceModel.builder()
                                                 .arn("test:arn")
                                                 .tags(new HashMap<String, String>())
                                                 .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        SchedulingPolicyDetail schedulingPolicyDetail = SchedulingPolicyDetail.builder()
                                                                              .arn("test:arn")
                                                                              .build();

        when(proxy.injectCredentialsAndInvokeV2(any(), any())).thenReturn(
                DescribeSchedulingPoliciesResponse.builder()
                                                  .schedulingPolicies(schedulingPolicyDetail)
                                                  .build());

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_ResourceNotFound() {
        final ReadHandler handler = new ReadHandler(batchClient);

        final ResourceModel model = ResourceModel.builder()
                                                 .arn("test:arn")
                                                 .tags(new HashMap<String, String>())
                                                 .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        when(proxy.injectCredentialsAndInvokeV2(any(), any())).thenReturn(
                DescribeSchedulingPoliciesResponse.builder()
                                                  .build());

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isEqualTo(String.format("Resource %s not found.", model.getArn()));
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.NotFound);
    }

    @Test
    public void handleRequest_catchExceptions() {

        final ReadHandler handler = new ReadHandler(batchClient);

        when(proxy.injectCredentialsAndInvokeV2(any(), any()))
                .thenThrow(BatchException.builder()
                                         .statusCode(Constants.INVALID_INPUT_STATUS_CODE_400)
                                         .message("Only scheduling policy Arn can be allowed")
                                         .build())
                .thenThrow(BatchException.builder()
                                         .statusCode(Constants.ACCESS_DENIED_STATUS_CODE_403)
                                         .build())
                .thenThrow(BatchException.builder()
                                         .build());

        final ResourceModel model = ResourceModel.builder()
                                                 .name("testSchedulingPolicy")
                                                 .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnInvalidRequestException.class, () ->
                handler.handleRequest(proxy, request, null, logger));
        assertThrows(CfnAccessDeniedException.class, () ->
                handler.handleRequest(proxy, request, null, logger));
        assertThrows(CfnGeneralServiceException.class, () ->
                handler.handleRequest(proxy, request, null, logger));
    }
}
