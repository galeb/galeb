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

package io.galeb.oldapi.controllers;

import io.galeb.oldapi.entities.v1.AbstractEntity;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractController<T extends AbstractEntity> {

    Map<String, String> queryWithIdOnly(String id) {
        return Stream.of(id).collect(Collectors.toMap(i -> "id", Function.identity()));
    }

    protected abstract ResponseEntity<PagedResources<Resource<? extends AbstractEntity>>> getSearch(String findType, Map<String, String> queryMap);

    protected abstract ResponseEntity<PagedResources<Resource<? extends AbstractEntity>>> get(Map<String, String> queryMap);

    protected abstract ResponseEntity<Resource<? extends AbstractEntity>> getWithId(String id, Map<String, String> queryMap);

    protected abstract ResponseEntity<Resource<? extends AbstractEntity>> post(String body);

    protected abstract ResponseEntity<String> postWithId(String id, String body);

    protected abstract ResponseEntity<String> put(String body);

    protected abstract ResponseEntity<Resource<? extends AbstractEntity>> putWithId(String id, String body);

    protected abstract ResponseEntity<String> delete();

    protected abstract ResponseEntity<Void> deleteWithId(String id);

    protected abstract ResponseEntity<String> patch(String body);

    protected abstract ResponseEntity<Void> patchWithId(String id, String body);

    protected abstract ResponseEntity<String> options();

    protected abstract ResponseEntity<String> optionsWithId(String id);

    protected abstract ResponseEntity<String> head();

    protected abstract ResponseEntity<String> headWithId(String id);

    protected abstract ResponseEntity<String> trace();

    protected abstract ResponseEntity<String> traceWithId(String id);
}
