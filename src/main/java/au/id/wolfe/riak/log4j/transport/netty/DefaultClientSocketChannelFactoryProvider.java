package au.id.wolfe.riak.log4j.transport.netty;

import org.jboss.netty.channel.socket.ClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * This returns a netty NIO socket factory using cached thread pools. This singleton is here to avoid
 * unnecessary allocation of thread pools when one is enough.
 *
 */
public class DefaultClientSocketChannelFactoryProvider implements ClientSocketChannelFactoryProvider {

    private static Executor bossExecutor = Executors.newCachedThreadPool();
    private static Executor workerExecutor = Executors.newCachedThreadPool();

    private static ClientSocketChannelFactory factory;

    public ClientSocketChannelFactory getClientSocketChannelFactory() {
        return getFactory();
    }

    private static synchronized ClientSocketChannelFactory getFactory() {
        if (factory == null) {
            factory = new NioClientSocketChannelFactory(bossExecutor, workerExecutor);
        }
        return factory;
    }
}
