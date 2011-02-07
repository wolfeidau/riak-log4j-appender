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

package au.id.wolfe.riak.log4j.util;

/**
 *
 */
public final class Assert {

    public static void hasText(String text, String message) {
        int strLen;
        if (text == null || (strLen = text.length()) == 0) {
            throw new IllegalArgumentException(message);
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(text.charAt(i))) {
                return;
            }
        }
        throw new IllegalArgumentException(message);
    }

    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void noneNull(Object... object) {
        for (int i = 0; i < object.length; ++i) {
            if (object[i] == null) {
                throw new NullPointerException("Null not allowed, number " + (i + 1));
            }
        }
    }

    public static void isTrue(boolean b, String message) {
        if (!b) {
            throw new IllegalArgumentException(message);
        }
    }
}
