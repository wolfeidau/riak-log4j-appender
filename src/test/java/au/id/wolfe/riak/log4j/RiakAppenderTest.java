package au.id.wolfe.riak.log4j;

import com.basho.riak.client.RiakClient;
import com.basho.riak.client.RiakObject;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;

/**
 *
 */
public class RiakAppenderTest {

    @Test
    public void testRiak() throws Exception {
        RiakClient riakClient = new RiakClient("http://192.168.0.39:8098/riak");

        riakClient.store(new RiakObject("test", Long.toString(new Date().getTime()), "test"));
    }

    @Test
    public void testLogger() throws Exception {

        final RiakAppender riakAppender = new RiakAppender();
        riakAppender.setName("bob");
        riakAppender.setUrl("http://192.168.0.39:8098/riak");
        riakAppender.setBucket("testing");

        final Logger logger = Logger.getRootLogger();

        logger.setLevel(Level.INFO);

        logger.addAppender(riakAppender);

        for (int i = 0; i < 10000; i++) {
            logger.info("message " + i, new IOException("Some random IO error"));
        }


    }
}
