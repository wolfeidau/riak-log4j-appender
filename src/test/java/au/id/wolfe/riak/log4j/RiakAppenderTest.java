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

import org.junit.Test;

import java.net.URLEncoder;
import java.nio.charset.Charset;

/**
 * Test the Riak Appender.
 */
public class RiakAppenderTest {

    @Test
    public void testLogger() throws Exception {
        String encode = URLEncoder.encode("1997-07-16T19:20:30.45+01:00", Charset.defaultCharset().name());

        System.out.println(encode);


    }
}
