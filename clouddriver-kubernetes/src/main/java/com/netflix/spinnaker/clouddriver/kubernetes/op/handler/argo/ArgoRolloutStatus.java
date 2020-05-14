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

import java.util.List;

public class ArgoRolloutStatus {

  int replicas;
  int updatedReplicas;
  int readyReplicas;
  int availableReplicas;
  boolean ControllerPause;
  boolean CurrentPodHash;
  String stableRS;
  String Selector;
  List<ArgoRolloutCondition> conditions;
  ArgoBlueGreenStatus blueGreen;

  public List<ArgoRolloutCondition> getConditions() {
    return conditions;
  }

  public void setConditions(List<ArgoRolloutCondition> conditions) {
    this.conditions = conditions;
  }

  public int getReplicas() {
    return replicas;
  }

  public void setReplicas(int replicas) {
    this.replicas = replicas;
  }

  public int getUpdatedReplicas() {
    return updatedReplicas;
  }

  public void setUpdatedReplicas(int updatedReplicas) {
    this.updatedReplicas = updatedReplicas;
  }

  public int getReadyReplicas() {
    return readyReplicas;
  }

  public void setReadyReplicas(int readyReplicas) {
    this.readyReplicas = readyReplicas;
  }

  public int getAvailableReplicas() {
    return availableReplicas;
  }

  public void setAvailableReplicas(int availableReplicas) {
    this.availableReplicas = availableReplicas;
  }

  public boolean isControllerPause() {
    return ControllerPause;
  }

  public void setControllerPause(boolean controllerPause) {
    ControllerPause = controllerPause;
  }

  public boolean isCurrentPodHash() {
    return CurrentPodHash;
  }

  public void setCurrentPodHash(boolean currentPodHash) {
    CurrentPodHash = currentPodHash;
  }

  public String getStableRS() {
    return stableRS;
  }

  public void setStableRS(String stableRS) {
    this.stableRS = stableRS;
  }

  public String getSelector() {
    return Selector;
  }

  public void setSelector(String selector) {
    Selector = selector;
  }

  public ArgoBlueGreenStatus getBlueGreen() {
    return blueGreen;
  }

  public void setBlueGreen(ArgoBlueGreenStatus blueGreen) {
    this.blueGreen = blueGreen;
  }
}
