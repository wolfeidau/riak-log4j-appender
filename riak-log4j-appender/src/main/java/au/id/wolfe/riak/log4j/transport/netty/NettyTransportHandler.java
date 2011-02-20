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

import au.id.wolfe.riak.log4j.transport.RiakTransportException;
import au.id.wolfe.riak.log4j.utils.Tracer;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipelineFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URL;

/**
 *
 */
public class NettyTransportHandler {

    private URL url;
    private ChannelFuture connectFuture;
    private Channel channel;

    private boolean inCache;
    private boolean keepingAlive = true;
    //private int keepAliveConnections = NettyTransportHandlerFactory.DEFAULT_KEEP_ALIVE_CON_COUNT;
    private NettyTransportHandlerFactory nettyTransportHandlerFactory;

    NettyTransportHandler(URL url, ChannelPipelineFactory channelPipelineFactory, ChannelFactory channelFactory) {
        this.url = url;

        ClientBootstrap bootstrap = new ClientBootstrap(channelFactory);

        bootstrap.setPipelineFactory(channelPipelineFactory);
        bootstrap.setOption("tcpNoDelay", true);

        connectFuture = bootstrap.connect(getSocketAddress(url));
    }

    public Channel getChannel(long timeout) throws RiakTransportException {
        if (channel == null && connectFuture != null) {

            if (!connectFuture.awaitUninterruptibly(timeout)) {
                //times up
                throw new RiakTransportException("Couldn't connect to " + url.getHost(), connectFuture.getCause());
            }
            channel = connectFuture.getChannel();

        }
        return channel;
    }

    /**
     * End the transport handler, closing the underlying connection.
     */
    public void end() {

        System.out.println("Ending connection to " + url.toString());

        keepingAlive = false;
        if (channel == null) {
            channel = connectFuture.getChannel();
            connectFuture.cancel();
        }

        if (channel != null) {
            channel.close();
        }
    }

    public void finished() {

        //keepAliveConnections--;

        //if (keepAliveConnections > 0 && keepingAlive) {
        if (keepingAlive) {
            nettyTransportHandlerFactory.putInCache(this);
        } else {
            end();
        }
    }

    private SocketAddress getSocketAddress(URL inputUrl) {

        int port = inputUrl.getPort();

        if (port < 0) {
            String protocol = inputUrl.getProtocol();

            if ("http".equalsIgnoreCase(protocol)) {
                port = 80;
            } else if ("https".equalsIgnoreCase(protocol)) {
                port = 443;
            }
        }

        return new InetSocketAddress(inputUrl.getHost(), port);
    }

    public URL getUrl() {
        return url;
    }

    public boolean isInCache() {
        return inCache;
    }

    public void setInCache(boolean inCache) {
        this.inCache = inCache;
    }

    public void setNettyTransportHandlerFactory(NettyTransportHandlerFactory nettyTransportHandlerFactory) {
        this.nettyTransportHandlerFactory = nettyTransportHandlerFactory;
    }
}
