package au.id.wolfe.riak.log4j.transport.netty;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.http.HttpClientCodec;
import org.jboss.netty.handler.codec.http.HttpContentDecompressor;


/**
 *
 */
public class NettyHttpClientPipelineFactory implements ChannelPipelineFactory {

    StoreResponseHandler storeResponseHandler;

    public NettyHttpClientPipelineFactory(StoreResponseHandler storeResponseHandler) {
        this.storeResponseHandler = storeResponseHandler;
    }

    public ChannelPipeline getPipeline() throws Exception {

        // Create a default pipeline implementation.
        ChannelPipeline pipeline = Channels.pipeline();

        pipeline.addLast("codec", new HttpClientCodec());

        // Remove the following line if you don't want automatic content decompression.
        pipeline.addLast("inflater", new HttpContentDecompressor());

        // response handler
        pipeline.addLast("handler", storeResponseHandler);

        return pipeline;
    }
}