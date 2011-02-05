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

package au.id.wolfe.riak.log4j;

import au.id.wolfe.riak.log4j.util.Assert;
import org.apache.log4j.spi.LoggingEvent;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.*;
import org.json.JSONException;
import org.json.JSONStringer;

import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This log4j appender builds and stores log events in riak.
 *
 * @author Mark Wolfe (<A HREF="mailto:mark@wolfe.id.au">mark@wolfe.id.au</A>)
 */
public class RiakAppender extends org.apache.log4j.AppenderSkeleton
        implements org.apache.log4j.Appender {

    /* sequence number for records created by this appender */
    private final AtomicLong sequence = new AtomicLong();

    /* URL of the RIAK server */
    private String url;

    /* bucket this will append messages too */
    private String bucket;

    /* RFC822 date formatter */
    private final SimpleDateFormat format =
            new SimpleDateFormat("yyyy-mm-DD'T'hh:mm:ssZ");

    // Configure the client.
    ClientBootstrap bootstrap = new ClientBootstrap(
            new NioClientSocketChannelFactory(
                    Executors.newCachedThreadPool(),
                    Executors.newCachedThreadPool()));

    @Override
    protected void append(LoggingEvent event) {

        try {
            storeJson(buildJson(event));
        } catch (JSONException e) {
            // todo ErrorHandler
        }

    }

    private void storeJson(String jsonObject){

        ChannelBuffer ch = ChannelBuffers.copiedBuffer(jsonObject.toCharArray(), Charset.defaultCharset());

        URI uri = buildRiakURL(url, bucket);

        String host = uri.getHost();
        int port = uri.getPort();
        String scheme = uri.getScheme();

        if (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https")) {
            System.err.println("Only HTTP(S) is supported.");
            return;
        }

        bootstrap.setPipelineFactory(new HttpClientPipelineFactory());

        // Start the connection attempt.
        ChannelFuture future = bootstrap.connect(new InetSocketAddress(host, port));

        // Wait until the connection attempt succeeds or fails.
        Channel channel = future.awaitUninterruptibly().getChannel();

        if (!future.isSuccess()) {
            errorHandler.error("Failed to connect to server. seq = " + sequence.get());
            bootstrap.releaseExternalResources();
            return;
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

        // Need ascertain if the record was store was successful
        if (!((HttpResponseHandler) channel.getPipeline().get("handler")).success){
            errorHandler.error("Failed to store message. seq = " + sequence.get());
        }

    }

    /* builds the JSON string containing the log event attributes i wanted */
    private String buildJson(LoggingEvent event) throws JSONException {

        return new JSONStringer()
                .object()
                .key("thread").value(event.getThreadName())
                .key("class").value(event.getFQNOfLoggerClass())
                .key("level").value(event.getLevel())
                .key("message").value(event.getMessage())
                .key("timestamp").value(event.getTimeStamp())
                .endObject()
                .toString();

    }

    /* Builds the key for the log entry, at this stage it is {RFC822date}-{sequence} */
    private String buildKey(LoggingEvent event) {

        String dateStamp = format.format(new Date(event.getTimeStamp()));
        long sequenceValue = sequence.getAndDecrement();

        return String.format("%s-%d", dateStamp, sequenceValue);
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

    public void setUrl(String url) {
        this.url = url;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public void close() {

        // Shut down executor threads to exit.
        bootstrap.releaseExternalResources();

    }

    public boolean requiresLayout() {
        return false;
    }
}
