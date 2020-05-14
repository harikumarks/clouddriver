/*
 * Copyright 2020 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.netflix.spinnaker.clouddriver.kubernetes.op.handler.argo;

import static com.netflix.spinnaker.clouddriver.kubernetes.op.handler.KubernetesHandler.DeployPriority.WORKLOAD_CONTROLLER_PRIORITY;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.netflix.spinnaker.clouddriver.kubernetes.artifact.Replacer;
import com.netflix.spinnaker.clouddriver.kubernetes.caching.agent.KubernetesCacheDataConverter;
import com.netflix.spinnaker.clouddriver.kubernetes.caching.agent.KubernetesCoreCachingAgent;
import com.netflix.spinnaker.clouddriver.kubernetes.caching.agent.KubernetesV2CachingAgentFactory;
import com.netflix.spinnaker.clouddriver.kubernetes.description.SpinnakerKind;
import com.netflix.spinnaker.clouddriver.kubernetes.description.manifest.KubernetesApiVersion;
import com.netflix.spinnaker.clouddriver.kubernetes.description.manifest.KubernetesKind;
import com.netflix.spinnaker.clouddriver.kubernetes.description.manifest.KubernetesManifest;
import com.netflix.spinnaker.clouddriver.kubernetes.model.Manifest.Status;
import com.netflix.spinnaker.clouddriver.kubernetes.op.handler.*;
import com.netflix.spinnaker.kork.annotations.NonnullByDefault;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import org.springframework.stereotype.Component;

@Component
@NonnullByDefault
public class KubernetesArgoRolloutHandler extends KubernetesHandler
    implements CanResize,
        CanScale,
        CanPauseRollout,
        CanResumeRollout,
        CanUndoRollout,
        CanRollingRestart,
        ServerGroupManagerHandler {

  private static final ImmutableSet<KubernetesApiVersion> SUPPORTED_API_VERSIONS =
      ImmutableSet.of(KubernetesApiVersion.fromString("argoproj.io/v1alpha1"));

  @Override
  protected ImmutableList<Replacer> artifactReplacers() {
    return ImmutableList.of(
        Replacer.dockerImage(),
        Replacer.configMapVolume(),
        Replacer.secretVolume(),
        Replacer.configMapEnv(),
        Replacer.secretEnv(),
        Replacer.configMapKeyValue(),
        Replacer.secretKeyValue());
  }

  @Override
  public int deployPriority() {
    return WORKLOAD_CONTROLLER_PRIORITY.getValue();
  }

  @Override
  public KubernetesKind kind() {
    return KubernetesKind.ARGO_ROLLOUT;
  }

  @Override
  public boolean versioned() {
    return false;
  }

  @Override
  public SpinnakerKind spinnakerKind() {
    return SpinnakerKind.SERVER_GROUP_MANAGERS;
  }

  @Override
  public Status status(KubernetesManifest manifest) {
    if (!SUPPORTED_API_VERSIONS.contains(manifest.getApiVersion())) {
      throw new UnsupportedVersionException(manifest);
    }
    ArgoRollout argoRollout = KubernetesCacheDataConverter.getResource(manifest, ArgoRollout.class);
    return status(argoRollout);
  }

  @Override
  protected KubernetesV2CachingAgentFactory cachingAgentFactory() {
    return KubernetesCoreCachingAgent::new;
  }

  private Status status(ArgoRollout argoRollout) {
    ArgoRolloutStatus status = argoRollout.getStatus();
    if (status == null) {
      return Status.noneReported();
    }

    List<ArgoRolloutCondition> conditions =
        Optional.ofNullable(status.getConditions()).orElse(ImmutableList.of());

    Status result = Status.defaultStatus();

    getPausedReason(conditions).ifPresent(result::paused);

    getUnavailableReason(conditions)
        .ifPresent(reason -> result.unstable(reason).unavailable(reason));

    getFailedReason(conditions).ifPresent(result::failed);

    checkReplicaCounts(argoRollout, status)
        .ifPresent(reason -> result.unstable(reason.getMessage()));

    return result;
  }

  private static Optional<String> getUnavailableReason(
      Collection<ArgoRolloutCondition> conditions) {
    return conditions.stream()
        .filter(c -> c.getType().equalsIgnoreCase("available"))
        .filter(c -> c.getStatus().equalsIgnoreCase("false"))
        .map(ArgoRolloutCondition::getMessage)
        .findAny();
  }

  private static Optional<String> getPausedReason(Collection<ArgoRolloutCondition> conditions) {
    return conditions.stream()
        .filter(c -> c.getReason() != null)
        .filter(c -> c.getReason().equalsIgnoreCase("RolloutPaused"))
        .map(ArgoRolloutCondition::getMessage)
        .findAny();
  }

  private static Optional<String> getFailedReason(Collection<ArgoRolloutCondition> conditions) {
    return conditions.stream()
        .filter(c -> c.getType().equalsIgnoreCase("progressing"))
        .filter(c -> c.getReason() != null)
        .filter(c -> c.getReason().equalsIgnoreCase("progressdeadlineexceeded"))
        .map(c -> "Rollout exceeded its progress deadline")
        .findAny();
  }

  // Unboxes an Integer, returning 0 if the input is null
  private static int defaultToZero(@Nullable Integer input) {
    return input == null ? 0 : input;
  }

  private static Optional<UnstableReason> checkReplicaCounts(
      ArgoRollout argoRollout, ArgoRolloutStatus argoRolloutStatus) {
    int desiredReplicas = defaultToZero(argoRollout.getSpec().getReplicas());
    int updatedReplicas = defaultToZero(argoRolloutStatus.getUpdatedReplicas());
    if (updatedReplicas < desiredReplicas) {
      return Optional.of(UnstableReason.UPDATED_REPLICAS);
    }

    // TODO this needs to be checked along with bluegreen promotion as this is true only after
    // promotion
    /*int statusReplicas = defaultToZero(argoRolloutStatus.getReplicas());
    if (statusReplicas > updatedReplicas) {
      return Optional.of(UnstableReason.OLD_REPLICAS);
    }*/

    int availableReplicas = defaultToZero(argoRolloutStatus.getAvailableReplicas());
    if (availableReplicas < desiredReplicas) {
      return Optional.of(UnstableReason.AVAILABLE_REPLICAS);
    }

    // TODO this is not really useful unless we check it after promotion is done. But leaving here
    // as this
    //  needs to be tested for first time deployment anyways
    int readyReplicas = defaultToZero(argoRolloutStatus.getReadyReplicas());
    if (readyReplicas < desiredReplicas) {
      return Optional.of(UnstableReason.READY_REPLICAS);
    }

    // TODO more conditions needs to be added for bluegreen based on the state of blue green

    return Optional.empty();
  }
}
