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

package io.galeb.api.handler;

import io.galeb.core.common.EntitiesRegistrable;
import io.galeb.core.entity.AbstractEntity;
import io.galeb.core.entity.Environment;
import io.galeb.core.services.ChangesService;
import io.galeb.core.services.VersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.event.AbstractRepositoryEventListener;

import java.util.Collections;
import java.util.Set;

@SuppressWarnings("unused")
public abstract class AbstractHandler<T extends AbstractEntity> extends AbstractRepositoryEventListener<T> {

    @Autowired
    ChangesService changesService;

    @Autowired
    VersionService versionService;

    @Override
    protected void onBeforeCreate(T entity) {
        super.onBeforeCreate(entity);
    }

    @Override
    protected void onAfterCreate(T entity) {
        super.onAfterCreate(entity);
        registerChanges(entity);
    }

    @Override
    protected void onBeforeSave(T entity) {
        super.onBeforeSave(entity);
    }

    @Override
    protected void onAfterSave(T entity) {
        super.onAfterSave(entity);
        registerChanges(entity);
    }

    @Override
    protected void onBeforeLinkSave(T parent, Object linked) {
        super.onBeforeLinkSave(parent, linked);
    }

    @Override
    protected void onAfterLinkSave(T parent, Object linked) {
        super.onAfterLinkSave(parent, linked);
    }

    @Override
    protected void onBeforeLinkDelete(T parent, Object linked) {
        super.onBeforeLinkDelete(parent, linked);
    }

    @Override
    protected void onAfterLinkDelete(T parent, Object linked) {
        super.onAfterLinkDelete(parent, linked);
    }

    @Override
    protected void onBeforeDelete(T entity) {
        super.onBeforeDelete(entity);
    }

    @Override
    protected void onAfterDelete(T entity) {
        super.onAfterDelete(entity);
        registerChanges(entity);
    }

    protected Set<Environment> getAllEnvironments(T entity) {
        return Collections.emptySet();
    }

    private void registerChanges(T entity) {
        if (!EntitiesRegistrable.contains(entity.getClass().getSimpleName())) {
            return;
        }
        getAllEnvironments(entity).forEach(e ->
                changesService.register(e, entity, String.valueOf(versionService.incrementVersion(e.getId()))));
    }

}
