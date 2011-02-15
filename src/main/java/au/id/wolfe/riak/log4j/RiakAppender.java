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

import au.id.wolfe.riak.log4j.transport.RiakClient;
import au.id.wolfe.riak.log4j.transport.RiakTransportException;
import au.id.wolfe.riak.log4j.transport.netty.NettyRiakClient;
import au.id.wolfe.riak.log4j.transport.netty.NettyRiakClientNew;
import au.id.wolfe.riak.log4j.utils.Tracer;
import org.apache.log4j.spi.ErrorCode;
import org.apache.log4j.spi.LoggingEvent;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONStringer;

import java.text.SimpleDateFormat;
import java.util.Date;
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

    private RiakClient riakClient = new NettyRiakClientNew();


    @Override
    protected void append(LoggingEvent event) {

        try {
            storeJson(buildJson(event));
        } catch (JSONException e) {
            errorHandler.error("Error encoding record to JSON format.", e, ErrorCode.GENERIC_FAILURE);
        }

    }

    /* Uses riak client to store the object */
    private void storeJson(String jsonObject) {
        try {
            riakClient.store(url, bucket, jsonObject);
        } catch (RiakTransportException e) {
            errorHandler.error("Error storing record", e, ErrorCode.WRITE_FAILURE);
        }
    }

    /* builds the JSON string containing the log event attributes i wanted */
    private String buildJson(LoggingEvent event) throws JSONException {

        JSONArray throwableInfo = null;


        if (event.getThrowableStrRep() != null){
            throwableInfo = new JSONArray(event.getThrowableStrRep());
        }

        return new JSONStringer()
                .object()
                .key("thread").value(event.getThreadName())
                .key("class").value(event.getFQNOfLoggerClass())
                .key("level").value(event.getLevel())
                .key("message").value(event.getMessage())
                .key("throwableInfo").value(throwableInfo)
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


    public void setUrl(String url) {
        this.url = url;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public void setTraceEnabled(boolean traceEnabled) {
        Tracer.setTraceEnabled(traceEnabled);
    }

    public void close() {

        riakClient.close();
    }

    public boolean requiresLayout() {
        return false;
    }
}
