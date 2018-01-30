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

package io.galeb.oldapi.entities.v1;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashSet;
import java.util.Set;

public class Provider extends AbstractEntity<Provider> {

    private static final long serialVersionUID = 5596582746795373019L;

    @JsonIgnore
    private final Set<Farm> farms = new HashSet<>();

    private String driver;

    private String provisioning;

    public Provider(String name) {
        setName(name);
    }

    public Provider() {
        //
    }

    public String getDriver() {
        return driver;
    }

    public Provider setDriver(String driver) {
        this.driver = driver;
        return this;
    }

    public String getProvisioning() {
        return provisioning;
    }

    public Provider setProvisioning(String provisioning) {
        this.provisioning = provisioning;
        return this;
    }

}
