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
import au.id.wolfe.riak.log4j.utils.Tracer;
import org.apache.log4j.spi.ErrorCode;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.text.SimpleDateFormat;
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

    /* RFC822 date formatter
    private final SimpleDateFormat format =
            new SimpleDateFormat("yyyy-mm-DD'T'hh:mm:ssZ");
    */

    private RiakClient riakClient = new NettyRiakClient();

    public RiakAppender() {
        riakClient = new NettyRiakClient();
    }

    public RiakAppender(boolean isActive) {
        super(isActive);
        riakClient = new NettyRiakClient();
    }

    public RiakAppender(RiakClient riakClient) {
        this.riakClient = riakClient;
    }

    @Override
    protected void append(LoggingEvent event) {

        try {
            String recordKey = buildKey(event);
            String jsonObject = buildJson(recordKey, event);
            riakClient.store(url, bucket, recordKey, jsonObject);
        } catch (JSONException e) {
            errorHandler.error("Error encoding record to JSON format.", e, ErrorCode.GENERIC_FAILURE);
        } catch (RiakTransportException e) {
            errorHandler.error("Error storing record", e, ErrorCode.WRITE_FAILURE);
        }

    }

    /**
     * builds the JSON string containing the log event attributes.
     *
     * @param recordKey The key for the record.
     * @param event log event
     * @throws JSONException This may be caused by malformed string input.
     * @return String containing log event serialised to JSON.
     * @see java.util.logging.XMLFormatter
     * */
    private String buildJson(String recordKey, LoggingEvent event) throws JSONException {

        JSONStringer jsonLogEvent;

        jsonLogEvent = new JSONStringer();

        jsonLogEvent.object();

        if (event.getLocationInformation() != null) {
            JSONObject sourceInfo = new JSONObject();

            sourceInfo.put("class", event.getLocationInformation().getClassName());
            sourceInfo.put("fileName", event.getLocationInformation().getFileName());
            sourceInfo.put("lineNumber", event.getLocationInformation().getLineNumber());
            sourceInfo.put("methodName", event.getLocationInformation().getMethodName());

            jsonLogEvent.key("sourceInfo").value(sourceInfo);
        }

        jsonLogEvent.key("class").value(event.getFQNOfLoggerClass());
        jsonLogEvent.key("level").value(event.getLevel());
        jsonLogEvent.key("message").value(event.getMessage());

        // ripple specific information, I don't see any issue including it even if it is not used.
        jsonLogEvent.key("record_id").value(recordKey);
        jsonLogEvent.key("_type").value("LogRecord");

        if (event.getThrowableInformation() != null) {
            ThrowableInformation throwableInformation = event.getThrowableInformation();

            JSONArray stack = new JSONArray();

            for (StackTraceElement stackTraceElement : throwableInformation.getThrowable().getStackTrace()) {
                JSONObject frame = new JSONObject();
                frame.put("class", stackTraceElement.getClassName());
                frame.put("method", stackTraceElement.getMethodName());
                if (stackTraceElement.getLineNumber() >= 0) {
                    frame.put("lineNumber", stackTraceElement.getLineNumber());
                }
                stack.put(frame);
            }

            JSONObject throwableInfo = new JSONObject();

            throwableInfo.put("throwable", throwableInformation.getThrowable().getClass().getCanonicalName());
            throwableInfo.put("message", throwableInformation.getThrowable().getMessage());
            throwableInfo.put("stack", stack);

            jsonLogEvent.key("throwableInfo").value(throwableInfo);


        }

        jsonLogEvent.key("millis").value(event.getTimeStamp());

        return jsonLogEvent.endObject().toString();


    }

    /* Builds the key for the log entry, at this stage it is {RFC822date}-{sequence} */
    private String buildKey(LoggingEvent event) {
        return String.format("%d-%d", event.getTimeStamp(), sequence.getAndIncrement());
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
