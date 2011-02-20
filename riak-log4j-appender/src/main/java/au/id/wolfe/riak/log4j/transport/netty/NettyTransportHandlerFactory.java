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

package au.id.wolfe.riak.log4j.transport.netty;

import org.jboss.netty.channel.ChannelPipelineFactory;

import java.net.URL;

/**
 * Factory which uses the keep alive cache to manage Transport Handler instances.
 */
public class NettyTransportHandlerFactory {

    /* client socket provider */
    private ClientSocketChannelFactoryProvider factoryProvider;

    /* keep alive cache, this is static as we want to pool for all appenders */
    private static NettyKeepAliveCache keepAliveCache = new NettyKeepAliveCache();

    public NettyTransportHandlerFactory(ClientSocketChannelFactoryProvider factoryProvider) {
        this.factoryProvider = factoryProvider;
    }

    public NettyTransportHandler getNettyTransportHandler(URL url, ChannelPipelineFactory pipelineFactory) {

        NettyTransportHandler nettyTransportHandler;

        nettyTransportHandler = keepAliveCache.get(url);

        if (nettyTransportHandler == null) {
            nettyTransportHandler =
                    new NettyTransportHandler(url, pipelineFactory, factoryProvider.getClientSocketChannelFactory());
        }

        nettyTransportHandler.setNettyTransportHandlerFactory(this);
        nettyTransportHandler.setInCache(false);

        return nettyTransportHandler;
    }

    public void putInCache(NettyTransportHandler nettyTransportHandler) {
        keepAliveCache.put(nettyTransportHandler.getUrl(), nettyTransportHandler);
    }

    public void shutdown() {

        keepAliveCache.shutdown();

        factoryProvider.getClientSocketChannelFactory().releaseExternalResources();

    }
}
