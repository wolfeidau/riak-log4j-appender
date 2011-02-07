package au.id.wolfe.riak.log4j.transport;

/**
 *
 */
public class RiakTransportException extends Exception{

    public RiakTransportException()  {
        super();
    }

    public RiakTransportException(String message) {
        super(message);
    }

    public RiakTransportException(String message, Throwable cause) {
        super(message, cause);
    }

}
