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
