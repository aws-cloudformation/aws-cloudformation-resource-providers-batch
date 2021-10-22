package software.amazon.batch.schedulingpolicy;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.batch.BatchClient;
import software.amazon.cloudformation.LambdaWrapper;

public class BatchClientBuilderTestHelper {

    private static Region region = Region.of("us-east-1");

    static BatchClient getClient() {
        return BatchClient.builder()
                        .httpClient(LambdaWrapper.HTTP_CLIENT)
                        .region(region)
                        .build();
    }
}
