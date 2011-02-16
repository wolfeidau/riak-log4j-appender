package au.id.wolfe.riak.log4j.utils;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Util methods for manipulating URI objects.
 */
public final class URIUtils {

    /**
     * This method will append a token to the path without destroying or removing the query component of the URI.
     *
     * @param uri           The URI to append to the path of.
     * @param pathsToAppend The token to append.
     * @return The new URI.
     * @throws URISyntaxException
     */
    public static URI appendToURIPath(URI uri, String... pathsToAppend) throws URISyntaxException {

        URI resultUri = URI.create(uri.toASCIIString());

        for (String pathToAppend : pathsToAppend) {

            String path = resultUri.getPath();

            if (path.endsWith("/")) {
                path = path.concat(pathToAppend);
            } else {
                path = String.format("%s/%s", path, pathToAppend);
            }

            resultUri = new URI(resultUri.getScheme(), null, resultUri.getHost(),
                    resultUri.getPort(), path, resultUri.getQuery(), null);
        }

        return resultUri;

    }
}
