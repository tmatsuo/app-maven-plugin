package com.google.cloud.tools.maven;


import com.google.cloud.tools.app.api.devserver.AppEngineDevServer;
import com.google.cloud.tools.app.api.devserver.RunConfiguration;
import com.google.cloud.tools.app.impl.cloudsdk.CloudSdkAppEngineDevServer;
import com.google.cloud.tools.app.impl.cloudsdk.internal.process.DefaultProcessRunner;
import com.google.cloud.tools.app.impl.cloudsdk.internal.process.ProcessOutputLineListener;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Run App Engine Development App Server synchronously.
 */
@Mojo(name = "run")
@Execute(phase = LifecyclePhase.PACKAGE)
public class RunMojo extends CloudSdkMojo implements RunConfiguration {

  // TODO: the name doesn't fit when you pass a build dir
  @Parameter(property = "gcp.app.yaml",
      defaultValue = "${project.build.directory}/${project.build.finalName}",
      required = true)
  private List<File> appYamls;
  @Parameter(property = "gcp.app.host")
  private String host;
  @Parameter(property = "gcp.app.port")
  private Integer port;
  @Parameter(property = "gcp.app.adminHost")
  private String adminHost;
  @Parameter(property = "gcp.app.adminPort")
  private Integer adminPort;
  @Parameter(property = "gcp.app.authDomain")
  private String authDomain;
  @Parameter(property = "gcp.app.storagePath")
  private String storagePath;
  @Parameter(property = "gcp.app.logLevel")
  private String logLevel;
  @Parameter(property = "gcp.app.maxModuleInstances")
  private Integer maxModuleInstances;
  @Parameter(property = "gcp.app.useMtimeFileWatcher")
  private boolean useMtimeFileWatcher;
  @Parameter(property = "gcp.app.threadsafeOverride")
  private String threadsafeOverride;
  @Parameter(property = "gcp.app.pythonStartupScript")
  private String pythonStartupScript;
  @Parameter(property = "gcp.app.pythonStartupArgs")
  private String pythonStartupArgs;
  @Parameter(property = "gcp.app.jvmFlag")
  private List<String> jvmFlags;
  @Parameter(property = "gcp.app.customEntrypoint")
  private String customEntrypoint;
  @Parameter(property = "gcp.app.runtime")
  private String runtime;
  @Parameter(property = "gcp.app.allowSkippedFiles")
  private boolean allowSkippedFiles;
  @Parameter(property = "gcp.app.apiPort")
  private Integer apiPort;
  @Parameter(property = "gcp.app.automaticRestart")
  private boolean automaticRestart;
  @Parameter(property = "gcp.app.devAppserverLogLevel")
  private String devAppserverLogLevel;
  @Parameter(property = "gcp.app.skipSdkUpdateCheck")
  private boolean skipSdkUpdateCheck;
  @Parameter(property = "gcp.app.defaultGcsBucketName")
  protected String defaultGcsBucketName;

  private CountDownLatch waitStartedLatch;

  /**
   * Sets up a process runner for synchronous execution and creates a listener for waiting for Dev
   * App Server to start.
   */
  public RunMojo() {
    super();

    processRunner = new DefaultProcessRunner(
        new ProcessBuilder().redirectErrorStream(true));
    processRunner.setAsync(false);

    ProcessOutputLineListener listener = new ProcessOutputLineListener() {
      @Override
      public void outputLine(String line) {
        if (line.contains("Dev App Server is now running")) {
          getLog().info("Dev App Server started.");
          waitStartedLatch.countDown();
        }
        getLog().info(line);
      }
    };
    processRunner.setStdOutLineListener(listener);
  }

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    super.execute();

    waitStartedLatch = new CountDownLatch(1);

    AppEngineDevServer devServer = new CloudSdkAppEngineDevServer(cloudSdk);

    try {
      devServer.run(this);
      if (!waitStartedLatch.await(10, TimeUnit.SECONDS)) {
        getLog().error("Timed out waiting to start App Engine Development server");
      }
    } catch (InterruptedException e) {
      throw new MojoExecutionException("Failed to start the App Engine Development server.", e);
    } finally {
      waitStartedLatch.countDown();
    }

  }

  @Override
  public List<File> getAppYamls() {
    return appYamls;
  }

  @Override
  public String getHost() {
    return host;
  }

  @Override
  public Integer getPort() {
    return port;
  }

  @Override
  public String getAdminHost() {
    return adminHost;
  }

  @Override
  public Integer getAdminPort() {
    return adminPort;
  }

  @Override
  public String getAuthDomain() {
    return authDomain;
  }

  @Override
  public String getStoragePath() {
    return storagePath;
  }

  @Override
  public String getLogLevel() {
    return logLevel;
  }

  @Override
  public Integer getMaxModuleInstances() {
    return maxModuleInstances;
  }

  @Override
  public boolean isUseMtimeFileWatcher() {
    return useMtimeFileWatcher;
  }

  @Override
  public String getThreadsafeOverride() {
    return threadsafeOverride;
  }

  @Override
  public String getPythonStartupScript() {
    return pythonStartupScript;
  }

  @Override
  public String getPythonStartupArgs() {
    return pythonStartupArgs;
  }

  @Override
  public List<String> getJvmFlags() {
    return jvmFlags;
  }

  @Override
  public String getCustomEntrypoint() {
    return customEntrypoint;
  }

  @Override
  public String getRuntime() {
    return runtime;
  }

  @Override
  public boolean isAllowSkippedFiles() {
    return allowSkippedFiles;
  }

  @Override
  public Integer getApiPort() {
    return apiPort;
  }

  @Override
  public boolean isAutomaticRestart() {
    return automaticRestart;
  }

  @Override
  public String getDevAppserverLogLevel() {
    return devAppserverLogLevel;
  }

  @Override
  public boolean isSkipSdkUpdateCheck() {
    return skipSdkUpdateCheck;
  }

  @Override
  public String getDefaultGcsBucketName() {
    return defaultGcsBucketName;
  }
}
