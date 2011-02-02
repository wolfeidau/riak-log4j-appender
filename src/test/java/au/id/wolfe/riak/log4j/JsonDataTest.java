package au.id.wolfe.riak.log4j;

import org.json.JSONStringer;
import org.junit.Test;

/**
 *
 */
public class JsonDataTest {

    @Test
    public void testMessageEncoding() throws Exception {

        String resultString = new JSONStringer()
                .object()
                .key("message")
                .value("SOME DATA\\\"s </ddd>")
                .endObject()
                .toString();

        System.out.println(resultString);
    }
}
