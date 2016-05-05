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
 * Generates a deploy-ready application directory for App Engine standard or flexible environment
 * deployment.
 */
@Mojo(name = "stage")
@Execute(phase = LifecyclePhase.PACKAGE)
public class StageMojo extends CloudSdkMojo implements StageStandardConfiguration,
    StageFlexibleConfiguration {

  ///////////////////////////////////
  // Standard & Flexible params
  //////////////////////////////////

  /**
   * The directory to which to stage the application.
   */
  @Parameter(required = true, defaultValue = "${project.build.directory}/appengine-staging",
      alias = "stage.stagingDirectory", property = "app.stage.stagingDirectory")
  private File stagingDirectory;

  /**
   * The location of the dockerfile to use for App Engine flexible environment. This also applies to
   * App Engine Standard applications running on the flexible environment.
   */
  @Parameter(alias = "stage.dockerfile", property = "app.stage.dockerfile")
  private File dockerfile;

  ///////////////////////////////////
  // Standard-only params
  ///////////////////////////////////

  /**
   * The location of the compiled web application files, or the exploded WAR. This will be used as
   * the source for staging.
   *
   * <p>Applies to App Engine standard environment only.
   */
  @Parameter(required = true,
      defaultValue = "${project.build.directory}/${project.build.finalName}",
      alias = "stage.sourceDirectory", property = "app.stage.sourceDirectory")
  private File sourceDirectory;


  /**
   * Use jetty quickstart to process servlet annotations.
   *
   * <p>Applies to App Engine standard environment only.
   */
  @Parameter(alias = "stage.enableQuickstart", property = "app.stage.enableQuickstart")
  private boolean enableQuickstart;

  /**
   * Split large jar files (> 10M) into smaller fragments.
   *
   * <p>Applies to App Engine standard environment only.
   */
  @Parameter(alias = "stage.enableJarSplitting", property = "app.stage.enableJarSplitting")
  private boolean enableJarSplitting;

  /**
   * Files that match the list of comma separated SUFFIXES will be excluded from all jars.
   *
   * <p>Applies to App Engine standard environment only.
   */
  @Parameter(alias = "stage.jarSplittingExcludes", property = "app.stage.jarSplittingExcludes")
  private String jarSplittingExcludes;

  /**
   * The character encoding to use when compiling JSPs.
   *
   * <p>Applies to App Engine standard environment only.
   */
  @Parameter(alias = "stage.compileEncoding", property = "app.stage.compileEncoding")
  private String compileEncoding;

  /**
   * Delete the JSP source files after compilation.
   *
   * <p>Applies to App Engine standard environment only.
   */
  @Parameter(alias = "stage.deleteJsps", property = "app.stage.deleteJsps")
  private boolean deleteJsps;

  /**
   * Jar the WEB-INF/classes content.
   *
   * <p>Applies to App Engine standard environment only.
   */
  @Parameter(alias = "stage.enableJarClasses", property = "app.stage.enableJarClasses")
  private boolean enableJarClasses;

  // always disable update check and do not expose this as a parameter
  private boolean disableUpdateCheck = true;

  ///////////////////////////////////
  // Flexible-only params
  ///////////////////////////////////

  /**
   * The location of the app.yaml in the source directory.
   *
   * <p>Applies to App Engine flexible environment only.
   */
  @Parameter(defaultValue = "${basedir}/src/main/appengine/app.yaml",
      alias = "stage.appYaml", property = "app.stage.appYaml")
  private File appYaml;

  /**
   * The location of the JAR or WAR archive to deploy.
   *
   * <p>Applies to App Engine flexible environment only.
   */
  @Parameter(defaultValue =
      "${project.build.directory}/${project.build.finalName}.${project.packaging}",
      alias = "stage.artifact", property = "app.stage.artifact")
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

  private void stageStandard() {
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
