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

import org.springframework.hateoas.Link;

import java.util.*;
import java.util.stream.Collectors;

public interface LinkProcessor {

    default void addLink(Set<Link> links, String href, String relName) {
        removeLink(links, href);
        links.add(new Link(href, relName));
    }

    default void removeLink(Set<Link> links, String relName) {
        links.removeIf(l -> l.getRel().equals(relName));
    }

    default Set<Link> pagedLinks(String resourceName, int size, int page) {
        final Set<Link> links = new HashSet<>();
        links.add(new Link("/" + resourceName + "?page=" + page + "&size=" + size, "self"));
        links.add(new Link("/" + resourceName + "/search", "search"));
        return links;
    }

    @SuppressWarnings("unchecked")
    default Link convertLink(Map.Entry<String, Object> entry, String resourceName) {
        String href = ((LinkedHashMap<String, String>) entry.getValue()).get("href")
                .replaceAll(".*/" + resourceName, "/" + resourceName);
        return new Link(href, entry.getKey());
    }

    @SuppressWarnings("unchecked")
    default Set<Link> extractLinks(LinkedHashMap resource, String resourceName) {
        return ((LinkedHashMap<String, Object>) resource.get("_links")).entrySet().stream()
                .map((Map.Entry<String, Object> entry) -> convertLink(entry, resourceName))
                .collect(Collectors.toSet());
    }

    default Set<Link> extractLinks(List<Link> links, String resourceName) {
        return links.stream()
                .map(e -> new Link(e.getHref().replaceAll(".*/" + resourceName, "/" + resourceName), e.getRel()))
                .collect(Collectors.toSet());
    }

    default long extractIdFromSelfLink(Set<Link> links) {
        return links.stream()
                .filter(l -> "self".equals(l.getRel()))
                .map(l -> l.getHref().replaceAll("^.*/", ""))
                .mapToLong(Long::parseLong).findAny().orElse(0);
    }
}
