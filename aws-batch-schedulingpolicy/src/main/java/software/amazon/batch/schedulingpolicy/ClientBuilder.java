package software.amazon.batch.schedulingpolicy;

import software.amazon.awssdk.services.batch.BatchClient;
import software.amazon.cloudformation.LambdaWrapper;


/**
 * ClientBuilder Class for Batch Client.
 */
class ClientBuilder {
    static BatchClient getClient() {
        return BatchClient.builder()
                        .httpClient(LambdaWrapper.HTTP_CLIENT)
                        .build();
    }
}
