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

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;

import java.math.BigInteger;
import java.util.List;

import me.tfeng.play.avro.AvroHelper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import controllers.protocols.Point;
import controllers.protocols.Points;

/**
 * @author Thomas Feng (huining.feng@gmail.com)
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath*:spring/*.xml"})
public class IntegrationTest {

  private static final int TIMEOUT = 10000;

  @Test
  public void testOneNearestPoint() {
    running(testServer(3333), () -> {
      try {
        WSResponse response;
        List<Point> nearestPoints;

        response = WS.url("http://localhost:3333/points/addPoint")
            .setContentType("avro/json")
            .post("{\"point\": {\"x\": 1.0, \"y\": 2.0}}")
            .get(TIMEOUT);
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("null");

        response = WS.url("http://localhost:3333/points/addPoint")
            .setContentType("avro/json")
            .post("{\"point\": {\"x\": 3.0, \"y\": 0.5}}")
            .get(TIMEOUT);
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("null");

        response = WS.url("http://localhost:3333/points/getNearestPoints")
            .setContentType("avro/json")
            .post("{\"from\": {\"x\": 0.0, \"y\": 0.0}, \"k\": 1}")
            .get(TIMEOUT);
        assertThat(response.getStatus()).isEqualTo(200);
        nearestPoints =
            AvroHelper.toRecord(Points.PROTOCOL.getMessages().get("getNearestPoints").getResponse(),
                response.getBody());
        assertThat(nearestPoints.size()).isEqualTo(1);
        assertThat(nearestPoints.get(0).getX()).isEqualTo(1.0);
        assertThat(nearestPoints.get(0).getY()).isEqualTo(2.0);
        new BigInteger(nearestPoints.get(0).getId(), 16); // Id is generated by MongoDB.
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });
  }

  @Test
  public void testThreeNearestPoints() {
    running(testServer(3333), () -> {
      try {
        WSResponse response;
        List<Point> nearestPoints;

        response = WS.url("http://localhost:3333/points/addPoint")
            .setContentType("avro/json")
            .post("{\"point\": {\"x\": 1.0, \"y\": 2.0}}")
            .get(TIMEOUT);
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("null");

        response = WS.url("http://localhost:3333/points/addPoint")
            .setContentType("avro/json")
            .post("{\"point\": {\"x\": 3.0, \"y\": 0.5}}")
            .get(TIMEOUT);
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("null");

        response = WS.url("http://localhost:3333/points/addPoint")
            .setContentType("avro/json")
            .post("{\"point\": {\"id\": {\"string\": \"53fdf8429436cecb91d26cfa\"}, \"x\": 2.1, \"y\": 1.8}}")
            .get(TIMEOUT);
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("null");

        response = WS.url("http://localhost:3333/points/getNearestPoints")
            .setContentType("avro/json")
            .post("{\"from\": {\"x\": 0.0, \"y\": 0.0}, \"k\": 3}")
            .get(TIMEOUT);
        assertThat(response.getStatus()).isEqualTo(200);
        nearestPoints =
            AvroHelper.toRecord(Points.PROTOCOL.getMessages().get("getNearestPoints").getResponse(),
                response.getBody());
        assertThat(nearestPoints.size()).isEqualTo(3);

        assertThat(nearestPoints.get(0).getX()).isEqualTo(1.0);
        assertThat(nearestPoints.get(0).getY()).isEqualTo(2.0);
        new BigInteger(nearestPoints.get(0).getId(), 16);

        assertThat(nearestPoints.get(1).getX()).isEqualTo(2.1);
        assertThat(nearestPoints.get(1).getY()).isEqualTo(1.8);
        assertThat(nearestPoints.get(1).getId()).isEqualTo("53fdf8429436cecb91d26cfa");

        assertThat(nearestPoints.get(2).getX()).isEqualTo(3.0);
        assertThat(nearestPoints.get(2).getY()).isEqualTo(0.5);
        new BigInteger(nearestPoints.get(2).getId(), 16);

        assertThat(new BigInteger(nearestPoints.get(0).getId(), 16))
            .isNotEqualTo(new BigInteger(nearestPoints.get(2).getId(), 16));
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });
  }
}