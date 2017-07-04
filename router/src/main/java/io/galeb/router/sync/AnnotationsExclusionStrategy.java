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

package io.galeb.router.sync;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

class AnnotationsExclusionStrategy implements ExclusionStrategy {

    @Override
    public boolean shouldSkipField(FieldAttributes f) {
        return f.getAnnotation(ManyToOne.class)  != null ||
               f.getAnnotation(OneToMany.class)  != null ||
               f.getAnnotation(ManyToMany.class) != null;
    }

    @Override
    public boolean shouldSkipClass(Class<?> clazz) {
        return clazz.getAnnotation(ManyToOne.class)  != null ||
               clazz.getAnnotation(OneToMany.class)  != null ||
               clazz.getAnnotation(ManyToMany.class) != null;
    }
}
