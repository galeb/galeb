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

package io.galeb.legba.model.v2;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;

public class FullEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Long vId;
    private final String vName;
    private final Date vLastModifiedAt;
    private final Date roLastModifiedAt;
    private final Integer roOrder;
    private final Date rLastModifiedAt;
    private final Boolean rGlobal;
    private final String rName;
    private final String rMatching;
    private final Date pLastModifiedAt;
    private final String pName;
    private final Integer pPoolSize;
    private final String bpName;
    private final Date tLastModifiedAt;
    private final String tName;
    private final String hsLastModifiedAt;

    public FullEntity(Object[] objects) {
        this.vId = ((BigInteger) objects[0]).longValue();
        this.vLastModifiedAt = (Date) objects[1];
        this.vName = (String) objects[2];
        this.roLastModifiedAt = (Date) objects[3];
        this.roOrder = (Integer) objects[4];
        this.rLastModifiedAt = (Date) objects[5];
        this.rGlobal = (Boolean) objects[6];
        this.rName = (String) objects[7];
        this.rMatching = (String) objects[8];
        this.pLastModifiedAt = (Date) objects[9];
        this.pName = (String) objects[10];
        this.pPoolSize = ((BigInteger) objects[11]).intValue();
        this.bpName = (String) objects[12];
        this.tLastModifiedAt = (Date) objects[13];
        this.tName = (String) objects[14];
        this.hsLastModifiedAt = (String) objects[15];
    }

    public Long getvId() {
        return vId;
    }

    public String getvName() {
        return vName;
    }

    public Date getvLastModifiedAt() {
        return vLastModifiedAt;
    }

    public Date getRoLastModifiedAt() {
        return roLastModifiedAt;
    }

    public Integer getRoOrder() {
        return roOrder;
    }

    public Date getrLastModifiedAt() {
        return rLastModifiedAt;
    }

    public Boolean getrGlobal() {
        return rGlobal;
    }

    public String getrName() {
        return rName;
    }

    public String getrMatching() {
        return rMatching;
    }

    public Date getpLastModifiedAt() {
        return pLastModifiedAt;
    }

    public String getpName() {
        return pName;
    }

    public Integer getpPoolSize() {
        return pPoolSize;
    }

    public String getBpName() {
        return bpName;
    }

    public Date gettLastModifiedAt() {
        return tLastModifiedAt;
    }

    public String gettName() {
        return tName;
    }

    public String getHsLastModifiedAt() {
        return hsLastModifiedAt;
    }
}
