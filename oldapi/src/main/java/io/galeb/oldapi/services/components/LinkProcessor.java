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

package io.galeb.oldapi.services.components;

import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class LinkProcessor {

    public LinkProcessor add(Set<Link> links, String href, String relName) {
        remove(links, href);
        links.add(new Link(href, relName));
        return this;
    }

    public LinkProcessor remove(Set<Link> links, String relName) {
        links.removeIf(l -> l.getRel().equals(relName));
        return this;
    }

    public Set<Link> pagedLinks(String resourceName, int size, int page) {
        final Set<Link> links = new HashSet<>();
        links.add(new Link("/" + resourceName + "?page=" + page + "&size=" + size, "self"));
        links.add(new Link("/" + resourceName + "/search", "search"));
        return links;
    }

    @SuppressWarnings("unchecked")
    public Link convertLink(Map.Entry<String, Object> entry, String resourceName) {
        String href = ((LinkedHashMap<String, String>) entry.getValue()).get("href")
                .replaceAll(".*/" + resourceName, "/" + resourceName);
        return new Link(href, entry.getKey());
    }

    @SuppressWarnings("unchecked")
    public Set<Link> extractLinks(LinkedHashMap resource, String resourceName) {
        return ((LinkedHashMap<String, Object>) resource.get("_links")).entrySet().stream()
                .map((Map.Entry<String, Object> entry) -> convertLink(entry, resourceName))
                .collect(Collectors.toSet());
    }

    public long extractId(Set<Link> links) {
        return links.stream()
                .filter(l -> "self".equals(l.getRel()))
                .map(l -> l.getHref().replaceAll("^.*/", ""))
                .mapToLong(Long::parseLong).findAny().orElse(0);
    }
}
