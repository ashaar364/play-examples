/**
 * Copyright 2014 Thomas Feng
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;

import me.tfeng.play.security.oauth2.OAuth2Authentication;
import play.libs.F.Promise;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

/**
 * @author Thomas Feng (huining.feng@gmail.com)
 */
@Service
@OAuth2Authentication
public class Application extends Controller {

  @PreAuthorize("hasRole('ROLE_USER')")
  public Promise<Result> userDetails() {
    String username = SecurityContextHolder.getContext().getAuthentication().getName();
    ObjectNode json = Json.newObject();
    json.setAll(ImmutableMap.of(
        "username", json.textNode(username), "isActive", json.booleanNode(true)));
    return Promise.pure(ok(json));
  }
}
