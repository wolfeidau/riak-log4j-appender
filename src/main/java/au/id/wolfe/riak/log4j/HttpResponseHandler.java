package au.id.wolfe.riak.log4j;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import java.util.List;
import java.util.Map;

/**
 *
 */
public class HttpResponseHandler extends SimpleChannelUpstreamHandler {

    List<Map.Entry<String, String>> responseHeaders;

    boolean success;

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {

        HttpResponse response = (HttpResponse) e.getMessage();


        responseHeaders = response.getHeaders();

        // HTTP 1.1 201 CREATED - The request has been fulfilled and resulted in a new resource being created.
        success = response.getStatus() == HttpResponseStatus.CREATED;

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder()
                .append(getClass().getSimpleName())
                .append(" { success = ").append(success);

        if (responseHeaders != null) {
            for (Map.Entry<String, String> entry : responseHeaders) {
                sb.append(" ( ").append(entry.getKey()).append(" = ").append(entry.getValue()).append(" )");
            }
        }

        sb.append(" }");

        return sb.toString();
    }
}
