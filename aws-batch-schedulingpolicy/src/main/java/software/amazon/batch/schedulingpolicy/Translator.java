package software.amazon.batch.schedulingpolicy;

import software.amazon.awssdk.services.batch.model.CreateSchedulingPolicyRequest;
import software.amazon.awssdk.services.batch.model.DeleteSchedulingPolicyRequest;
import software.amazon.awssdk.services.batch.model.DescribeSchedulingPoliciesRequest;
import software.amazon.awssdk.services.batch.model.ListSchedulingPoliciesRequest;
import software.amazon.awssdk.services.batch.model.TagResourceRequest;
import software.amazon.awssdk.services.batch.model.UntagResourceRequest;
import software.amazon.awssdk.services.batch.model.UpdateSchedulingPolicyRequest;
import software.amazon.awssdk.services.batch.model.FairsharePolicy;
import software.amazon.awssdk.services.batch.model.SchedulingPolicyDetail;
import software.amazon.awssdk.services.batch.model.ShareAttributes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Translator class that provides methods for request and response translations from sdk to CFN model and vice-versa.
 */
public class Translator {

    static ResourceModel toModelSchedulingPolicy(final SchedulingPolicyDetail schedulingPolicyDetail) {
        return ResourceModel
                .builder()
                .arn(schedulingPolicyDetail.arn())
                .name(schedulingPolicyDetail.name())
                .fairsharePolicy(schedulingPolicyDetail.fairsharePolicy() != null
                                 ? toModelFairsharePolicy(schedulingPolicyDetail.fairsharePolicy()) : null)
                .tags(schedulingPolicyDetail.tags())
                .build();

    }

    static FairsharePolicy toRequestFairSharePolicy(
            final software.amazon.batch.schedulingpolicy.FairsharePolicy fairSharePolicy) {
        return FairsharePolicy.builder()
                              .shareDecaySeconds(fairSharePolicy.getShareDecaySeconds() != null
                                                 ? fairSharePolicy.getShareDecaySeconds().intValue() : null)
                              .computeReservation(fairSharePolicy.getComputeReservation() != null
                                                  ? fairSharePolicy.getComputeReservation().intValue() : null)
                              .shareDistribution(fairSharePolicy.getShareDistribution() != null
                                                 ? toShareDistribution(fairSharePolicy.getShareDistribution()) : null)
                              .build();

    }

    static List<ShareAttributes> toShareDistribution(
            final List<software.amazon.batch.schedulingpolicy.ShareAttributes> shareAttributesList) {
        return shareAttributesList.stream()
                     .map(s -> ShareAttributes.builder()
                                              .shareIdentifier(s.getShareIdentifier() != null
                                                               ? s.getShareIdentifier() : null)
                                              .weightFactor(s.getWeightFactor() != null
                                                            ? s.getWeightFactor().floatValue() : null)
                                              .build())
                     .collect(Collectors.toList());
    }

    static software.amazon.batch.schedulingpolicy.FairsharePolicy toModelFairsharePolicy(
            final FairsharePolicy fairsharePolicy) {
        return software.amazon.batch.schedulingpolicy.FairsharePolicy
                .builder()
                .computeReservation(fairsharePolicy.computeReservation().doubleValue())
                .shareDecaySeconds(fairsharePolicy.shareDecaySeconds().doubleValue())
                .shareDistribution(toModelShareDistribution(fairsharePolicy.shareDistribution()))
                .build();
    }

    static List<software.amazon.batch.schedulingpolicy.ShareAttributes> toModelShareDistribution(
            final List<ShareAttributes> shareAttributesList) {
        return shareAttributesList.stream()
                .map(s -> software.amazon.batch.schedulingpolicy.ShareAttributes
                        .builder()
                        .shareIdentifier(s.shareIdentifier())
                        .weightFactor(s.weightFactor().doubleValue())
                        .build())
                .collect(Collectors.toList());
    }

    static CreateSchedulingPolicyRequest toCreateSchedulingPolicyRequest(final ResourceModel model, final Map<String,
            String> tags) {
        return CreateSchedulingPolicyRequest
                .builder()
                .name(model.getName())
                .fairsharePolicy(model.getFairsharePolicy() != null
                                 ? toRequestFairSharePolicy(model.getFairsharePolicy()) : null)
                .tags(tags)
                .build();
    }

    static DeleteSchedulingPolicyRequest toDeleteSchedulingPolicyRequest(final ResourceModel model) {
        return DeleteSchedulingPolicyRequest
                .builder()
                .arn(model.getArn())
                .build();
    }

    static DescribeSchedulingPoliciesRequest toDescribeSchedulingPoliciesRequest(final List<String> arns) {
        return DescribeSchedulingPoliciesRequest
                .builder()
                .arns(arns)
                .build();
    }

    static ListSchedulingPoliciesRequest toListSchedulingPoliciesRequest(final String nextToken) {
        return ListSchedulingPoliciesRequest
                .builder()
                .nextToken(nextToken)
                .build();
    }
    static UpdateSchedulingPolicyRequest toUpdateSchedulingPolicyRequest(final ResourceModel model) {
        return UpdateSchedulingPolicyRequest
                .builder()
                .arn(model.getArn())
                .fairsharePolicy(model.getFairsharePolicy() != null
                                 ? toRequestFairSharePolicy(model.getFairsharePolicy()) : null)
                .build();
    }

    static UntagResourceRequest toUntagResourceRequest(final ResourceModel model, final List<String> tagsToRemove) {
        return UntagResourceRequest
                .builder()
                .resourceArn(model.getArn())
                .tagKeys(tagsToRemove)
                .build();
    }

    static TagResourceRequest toTagResourceRequest(final ResourceModel model, final Map<String, String> tagsToRemove) {
        return TagResourceRequest
                .builder()
                .resourceArn(model.getArn())
                .tags(tagsToRemove)
                .build();
    }
}
