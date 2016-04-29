package com.google.cloud.tools.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Starts running App Engine Development App Server asynchronously.
 */
@Mojo(name = "start")
@Execute(phase = LifecyclePhase.PACKAGE)
public class RunAsyncMojo extends RunMojo {

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    // TODO: do it async
    super.execute();
  }
}
