package com.google.cloud.tools.maven;

import com.google.cloud.tools.app.impl.cloudsdk.internal.process.DefaultProcessRunner;
import com.google.cloud.tools.app.impl.cloudsdk.internal.process.ProcessRunner;
import com.google.cloud.tools.app.impl.cloudsdk.internal.sdk.CloudSdk;
import com.google.cloud.tools.app.impl.cloudsdk.internal.sdk.PathResolver;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Abstract Mojo from which all goals inherit.
 */
public abstract class CloudSdkMojo extends AbstractMojo {

  @Parameter(required = true)
  protected File cloudSdkPath;

  protected CloudSdk cloudSdk;

  protected DefaultProcessRunner processRunner;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    // ensure that the Cloud SDK is available
    if (cloudSdkPath == null) {
      try {
        cloudSdkPath = PathResolver.INSTANCE.getCloudSdkPath().toFile();
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
