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
