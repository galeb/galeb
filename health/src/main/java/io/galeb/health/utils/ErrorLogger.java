/*
 * Copyright (c) 2014-2017 Globo.com - ATeam
 * All rights reserved.
 *
 * This source is subject to the Apache License, Version 2.0.
 * Please see the LICENSE file for more information.
 *
 * Authors: See AUTHORS file
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.galeb.health.utils;

import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class ErrorLogger {

    private static final StackTraceElement UNDEF_STASK_TRACE_ELEMENT =
            new StackTraceElement(Object.class.getName(), "undef", "undef", -1);

    public static void logError(Exception e, Class klazz) {
        String message = e.getMessage();
        if (message == null) {
            message = e.getClass().getSimpleName();
        }
        LoggerFactory.getLogger(klazz).error("Line " + Arrays.stream(e.getStackTrace())
                                                             .filter(stacktrace -> stacktrace.getClassName().equals(klazz.getName()))
                                                             .findFirst().orElse(UNDEF_STASK_TRACE_ELEMENT)
                                                             .getLineNumber() + ": " + message);
    }
}
