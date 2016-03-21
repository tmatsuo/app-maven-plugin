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
package com.google.cloud.tools.maven.module;

import com.google.appengine.repackaged.com.google.api.client.util.Strings;
import com.google.cloud.tools.InvalidFlagException;
import com.google.cloud.tools.Option;
import com.google.cloud.tools.module.ListAction;
import com.google.cloud.tools.maven.GcpAppMojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;

import java.util.HashMap;
import java.util.Map;

/**
 * Lists versions for every module, or for a selected set of modules.
 */
@Mojo(name = "module-list")
public class List extends GcpAppMojo {

  public void execute() throws MojoExecutionException {

    Map<Option, String> flags = new HashMap<>();

    if (!Strings.isNullOrEmpty(server)) {
      flags.put(Option.SERVER, server);
    }

    // TODO(joaomartins): On no module provided by the user, this will be set to "default", whereas
    // we want to list every module.
    action = new ListAction(modules, flags);

    this.executeAction();
  }
}
