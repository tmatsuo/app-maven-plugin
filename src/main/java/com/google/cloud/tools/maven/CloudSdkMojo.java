/*
 * Copyright (c) 2016 Google Inc. All Right Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.google.cloud.tools.maven;

import com.google.cloud.tools.app.impl.cloudsdk.internal.process.DefaultProcessRunner;
import com.google.cloud.tools.app.impl.cloudsdk.internal.sdk.CloudSdk;
import com.google.cloud.tools.app.impl.cloudsdk.internal.sdk.PathResolver;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;

/**
 * Abstract Mojo from which all goals inherit.
 */
public abstract class CloudSdkMojo extends AbstractMojo {

  /**
   * Optional parameter to configure the location of the Google Cloud SDK.
   */
  @Parameter(property = "cloudSdkPath", required = false)
  protected File cloudSdkPath;

  protected CloudSdk cloudSdk;

  protected DefaultProcessRunner processRunner;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    // ensure that the Cloud SDK is available
    if (cloudSdkPath == null) {
      try {
        Path sdkPath = PathResolver.INSTANCE.getCloudSdkPath();
        if (sdkPath != null) {
          cloudSdkPath = PathResolver.INSTANCE.getCloudSdkPath().toFile();
        }
      } catch (FileNotFoundException e) {
        throw new MojoFailureException(
            "Cloud SDK Path was not provided, and cannot be automatically detected.", e);
      }
    }
    if (processRunner == null) {
      this.cloudSdk = new CloudSdk(cloudSdkPath);
    } else {
      this.cloudSdk = new CloudSdk(cloudSdkPath, processRunner);
    }
  }
}
