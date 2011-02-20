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

import au.id.wolfe.riak.log4j.transport.RiakClient;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Test the Riak Appender.
 */
@RunWith(MockitoJUnitRunner.class)
public class RiakAppenderTest {

    private static final String TEST_NAME = "bob";
    private static final String TEST_URL = "http://192.168.0.39:8098/riak";
    private static final String TEST_BUCKET = "testing";

    @Mock(name = "riakClient")
    RiakClient riakClient;

    @Captor
    ArgumentCaptor<String> argumentCaptor;

    RiakAppender riakAppender;

    @Before
    public void setup() throws Exception {

        riakAppender = new RiakAppender(riakClient);
        riakAppender.setName(TEST_NAME);
        riakAppender.setUrl(TEST_URL);
        riakAppender.setTraceEnabled(false);
        riakAppender.setBucket(TEST_BUCKET);

    }

    @Test
    public void testLogger() throws Exception {
        Logger logger = Logger.getRootLogger();

        logger.setLevel(Level.ALL);
        logger.addAppender(riakAppender);

        logger.error("message from the logger test");

        verify(riakClient).store(eq(TEST_URL), eq(TEST_BUCKET), anyString(), argumentCaptor.capture());

        assertEquals(1, argumentCaptor.getAllValues().size());

        String message = argumentCaptor.getAllValues().get(0);

        JSONObject.testValidity(message);

    }
}
