/*
 * Copyright (c) 2014-2016 Globo.com - ATeam
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

package io.galeb.router.handlers;

import io.undertow.util.StatusCodes;

interface ProcessorLocalStatusCode {

    int OFFSET_LOCAL_ERROR = 400;
    int NOT_MODIFIED       = 0;

    default int getFakeStatusCode(final String backend,
                                  int statusCode,
                                  long responseBytesSent,
                                  Integer responseTime,
                                  int maxRequestTime) {
        int statusLogged = NOT_MODIFIED;
        if (responseTime != null &&
                statusCode == StatusCodes.OK &&
                responseBytesSent <= 0 &&
                maxRequestTime > 0 &&
                responseTime > maxRequestTime) {

            statusLogged = StatusCodes.BAD_GATEWAY + OFFSET_LOCAL_ERROR;

        } else if (backend == null) {

            statusLogged = statusCode + OFFSET_LOCAL_ERROR;

        }

        return statusLogged;
    }
}
