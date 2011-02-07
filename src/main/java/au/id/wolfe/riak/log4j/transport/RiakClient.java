package au.id.wolfe.riak.log4j.transport;

import java.net.URI;
import java.util.Map;

/**
 * Basic methods for interacting with riak http
 */
public interface RiakClient {

    void store(String hostUri, String bucket, String message) throws RiakTransportException;

    void close();

}
