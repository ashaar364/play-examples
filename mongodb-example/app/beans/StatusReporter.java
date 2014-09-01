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

import me.tfeng.play.mongodb.OplogItem;
import me.tfeng.play.mongodb.OplogItemHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import play.Logger;
import play.Logger.ALogger;

/**
 * @author Thomas Feng (huining.feng@gmail.com)
 */
@Component
public class StatusReporter implements OplogItemHandler {

  private static final ALogger LOG = Logger.of(StatusReporter.class);

  @Autowired
  private PointsImpl points;

  @Override
  public void handle(OplogItem oplogItem) {
    LOG.info("Storage status: " + points.countPoints() + " points; "
        + String.format("%.3f", points.calculatePointsPerSecond()) + " points/sec");
  }
}
