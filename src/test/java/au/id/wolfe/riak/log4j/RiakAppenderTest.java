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

package au.id.wolfe.riak.log4j;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.IOException;

/**
 *
 */
public class RiakAppenderTest {

    @Test
    public void testLogger() throws Exception {

        final RiakAppender riakAppender = new RiakAppender();

        riakAppender.setName("bob");
        //riakAppender.setUrl("http://172.16.252.128:80/riak");
        riakAppender.setUrl("http://172.16.252.128:8098/riak");
        riakAppender.setBucket("testing");

        final Logger logger = Logger.getRootLogger();

        logger.setLevel(Level.ERROR);

        logger.addAppender(riakAppender);

        for (int i = 0; i < 1000; i++) {
            logger.error("message " + i, new IOException("Some random IO error"));
        }


    }
}
