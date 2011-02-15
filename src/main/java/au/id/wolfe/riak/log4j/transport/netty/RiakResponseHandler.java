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

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Handles the http response from riak using a future to monitor it's status.
 */
public class RiakResponseHandler extends SimpleChannelUpstreamHandler {

    private FutureResult futureResult;

    public RiakResponseHandler() {
        super();
        this.futureResult = new FutureResult();
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {

        if (futureResult.isCancelled()) {
            return;
        }

        futureResult.start();

        try {
            HttpResponse httpResponse = (HttpResponse) e.getMessage();

            // TODO look into handling HTTP 100
            Result result = new ResultImpl();

            result.setStatusCode(httpResponse.getStatus().getCode());

            Map<String, List<String>> responseHeaders = result.getResponseHeaders();

            for (String headerName : httpResponse.getHeaderNames()) {
                responseHeaders.put(headerName, httpResponse.getHeaders(headerName));
            }

            futureResult.setResult(result);
            futureResult.done();

        } catch (Throwable t) {
            futureResult.setException(t);
            futureResult.done();
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {

        if (futureResult.isCancelled()){
            return;
        }
        futureResult.start();
        futureResult.setException(e.getCause());
        futureResult.done();
    }

    public Future<Result> getFutureResult() {
        return futureResult;
    }

    public static class FutureResult implements Future<Result> {

        private volatile Result result;
        private volatile Throwable exception;
        private volatile ResponseStatus responseStatus = ResponseStatus.NONE;

        private FutureResult() {
        }

        public void setResult(Result result) {
            this.result = result;
        }

        public void setException(Throwable exception) {
            this.exception = exception;
        }

        public void start() {
            responseStatus = ResponseStatus.STARTED;
        }

        public void done() {
            responseStatus = ResponseStatus.DONE;

            synchronized (this) {
                notifyAll();
            }
        }

        public boolean cancel(boolean mayInterruptIfRunning) {

            if (responseStatus != ResponseStatus.STARTED) {

                responseStatus = ResponseStatus.CANCELLED;

                synchronized (this) {
                    notifyAll();
                }

                return true;
            }

            return false;
        }

        public Result get() throws InterruptedException, ExecutionException {

            synchronized (this) {
                if (responseStatus != ResponseStatus.DONE) {
                    wait();
                }
            }

            if (responseStatus == ResponseStatus.CANCELLED) {
                throw new InterruptedException("Operation Cancelled");
            }

            if (exception != null) {
                throw new ExecutionException("Exception occured retrieving response.", exception);
            }

            return result;
        }

        public Result get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {

            synchronized (this) {
                if (responseStatus != ResponseStatus.DONE) {
                    unit.timedWait(this, timeout);
                }
            }

            if (responseStatus == ResponseStatus.CANCELLED) {
                throw new InterruptedException("Operation Cancelled");
            }

            if (exception != null) {
                throw new ExecutionException("Exception occured retrieving response.", exception);
            }

            return result;
        }

        public boolean isCancelled() {
            return responseStatus == ResponseStatus.CANCELLED;
        }

        public boolean isDone() {
            return responseStatus == ResponseStatus.DONE;
        }
    }

    public interface Result {

        public Map<String, List<String>> getResponseHeaders();

        public int getStatusCode();

        public void setStatusCode(int statusCode);

        public String getHeadersAsString();
    }

    public static class ResultImpl implements Result {

        Map<String, List<String>> responseHeaders = new HashMap<String, List<String>>();

        int statusCode;

        public Map<String, List<String>> getResponseHeaders() {
            return responseHeaders;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public void setStatusCode(int statusCode) {
            this.statusCode = statusCode;
        }

        public String getHeadersAsString() {
            StringBuilder stringBuilder = new StringBuilder();

            stringBuilder.append("{ ");
            for (String key : responseHeaders.keySet()) {
                stringBuilder.append("").append(key);
                stringBuilder.append(" = { ");
                for (String headerValue : responseHeaders.get(key)) {
                    stringBuilder.append(headerValue).append(" ");
                }
                stringBuilder.append("} ");
            }
            stringBuilder.append(" }");

            return stringBuilder.toString();
        }
    }

}
