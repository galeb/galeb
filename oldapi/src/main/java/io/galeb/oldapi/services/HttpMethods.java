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

import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface HttpMethods<T> {

    default ResponseEntity<String> methodNotAllowed() {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }

    default ResponseEntity<PagedResources<Resource<T>>> getSearch(String findType, Map<String, String> queryMap) {
        return ResponseEntity.ok().build();
    }

    default ResponseEntity<PagedResources<Resource<T>>> get(Class<? extends io.galeb.core.entity.AbstractEntity> v2entityClass, Map<String, String> queryMap) {
        return ResponseEntity.badRequest().build();
    }

    default ResponseEntity<Resource<T>> getWithId(String id, Class<? extends io.galeb.core.entity.AbstractEntity> v2entityClass) {
        return ResponseEntity.badRequest().build();
    }

    default ResponseEntity<Resource<T>> post(String body, Class<? extends io.galeb.core.entity.AbstractEntity> v2entityClass) {
        return ResponseEntity.badRequest().build();
    }

    default ResponseEntity<Resource<T>> putWithId(String id, String body, Class<? extends io.galeb.core.entity.AbstractEntity> v2entityClass) {
        return ResponseEntity.badRequest().build();
    }

    default ResponseEntity<Void> deleteWithId(String id) {
        return ResponseEntity.noContent().build();
    }

    default ResponseEntity<Void> patchWithId(String id, String body) {
        return ResponseEntity.noContent().build();
    }

    default ResponseEntity<String> head() {
        return ResponseEntity.noContent().build();
    }
}
