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

package io.galeb.oldapi.services;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.reflect.TypeToken;
import io.galeb.oldapi.entities.v1.AbstractEntity;
import io.galeb.oldapi.entities.v1.Account;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.Response;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public abstract class AbstractHttpService<T> {

    protected final ObjectMapper mapper = new ObjectMapper();
    private TypeToken<T> typeToken = new TypeToken<T>(getClass()) {};
    private Class<? super T> entityClass = typeToken.getRawType();
    protected String envClassName = entityClass.getSimpleName().toLowerCase();
    private final AsyncHttpClient httpClient;

    AbstractHttpService(AsyncHttpClient httpClient) {
        this.httpClient = httpClient;
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @SuppressWarnings("unchecked")
    private Link convertLink(Map.Entry<String, Object> entry) {
        String href = ((LinkedHashMap<String, String>) entry.getValue()).get("href")
                .replaceAll(".*/" + envClassName, "/" + envClassName);
        return new Link(href, entry.getKey());
    }

    private String extractApiToken(Account account) {
        return account.getDescription().replaceAll(".*#", "");
    }

    protected abstract Set<Resource<T>> convertResources(ArrayList<LinkedHashMap> v2s);

    protected abstract T convertResource(LinkedHashMap resource) throws IOException;

    @SuppressWarnings("unchecked")
    List<Link> extractLinks(LinkedHashMap resource) {
        return ((LinkedHashMap<String, Object>) resource.get("_links")).entrySet().stream()
                .map(this::convertLink)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    ArrayList<LinkedHashMap> jsonToList(String body) throws IOException {
        return (ArrayList<LinkedHashMap>) ((LinkedHashMap)
                mapper.readValue(body, HashMap.class).get("_embedded")).get(envClassName);
    }

    // TODO: Set Environment Status
    AbstractEntity.EntityStatus extractStatus() {
        return AbstractEntity.EntityStatus.UNKNOWN;
    }

    Response getResponse(String url) throws InterruptedException, ExecutionException {
        Account account = (Account) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = account.getName();
        String password = extractApiToken(account); // extract token from description
        RequestBuilder requestBuilder = new RequestBuilder();
        requestBuilder.setRealm(Dsl.basicAuthRealm(username, password).setUsePreemptiveAuth(true));
        requestBuilder.setUrl(url);
        return httpClient.executeRequest(requestBuilder).get();
    }

    List<Link> getBaseLinks() {
        final List<Link> links = new ArrayList<>();
        links.add(new Link("/" + envClassName + "?page=0&size=1000{&sort}", "self"));
        links.add(new Link("/" + envClassName + "/search", "search"));
        return links;
    }
}
