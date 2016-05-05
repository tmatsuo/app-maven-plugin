package com.google.cloud.tools.maven;

import com.google.cloud.tools.app.api.devserver.AppEngineDevServer;
import com.google.cloud.tools.app.api.devserver.StopConfiguration;
import com.google.cloud.tools.app.impl.cloudsdk.CloudSdkAppEngineDevServer;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Stops a running App Engine Development App Server.
 */
@Mojo(name = "stop")
public class StopMojo extends CloudSdkMojo implements StopConfiguration {

  @Parameter(property = "gcp.app.adminHost")
  private String adminHost;
  @Parameter(property = "gcp.app.adminPort")
  private Integer adminPort;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    super.execute();

    AppEngineDevServer devServer = new CloudSdkAppEngineDevServer(cloudSdk);

    devServer.stop(this);
  }

  @Override
  public String getAdminHost() {
    return adminHost;
  }

  @Override
  public Integer getAdminPort() {
    return adminPort;
  }
}
