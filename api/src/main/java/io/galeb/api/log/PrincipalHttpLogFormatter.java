/*
 * Copyright (c) 2014-2018 Globo.com - ATeam
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

package io.galeb.api.log;

import io.galeb.core.enums.SystemEnv;
import java.io.IOException;
import java.util.Map;
import javax.annotation.Nullable;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.JsonHttpLogFormatter;
import org.zalando.logbook.Precorrelation;

public class PrincipalHttpLogFormatter implements HttpLogFormatter {

    private final JsonHttpLogFormatter delegate;

    private static final String LOGGING_TAGS = SystemEnv.LOGGING_TAGS.getValue();

    public PrincipalHttpLogFormatter(final JsonHttpLogFormatter delegate) {
        this.delegate = delegate;
    }

    @Override
    public String format(final Precorrelation<HttpRequest> precorrelation) throws IOException {
        // Not logging GET/HEAD methods
        if (precorrelation.getRequest().getMethod().equalsIgnoreCase(HttpMethod.GET.name()) ||
            precorrelation.getRequest().getMethod().equalsIgnoreCase(HttpMethod.HEAD.name())) {
            return "";
        }
        final Map<String, Object> content = delegate.prepare(precorrelation);
        content.put("type","gelf");
        content.put("principal", getPrincipal());
        content.put("message", "Request message");
        content.put("tags", LOGGING_TAGS);
        return delegate.format(content);
    }

    @Override
    public String format(final Correlation<HttpRequest, HttpResponse> correlation) throws IOException {
        if (correlation.getRequest().getMethod().equalsIgnoreCase(HttpMethod.GET.name()) ||
            correlation.getRequest().getMethod().equalsIgnoreCase(HttpMethod.HEAD.name())) {
            return "";
        }
        final Map<String, Object> content = delegate.prepare(correlation);
        content.put("type":"gelf");
        content.put("bodyResponse", correlation.getOriginalResponse().getBodyAsString());
        content.put("principal", getPrincipal());
        content.put("message", "Response message");
        content.put("tags", LOGGING_TAGS);
        return delegate.format(content);
    }

    private String getPrincipal() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        @Nullable final String principal = authentication == null ? null : authentication.getName();
        return principal == null ? "anonymous" : principal;
    }
}