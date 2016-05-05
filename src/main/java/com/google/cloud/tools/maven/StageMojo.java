package com.google.cloud.tools.maven;

import com.google.cloud.tools.app.api.deploy.AppEngineFlexibleStaging;
import com.google.cloud.tools.app.api.deploy.StageFlexibleConfiguration;
import com.google.cloud.tools.app.api.deploy.StageStandardConfiguration;
import com.google.cloud.tools.app.impl.cloudsdk.CloudSdkAppEngineFlexibleStaging;
import com.google.cloud.tools.app.impl.cloudsdk.CloudSdkAppEngineStandardStaging;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;

/**
 * Stages application for App Engine standard or flexible environment deployment.
 */
@Mojo(name = "stage")
@Execute(phase = LifecyclePhase.PACKAGE)
public class StageMojo extends CloudSdkMojo implements StageStandardConfiguration,
    StageFlexibleConfiguration {

  // Standard & Flexible params
  @Parameter(required = true, defaultValue = "${project.build.directory}/appengine-staging",
      alias = "stage.stagingDirectory", property = "app.stage.stagingDirectory")
  private File stagingDirectory;
  @Parameter(property = "app.stage.dockerfile")
  private File dockerfile;

  // Standard-only params
  @Parameter(required = true,
      defaultValue = "${project.build.directory}/${project.build.finalName}",
      property = "app.stage.sourceDirectory")
  private File sourceDirectory;
  @Parameter(property = "app.stage.enable-quickstart")
  private boolean enableQuickstart;
  @Parameter(property = "app.stage.disable-update-check")
  private boolean disableUpdateCheck;
  @Parameter(property = "app.stage.enable-jar-splitting")
  private boolean enableJarSplitting;
  @Parameter(property = "app.stage.jar-splitting-excludes")
  private String jarSplittingExcludes;
  @Parameter(property = "app.stage.compile-encoding-key")
  private String compileEncoding;
  @Parameter(property = "app.stage.delete-jsps")
  private boolean deleteJsps;
  @Parameter(property = "app.stage.enable-jar-classes")
  private boolean enableJarClasses;

  // Flexible-only params
  @Parameter(defaultValue = "${basedir}/src/main/appengine/app.yaml")
  private File appYaml;
  @Parameter(defaultValue =
      "${project.build.directory}/${project.build.finalName}.${project.packaging}")
  private File artifact;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    super.execute();

    // delete staging directory if it exists
    if (stagingDirectory.exists()) {
      getLog().info("Deleting the staging directory: " + stagingDirectory);
      try {
        FileUtils.deleteDirectory(stagingDirectory);
      } catch (IOException e) {
        new MojoFailureException("Unable to delete staging directory.", e);
      }
    }
    stagingDirectory.mkdir();

    getLog().info("Staging the application to: " + stagingDirectory);

    if (new File(sourceDirectory.toString() + "/WEB-INF/appengine-web.xml").exists()) {
      getLog().info("Detected App Engine standard environment application.");
      stageStandard();
    } else {
      getLog().info("Detected App Engine flexible environment application.");
      stageFlexible();
    }
  }

  private void stageStandard()  {
    CloudSdkAppEngineStandardStaging staging = new CloudSdkAppEngineStandardStaging(cloudSdk);

    // execute the staging
    staging.stageStandard(this);
  }

  private void stageFlexible() {
    getLog().info(artifact.toString());
    AppEngineFlexibleStaging staging = new CloudSdkAppEngineFlexibleStaging();
    staging.stageFlexible(this);
  }

  @Override
  public File getSourceDirectory() {
    return sourceDirectory;
  }

  @Override
  public File getStagingDirectory() {
    return stagingDirectory;
  }

  @Override
  public File getDockerfile() {
    return dockerfile;
  }

  @Override
  public boolean isEnableQuickstart() {
    return enableQuickstart;
  }

  @Override
  public boolean isDisableUpdateCheck() {
    return disableUpdateCheck;
  }

  @Override
  public boolean isEnableJarSplitting() {
    return enableJarSplitting;
  }

  @Override
  public String getJarSplittingExcludes() {
    return jarSplittingExcludes;
  }

  @Override
  public String getCompileEncoding() {
    return compileEncoding;
  }

  @Override
  public boolean isDeleteJsps() {
    return deleteJsps;
  }

  @Override
  public boolean isEnableJarClasses() {
    return enableJarClasses;
  }

  @Override
  public File getAppYaml() {
    return appYaml;
  }

  @Override
  public File getArtifact() {
    return artifact;
  }
}
