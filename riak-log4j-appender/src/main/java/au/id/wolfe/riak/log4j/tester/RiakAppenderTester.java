/**
 *
 * Copyright (C) 2010 markw <mark@wolfe.id.au>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package au.id.wolfe.riak.log4j.tester;

import au.id.wolfe.riak.log4j.RiakAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Test application just to exercise the appender appending to a local riak server.
 */
public class RiakAppenderTester {

    public static void main(String[] args) {

        try {
            new RiakAppenderTester().run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() throws Exception {
        final RiakAppender riakAppender = new RiakAppender();

        riakAppender.setName("bob");
        riakAppender.setUrl("http://192.168.0.39:8098/riak");
        //riakAppender.setUrl("http://172.16.252.128:8098/riak");
        riakAppender.setTraceEnabled(false);
        riakAppender.setBucket("testing");

        final Logger logger = Logger.getRootLogger();

        logger.setLevel(Level.ERROR);

        logger.addAppender(riakAppender);

        System.out.println("Base messages");

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 100000; i++) {
            //Thread.sleep(1000);
            logger.error("message " + i);
        }

        System.out.println("Base message with Exceptions");

        for (int i = 0; i < 10; i++) {
            logger.error("message " + i,
                    new IOException("Some random IO error", new FileNotFoundException("Not found bro.")));
        }

        long endTime = System.currentTimeMillis();

        System.out.println("Took " + (endTime - startTime));

        //logger.removeAppender(riakAppender);

        riakAppender.close();

    }
}
