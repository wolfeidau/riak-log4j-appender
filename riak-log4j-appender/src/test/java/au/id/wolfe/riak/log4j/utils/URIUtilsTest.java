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

package au.id.wolfe.riak.log4j.utils;

import org.junit.Test;

import java.net.URI;

import static junit.framework.Assert.assertEquals;

/**
 * Tests the URI Utils class.
 */
public class URIUtilsTest {

    @Test
    public void testAppendToURIPath() throws Exception {

        URI simpleBaseURI;

        simpleBaseURI = URI.create("http://www.example.com?bob=true");

        assertEquals("http://www.example.com/testing?bob=true",
                URIUtils.appendToURIPath(simpleBaseURI, "testing").toASCIIString());

        simpleBaseURI = URI.create("http://www.example.com/some?bob=true");

        assertEquals("http://www.example.com/some/testing?bob=true",
                URIUtils.appendToURIPath(simpleBaseURI, "testing").toASCIIString());

        simpleBaseURI = URI.create("http://www.example.com/some?bob=true");

        assertEquals("http://www.example.com/some/testing/fred?bob=true",
                URIUtils.appendToURIPath(simpleBaseURI, "testing", "fred").toASCIIString());
    }
}
