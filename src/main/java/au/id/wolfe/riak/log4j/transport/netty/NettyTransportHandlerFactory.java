package au.id.wolfe.riak.log4j.transport.netty;

import org.jboss.netty.channel.ChannelPipelineFactory;

import java.net.URL;

/**
 * Factory which uses the keep alive cache to manage Transport Handler instances.
 */
public class NettyTransportHandlerFactory {

    /* number of connections to keep alive */
    public static final int DEFAULT_KEEP_ALIVE_CON_COUNT = 5;

    /* client socket provider */
    private ClientSocketChannelFactoryProvider factoryProvider;

    // keep alive cache
    private static NettyKeepAliveCache keepAliveCache = new NettyKeepAliveCache();

    public NettyTransportHandlerFactory(ClientSocketChannelFactoryProvider factoryProvider) {
        this.factoryProvider = factoryProvider;
    }

    public NettyTransportHandler getNettyTransportHandler(URL url, ChannelPipelineFactory pipelineFactory){

        NettyTransportHandler nettyTransportHandler;

        nettyTransportHandler = keepAliveCache.get(url);

        if (nettyTransportHandler == null){
            nettyTransportHandler = new NettyTransportHandler(url, pipelineFactory, factoryProvider.getClientSocketChannelFactory());
        }

        nettyTransportHandler.setNettyTransportHandlerFactory(this);
        nettyTransportHandler.setInCache(false);

        return nettyTransportHandler;
    }

    public void putInCache(NettyTransportHandler nettyTransportHandler) {
        keepAliveCache.put(nettyTransportHandler.getUrl(), nettyTransportHandler);
    }

    public void shutdown(){

        keepAliveCache.shutdown();

        factoryProvider.getClientSocketChannelFactory().releaseExternalResources();

    }
}
