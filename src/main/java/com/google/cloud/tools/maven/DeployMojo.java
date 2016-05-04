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

/**
 * Stage and deploy an application to Google App Engine standard or flexible environment.
 */
@Mojo(name = "deploy")
@Execute(phase = LifecyclePhase.PACKAGE)
public class DeployMojo extends StageMojo implements DeployConfiguration {

  /**
   * The yaml files for the services or configurations you want to deploy. If not given, defaults to
   * app.yaml in the current directory. If that is not found, attempts to automatically generate
   * necessary configuration files (such as app.yaml) in the current directory.
   */
  @Parameter(name = "deployDeployables", property = "app.deploy.deployables")
  private List<File> deployables;

  /**
   * The Google Cloud Storage bucket used to stage files associated with the deployment. If this
   * argument is not specified, the application's default code bucket is used.
   */
  @Parameter(name = "deployBucket", property = "app.deploy.bucket")
  private String bucket;

  /**
   * Perform a hosted ('remote') or local ('local') Docker build. To perform a local build, you must
   * have your local docker environment configured correctly. The default is a hosted build.
   */
  @Parameter(name = "deployDockerBuild", property = "app.deploy.dockerBuild")
  private String dockerBuild;

  /**
   * Force deploying, overriding any previous in-progress deployments to this version.
   */
  @Parameter(name = "deployForce", property = "app.deploy.force")
  private boolean force;

  /**
   * Deploy with a specific Docker image. Docker url must be from one of the valid gcr hostnames.
   */
  @Parameter(name = "deployImageUrl", property = "app.deploy.imageUrl")
  private String imageUrl;

  /**
   * Promote the deployed version to receive all traffic. True by default.
   */
  @Parameter(name = "deployPromote", property = "app.deploy.promote")
  private boolean promote;

  /**
   * The App Engine server to connect to. You will not typically need to change this value.
   */
  @Parameter(name = "deployServer", property = "app.deploy.server")
  private String server;

  /**
   * Stop the previously running version when deploying a new version that receives all traffic.
   */
  @Parameter(name = "deployStopPreviousVersion", property = "app.deploy.stopPreviousVersion")
  private boolean stopPreviousVersion;

  /**
   * The version of the app that will be created or replaced by this deployment. If you do not
   * specify a version, one will be generated for you.
   */
  @Parameter(name = "deployVersion", property = "app.deploy.version")
  private String version;

  /**
   * The Google Cloud Platform project name to use for this invocation. If omitted then the current
   * project is assumed.
   */
  @Parameter(name = "deployProject", property = "app.deploy.project")
  private String project;

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

  @Override
  public String getProject() {
    return project;
  }
}
