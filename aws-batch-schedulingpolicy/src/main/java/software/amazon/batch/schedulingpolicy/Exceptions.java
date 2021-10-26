package software.amazon.batch.schedulingpolicy;

import software.amazon.awssdk.services.batch.model.BatchException;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;


/**
 * Helper class for exception handling in resource handlers.
 */
public class Exceptions {

    /**
     * Common function for mapping Batch exception codes to CFN exceptions.
     */
    static void handleBatchExceptions(final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext, final BatchException e) {
        if (e.statusCode() == Constants.INVALID_INPUT_STATUS_CODE_400) {
            handleInvalidInputException(request, callbackContext, e);
        } else if (e.statusCode() == Constants.ACCESS_DENIED_STATUS_CODE_403) {
            throw new CfnAccessDeniedException(ResourceModel.TYPE_NAME, e);
        } else if (e.statusCode() == Constants.ALREADY_EXISTS_STATUS_CODE_409) {
            throw new CfnAlreadyExistsException(e);
        } else {
            throw new CfnGeneralServiceException(e);
        }
    }

    static void handleInvalidInputException(final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext, final BatchException e) {
        // Batch returns a 400 error code for resource not found instead of a 404.
        // Other 400 errors are Client Exceptions and are mapped to Invalid Request Exception.
        if (e.getMessage().contains(Constants.DOES_NOT_EXIST)) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, request.getDesiredResourceState().getArn(), e);
        } else {
            throw new CfnInvalidRequestException(e);
        }
    }
}
