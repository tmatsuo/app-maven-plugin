package com.google.cloud.tools.maven;

import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import java.util.concurrent.CountDownLatch;

/**
 * Starts running App Engine Development App Server asynchronously.
 */
@Mojo(name = "start")
@Execute(phase = LifecyclePhase.PACKAGE)
public class RunAsyncMojo extends RunMojo {

  private CountDownLatch waitStartedLatch;

  /**
   * Configures an asynchronous process runner for running server.
   */
  public RunAsyncMojo() {
    super();
    processRunner.setAsync(true);
  }

}
