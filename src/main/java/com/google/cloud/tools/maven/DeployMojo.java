package com.google.cloud.tools.maven;

/**
 * Stages and then deploys application to App Engine standard or flexible environment.
 */

import com.google.cloud.tools.app.api.AppEngineException;
import com.google.cloud.tools.app.api.deploy.AppEngineDeployment;
import com.google.cloud.tools.app.api.deploy.DeployConfiguration;
import com.google.cloud.tools.app.impl.cloudsdk.CloudSdkAppEngineDeployment;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.util.List;

@Mojo(name = "deploy")
@Execute(phase = LifecyclePhase.PACKAGE)
public class DeployMojo extends StageMojo implements DeployConfiguration {

  @Parameter(required = true, property = "deploy.deployables")
  private List<File> deployables;
  @Parameter(property = "gcp.app.deploy.bucket")
  private String bucket;
  @Parameter(property = "gcp.app.deploy.dockerBuild")
  private String dockerBuild;
  @Parameter(property = "gcp.app.deploy.force")
  private boolean force;
  @Parameter(property = "gcp.app.deploy.imageUrl")
  private String imageUrl;
  @Parameter(property = "gcp.app.deploy.promote")
  private boolean promote;
  @Parameter(property = "gcp.app.deploy.server")
  private String server;
  @Parameter(property = "gcp.app.deploy.stopPreviousVersion")
  private boolean stopPreviousVersion;
  @Parameter(property = "gcp.app.deploy.version")
  private String version;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    // execute stage
    super.execute();

    if (deployables.size() == 0) {
      deployables.add(
          new File(getStagingDirectory() + "/app.yaml"));
    }

    AppEngineDeployment deployment = new CloudSdkAppEngineDeployment(cloudSdk);

    try {
      deployment.deploy(this);
    } catch (AppEngineException e) {
      throw new MojoExecutionException(e.getMessage());
    }
  }

  @Override
  public List<File> getDeployables() {
    return deployables;
  }

  @Override
  public String getBucket() {
    return bucket;
  }

  @Override
  public String getDockerBuild() {
    return dockerBuild;
  }

  @Override
  public boolean isForce() {
    return force;
  }

  @Override
  public String getImageUrl() {
    return imageUrl;
  }

  @Override
  public boolean isPromote() {
    return promote;
  }

  @Override
  public String getServer() {
    return server;
  }

  @Override
  public boolean isStopPreviousVersion() {
    return stopPreviousVersion;
  }

  @Override
  public String getVersion() {
    return version;
  }
}
