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

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import controllers.protocols.UserMessage;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import me.tfeng.play.avro.AvroHelper;
import me.tfeng.play.plugins.KafkaPlugin;
import me.tfeng.play.spring.Startable;
import play.Logger;
import play.Logger.ALogger;
import utils.Constants;

/**
 * @author Thomas Feng (huining.feng@gmail.com)
 */
@Component("consumer")
@DependsOn("system")
public class ConsumerStartable implements Startable {

  private static class ConsumerRunnable implements Runnable {

    private static final ALogger LOG = Logger.of(ConsumerRunnable.class);

    private final KafkaStream<byte[], byte[]> stream;

    public ConsumerRunnable(KafkaStream<byte[], byte[]> stream) {
      this.stream = stream;
    }

    @Override
    public void run() {
      ConsumerIterator<byte[], byte[]> iterator = stream.iterator();
      while (iterator.hasNext()) {
        try {
          UserMessage message = AvroHelper.decodeRecord(UserMessage.class, iterator.next().message());
          LOG.info("Consuming message: " + message);
        } catch (Throwable t) {
          LOG.error("Unable to consume message", t);
        }
      }
    }
  }

  private ConsumerConnector consumer;

  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

  @Override
  public void onStart() throws Throwable {
    consumer = KafkaPlugin.getInstance().createConsumerConnector();
    List<KafkaStream<byte[], byte[]>> streams =
        consumer.createMessageStreams(Collections.singletonMap(Constants.TOPIC, 1))
            .get(Constants.TOPIC);
    for (KafkaStream<byte[], byte[]> stream : streams) {
      scheduler.execute(new ConsumerRunnable(stream));
    }
  }

  @Override
  public void onStop() throws Throwable {
    consumer.shutdown();
    scheduler.shutdown();
  }
}
