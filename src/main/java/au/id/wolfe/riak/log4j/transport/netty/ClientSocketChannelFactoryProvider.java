package au.id.wolfe.riak.log4j.transport.netty;

import org.jboss.netty.channel.socket.ClientSocketChannelFactory;

/**
 *
 * Client socket provider.
 *
 */
public interface ClientSocketChannelFactoryProvider {

    ClientSocketChannelFactory getClientSocketChannelFactory();
}
