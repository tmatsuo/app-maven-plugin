/**
 * Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.cloud.tools.maven;

import com.google.cloud.tools.Action;
import com.google.cloud.tools.StageGenericJavaAction;
import com.google.cloud.tools.maven.configs.DeployConfig;
import com.google.cloud.tools.maven.configs.StageConfig;
import com.google.cloud.tools.StageAction;
import com.google.common.base.Strings;
import com.google.cloud.tools.DeployAction;
import com.google.cloud.tools.DeployAction.AppType;
import com.google.cloud.tools.InvalidDirectoryException;
import com.google.cloud.tools.InvalidFlagException;
import com.google.cloud.tools.Option;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.FileUtils;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Deploys an app to the server.
 */
@Mojo(name = "deploy")
@Execute(phase = LifecyclePhase.PACKAGE)
public class Deploy extends GcpAppMojo {

  // Maven parameters.
  @Parameter(defaultValue = "${stage}")
  private StageConfig stageConfig;
  @Parameter(defaultValue = "${deploy}")
  private DeployConfig deployConfig;
  @Parameter(property = "gcp.app.deploy.stagingDir",
      defaultValue = "${project.build.directory}/appengine-staging")
  private String stagingDirLocation;
  @Parameter(property = "gcp.app.deploy.sourceDir",
      defaultValue = "${project.build.directory}/${project.build.finalName}")
  private String sourceDirLocation;
  @Parameter(property = "gcp.app.deploy.configDir",
      defaultValue = "${project.build.directory}/src/main/appengine")
  private String configLocation;

  // Required by SdkResolver.
  @Component
  protected RepositorySystem repositorySystem;
  @Parameter(defaultValue = "${repositorySystemSession}")
  protected RepositorySystemSession repositorySession;
  @Parameter(defaultValue = "${project.remoteProjectRepositories}", readonly = true)
  protected List<RemoteRepository> projectRepositories;
  @Parameter(defaultValue = "${project.remotePluginRepositories}", readonly = true)
  protected List<RemoteRepository> pluginRepositories;

  // Class members.
  private File stagingDir;

  public void execute() throws MojoExecutionException {
    getLog().info(String.format("Starting to execute app to project %s.", cloudProject));

    // Applies command line flag overrides specified with -D.
    deployConfig.overrideWithCommandLineFlags();

    try {
      stagingDir = prepareStagingEnvironment();
    } catch (IOException ioEx) {
      getLog().error(ioEx);
      return;
    }

    File appEngineXml = new File(sourceDirLocation + "/WEB-INF/appengine-web.xml");
    Action stageAction;
    if (mavenProject.getPackaging().equals("jar") || !appEngineXml.exists()) {
      File configDir = new File(configLocation);
      File appYaml = new File(configDir, "app.yaml");
      File dockerfile = new File(configDir, "Dockerfile");
      File artifact = new File(sourceDirLocation,
          mavenProject.getBuild().getFinalName() + "." + mavenProject.getPackaging());

      stageAction = new StageGenericJavaAction(appYaml, dockerfile, artifact, stagingDir);
    } else {
      File sdkRoot = SdkResolver.getSdk(mavenProject, repositorySystem, repositorySession,
          pluginRepositories, projectRepositories);
      stageAction = new StageAction(sourceDirLocation, stagingDirLocation,
          sdkRoot.getAbsolutePath(), getStagingFlags());
    }

    try {
      this.action = new DeployAction(stagingDir.getAbsolutePath(), AppType.CLASSIC_APP_ENGINE,
          getFlags(), stageAction);
    } catch (InvalidDirectoryException ide) {
      throw new MojoExecutionException(ide.getMessage(), ide);
    } catch (InvalidFlagException ife) {
      throw new MojoExecutionException(ife.getMessage(), ife);
    }

    this.executeAction();
  }

  /**
   * Prepares the staging directory for the staging phase.
   *
   * <p>If the staging directory already exists, its content is deleted. If there is no staging
   * directory yet, it creates an empty one.
   */
  private File prepareStagingEnvironment() throws IOException, MojoExecutionException {
    File stagingDir = new File(stagingDirLocation);

    if (stagingDir.exists() && stagingDir.isFile()) {
      throw new MojoExecutionException(String.format(
          "Staging location already exists and is a file. Please delete it before deploying. "
          + "Location: %s", stagingDir.getAbsolutePath()));
    }

    if (!stagingDir.exists()) {
      if (!stagingDir.mkdir()) {
        throw new MojoExecutionException(String.format(
            "Staging directory could not be created at %s", stagingDir.getAbsolutePath()));
      }
      getLog().info(String.format("Staging directory created at %s", stagingDir.getAbsolutePath()));
    }

    if (stagingDir.listFiles().length > 0) {
      FileUtils.cleanDirectory(stagingDir);
    }

    return stagingDir;
  }

  private Map<Option, String> getStagingFlags() {
    Map<Option, String> flags = new HashMap<>();

//    if (!Strings.isNullOrEmpty())

    return flags;
  }

  private Map<Option, String> getFlags() {
    Map<Option, String> flags = new HashMap<>();

    if (!Strings.isNullOrEmpty(deployConfig.bucket)) {
      flags.put(Option.BUCKET, deployConfig.bucket);
    }
    if (!Strings.isNullOrEmpty(deployConfig.dockerBuild)) {
      flags.put(Option.DOCKER_BUILD, deployConfig.dockerBuild);
    }
    if (deployConfig.force != null) {
      flags.put(Option.FORCE, deployConfig.force.toString());
    }
    if (!Strings.isNullOrEmpty(deployConfig.imageUrl)) {
      flags.put(Option.IMAGE_URL, deployConfig.imageUrl);
    }
    if (deployConfig.promote != null) {
      flags.put(Option.PROMOTE, deployConfig.promote.toString());
    }
    if (!Strings.isNullOrEmpty(deployConfig.server)) {
      flags.put(Option.SERVER, deployConfig.server);
    }

    return flags;
  }
}
