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

package beans;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import me.tfeng.play.security.oauth2.Authentication;
import me.tfeng.play.security.oauth2.AuthenticationManagerClient;
import me.tfeng.play.security.oauth2.ClientAuthentication;
import me.tfeng.play.security.oauth2.UserAuthentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationManager;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;

import play.libs.Akka;
import play.libs.F.Promise;
import scala.concurrent.ExecutionContext;

/**
 * @author Thomas Feng (huining.feng@gmail.com)
 */
@Component("oauth2-plugin.authentication-manager")
public class AuthenticationManagerImpl implements AuthenticationManagerClient {

  @Autowired
  @Qualifier("oauth2-example.authentication-manager")
  private OAuth2AuthenticationManager authenticationManager;

  @Value("${oauth2-example.execution-context:play.akka.actor.security-context}")
  private String executionContextId;

  @Override
  public Promise<Authentication> authenticate(String token) {
    ExecutionContext executionContext = Akka.system().dispatchers().lookup(executionContextId);
    return Promise.promise(() -> {
      PreAuthenticatedAuthenticationToken authRequest =
          new PreAuthenticatedAuthenticationToken(token.toString(), "");
      OAuth2Authentication authResult =
          (OAuth2Authentication) authenticationManager.authenticate(authRequest);

      Authentication authentication = new Authentication();
      authentication.setClient(getClientAuthentication(authResult.getOAuth2Request()));
      authentication.setUser(getUserAuthentication(authResult.getUserAuthentication()));
      return authentication;
    }, executionContext);
  }

  private ClientAuthentication getClientAuthentication(OAuth2Request request) {
    List<String> authorities = request.getAuthorities().stream()
        .map(authority -> authority.getAuthority()).collect(Collectors.toList());
    List<String> scopes = new ArrayList<>(request.getScope());
    ClientAuthentication client = new ClientAuthentication();
    client.setId(request.getClientId());
    client.setAuthorities(authorities);
    client.setScopes(scopes);
    return client;
  }

  private UserAuthentication getUserAuthentication(
      org.springframework.security.core.Authentication authentication) {
    if (authentication == null) {
      return null;
    } else {
      List<String> authorities = authentication.getAuthorities().stream()
          .map(authority -> authority.getAuthority()).collect(Collectors.toList());
      UserAuthentication user = new UserAuthentication();
      user.setId(authentication.getName());
      user.setAuthorities(authorities);
      return user;
    }
  }
}
