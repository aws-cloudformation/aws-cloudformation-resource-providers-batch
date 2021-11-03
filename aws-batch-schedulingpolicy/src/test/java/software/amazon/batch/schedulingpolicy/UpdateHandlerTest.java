package software.amazon.batch.schedulingpolicy;

import software.amazon.awssdk.services.batch.model.BatchException;
import software.amazon.awssdk.services.batch.model.TagResourceResponse;
import software.amazon.awssdk.services.batch.model.UntagResourceResponse;
import software.amazon.awssdk.services.batch.model.UpdateSchedulingPolicyResponse;
import software.amazon.awssdk.services.batch.BatchClient;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
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
public class UpdateHandlerTest {

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
        final UpdateHandler handler = new UpdateHandler(batchClient);

        final ResourceModel model = ResourceModel.builder().build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .desiredResourceTags(new HashMap<String, String>())
                .previousResourceTags(new HashMap<String, String>())
                .build();

        when(proxy.injectCredentialsAndInvokeV2(any(), any())).thenReturn(
                UpdateSchedulingPolicyResponse.builder().build(),
                UntagResourceResponse.builder().build(),
                TagResourceResponse.builder().build());

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
    public void handleRequest_catchExceptions() {

        final UpdateHandler handler = new UpdateHandler(batchClient);

        when(proxy.injectCredentialsAndInvokeV2(any(), any()))
                .thenThrow(BatchException.builder()
                                         .statusCode(Constants.INVALID_INPUT_STATUS_CODE_400)
                                         .message("Only scheduling policy Arn can be allowed")
                                         .build())
                .thenThrow(BatchException.builder()
                                         .statusCode(Constants.INVALID_INPUT_STATUS_CODE_400)
                                         .message("Resource does not exist")
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
        assertThrows(CfnNotFoundException.class, () ->
                handler.handleRequest(proxy, request, null, logger));
        assertThrows(CfnAccessDeniedException.class, () ->
                handler.handleRequest(proxy, request, null, logger));
        assertThrows(CfnGeneralServiceException.class, () ->
                handler.handleRequest(proxy, request, null, logger));
    }
}
