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
import au.id.wolfe.riak.log4j.util.Assert;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.*;

import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of a basic riak client using Netty.
 */
public class NettyRiakClient implements RiakClient {

    /* Thread pool */
    ExecutorService executorService = Executors.newCachedThreadPool();

    /* client bootstrap */
    ClientBootstrap bootstrap = new ClientBootstrap(
            new NioClientSocketChannelFactory(
                    executorService,
                    executorService));


    public void store(String hostUrl, String bucket, String message) throws RiakTransportException {

        ChannelBuffer ch = ChannelBuffers.copiedBuffer(message.toCharArray(), Charset.defaultCharset());

        URI uri = buildRiakURL(hostUrl, bucket);

        String host = uri.getHost();
        int port = uri.getPort();
        String scheme = uri.getScheme();

        if (!scheme.equalsIgnoreCase("http")) {
            System.err.println("Only HTTP is supported.");
            return;
        }

        RiakResponseHandler storeResponseHandler = new RiakResponseHandler();

        bootstrap.setPipelineFactory(new NettyHttpClientPipelineFactory(storeResponseHandler));

        // Start the connection attempt.
        ChannelFuture future = bootstrap.connect(new InetSocketAddress(host, port));

        // Wait until the connection attempt succeeds or fails.
        Channel channel = future.awaitUninterruptibly().getChannel();

        if (!future.isSuccess()) {
            bootstrap.releaseExternalResources();
            throw new RiakTransportException("Failed to connect to server");
        }

        // Prepare the HTTP request.
        HttpRequest request = new DefaultHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.POST, uri.toASCIIString());
        request.setContent(ch);
        request.setHeader(HttpHeaders.Names.CONTENT_TYPE, "application/json");
        request.setHeader(HttpHeaders.Names.HOST, host);
        request.setHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
        request.setHeader(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.GZIP);

        // Send the HTTP request.
        channel.write(request);

        // Wait for the server to close the connection.
        channel.getCloseFuture().awaitUninterruptibly();


        Future<Result> futureResult = storeResponseHandler.getFutureResult();

        Result result = null;

        try {
            result = futureResult.get(60, TimeUnit.SECONDS);

            Assert.isTrue(result.getStatusCode() == 201, "Invalid response");

        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    public void close() {

        // Shut down executor threads to exit.
        bootstrap.releaseExternalResources();

    }

    public URI buildRiakURL(String url, String bucket) {

        Assert.notNull(url, "Url is null");
        Assert.notNull(bucket, "Bucket is null");

        URI uri = URI.create(url);

        if (uri.getPath().endsWith("/")) {
            return URI.create(uri.toString().concat(bucket));
        } else {
            return URI.create(uri.toString() + "/" + bucket);
        }

    }

}
