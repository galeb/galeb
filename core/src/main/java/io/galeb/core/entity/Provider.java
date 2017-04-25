/*
 *   Galeb - Load Balance as a Service Plataform
 *
 *   Copyright (C) 2014-2015 Globo.com
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.galeb.core.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "UK_name_provider", columnNames = { "name" }) })
public class Provider extends AbstractEntity<Provider> {

    private static final long serialVersionUID = 5596582746795373019L;

    @JsonIgnore
    @OneToMany(mappedBy = "provider", fetch = FetchType.EAGER)
    private final Set<Farm> farms = new HashSet<>();

    @Column
    private String driver;

    @Column
    private String provisioning;

    public Provider(String name) {
        setName(name);
    }

    protected Provider() {
        //
    }

    public String getDriver() {
        return driver;
    }

    public Provider setDriver(String driver) {
        updateHash();
        this.driver = driver;
        return this;
    }

    public String getProvisioning() {
        return provisioning;
    }

    public Provider setProvisioning(String provisioning) {
        updateHash();
        this.provisioning = provisioning;
        return this;
    }

}
