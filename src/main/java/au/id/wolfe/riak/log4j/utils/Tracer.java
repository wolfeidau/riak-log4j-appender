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

/**
 * Just used for trace message.
 */
public final class Tracer {

    private static boolean traceEnabled = false;

    public static void log(String message) {

        if (traceEnabled) {
            System.out.println(message);
        }
    }

    public static void setTraceEnabled(boolean traceEnabled) {
        Tracer.traceEnabled = traceEnabled;
    }
}
