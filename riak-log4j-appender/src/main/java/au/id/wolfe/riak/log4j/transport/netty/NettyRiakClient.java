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

import au.id.wolfe.riak.log4j.transport.RiakClient;
import au.id.wolfe.riak.log4j.transport.RiakTransportException;
import au.id.wolfe.riak.log4j.transport.netty.RiakResponseHandler.Result;
import au.id.wolfe.riak.log4j.utils.URIUtils;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static au.id.wolfe.riak.log4j.utils.Tracer.log;

/**
 * Implementation of a basic riak client using Netty.
 */
public class NettyRiakClient implements RiakClient {

    ClientSocketChannelFactoryProvider clientSocketChannelFactoryProvider;
    NettyTransportHandlerFactory nettyTransportHandlerFactory;


    public NettyRiakClient() {
        clientSocketChannelFactoryProvider = new DefaultClientSocketChannelFactoryProvider();
        nettyTransportHandlerFactory = new NettyTransportHandlerFactory(clientSocketChannelFactoryProvider);
    }

    public void store(String hostUrl, String bucket, String key, String message) throws RiakTransportException {

        System.out.println("message\n" + message);

        ChannelBuffer ch = ChannelBuffers.copiedBuffer(message.toCharArray(), Charset.defaultCharset());

        URL target;

        try {
            target = URIUtils.appendToURIPath(URI.create(hostUrl), bucket, key).toURL();
        } catch (MalformedURLException e) {
            throw new RiakTransportException("Invalid URL.");
        } catch (URISyntaxException e) {
            throw new RiakTransportException("Unable to build URI from bucket and key.");
        }

//      System.out.println("connecting to " + target.toString());

        NettyTransportHandler nettyTransportHandler =
                nettyTransportHandlerFactory.getNettyTransportHandler(target, new BasicHttpPipelineFactory());

        Channel channel = null;
        Result result = null;

        try {
            channel = nettyTransportHandler.getChannel(10000);

            RiakResponseHandler storeResponseHandler = new RiakResponseHandler();
            setResponseHandler(channel, storeResponseHandler);

            // Prepare the HTTP request.
            HttpRequest request = new DefaultHttpRequest(
                    HttpVersion.HTTP_1_1, HttpMethod.POST, target.getPath());
            request.setContent(ch);

            request.setHeader(HttpHeaders.Names.CONTENT_TYPE, "application/json");
            request.setHeader(HttpHeaders.Names.HOST, target.getHost());
            request.setHeader(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.GZIP);

            // set the content length property
            request.setHeader(HttpHeaders.Names.CONTENT_LENGTH, ch.readableBytes());

            // Send the HTTP request.
            ChannelFuture writeFuture = channel.write(request);

            writeFuture.awaitUninterruptibly();

            Future<Result> futureResult = storeResponseHandler.getFutureResult();

            result = futureResult.get(60, TimeUnit.SECONDS);

            if (result.getStatusCode() != HttpResponseStatus.NO_CONTENT.getCode()) {
                nettyTransportHandler.end();
                throw new RiakTransportException("Invalid response status code = " + result.getStatusCode());
            }

        } catch (InterruptedException e) {
            nettyTransportHandler.end();
            throw new RiakTransportException("Issue retrieving response.", e);
        } catch (ExecutionException e) {
            nettyTransportHandler.end();
            throw new RiakTransportException("Issue retrieving response.", e);
        } catch (TimeoutException e) {
            nettyTransportHandler.end();
            throw new RiakTransportException("Timeout retrieving response.", e);
        } finally {
            if (channel != null) {
                clearResponseHandler(channel);
            }

            nettyTransportHandler.finished();
        }

        //} catch (Exception e) {
        //throw new RiakTransportException("Issue retrieving response.", e);
        //}

        log("headers\n" + result.getHeadersAsString());
    }

    public void close() {

        // Shut down executor threads to exit.

        nettyTransportHandlerFactory.shutdown();

    }

    public String buildRiakURL(String url, String bucket) {

        if (url.endsWith("/")) {
            return url.concat(bucket);
        } else {
            return url + "/" + bucket;
        }

    }

    private void clearResponseHandler(Channel channel) {
        channel.getPipeline().remove("RIAK_RESPONSE_HANDLER");
    }

    private void setResponseHandler(Channel channel, RiakResponseHandler responseHandler) {

        channel.getPipeline().addLast("RIAK_RESPONSE_HANDLER", responseHandler);

    }

}
