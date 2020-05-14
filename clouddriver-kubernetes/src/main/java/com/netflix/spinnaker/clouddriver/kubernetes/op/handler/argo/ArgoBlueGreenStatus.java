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

public class ArgoBlueGreenStatus {
  public String getPreviewSelector() {
    return previewSelector;
  }

  public void setPreviewSelector(String previewSelector) {
    this.previewSelector = previewSelector;
  }

  public String getActiveSelector() {
    return activeSelector;
  }

  public void setActiveSelector(String activeSelector) {
    this.activeSelector = activeSelector;
  }

  public String getPreviousActiveSelector() {
    return previousActiveSelector;
  }

  public void setPreviousActiveSelector(String previousActiveSelector) {
    this.previousActiveSelector = previousActiveSelector;
  }

  public boolean isScaleUpPreviewCheckPoint() {
    return ScaleUpPreviewCheckPoint;
  }

  public void setScaleUpPreviewCheckPoint(boolean scaleUpPreviewCheckPoint) {
    ScaleUpPreviewCheckPoint = scaleUpPreviewCheckPoint;
  }

  String previewSelector;
  String activeSelector;
  String previousActiveSelector;
  boolean ScaleUpPreviewCheckPoint;
}
