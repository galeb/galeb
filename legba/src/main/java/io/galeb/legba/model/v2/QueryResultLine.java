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

public class QueryResultLine implements Serializable {

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
    private final Integer pSize;
    private final String bpName;
    private final Date tLastModifiedAt;
    private final String tName;
    private final String hsLastModifiedAt;
    private final String hsStatus;
    private final Long rId;
    private final Long tId;

    public QueryResultLine(Object[] objects) {
        this.vId = objects[0] instanceof BigInteger ? ((BigInteger) objects[0]).longValue() : (Long) objects[0];
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
        this.pSize = ((BigInteger) objects[11]).intValue();
        this.bpName = (String) objects[12];
        this.tLastModifiedAt = (Date) objects[13];
        this.tName = (String) objects[14];
        this.hsLastModifiedAt = (String) objects[15];
        this.hsStatus = (String) objects[16];
        this.rId = objects[17] instanceof BigInteger ? ((BigInteger) objects[17]).longValue() : (Long) objects[17];
        this.tId = objects[18] instanceof BigInteger ? ((BigInteger) objects[18]).longValue() : (Long) objects[18];
    }

    public Long getVirtualhostId() {
        return vId;
    }

    public String getVirtualhostName() {
        return vName;
    }

    public Date getVirtualhostLastModifiedAt() {
        return vLastModifiedAt;
    }

    public Date getRuleOrderedLastModifiedAt() {
        return roLastModifiedAt;
    }

    public Integer getRuleOrderedOrder() {
        return roOrder;
    }

    public Date getRuleLastModifiedAt() {
        return rLastModifiedAt;
    }

    public Boolean getRuleGlobal() {
        return rGlobal;
    }

    public String getRuleName() {
        return rName;
    }

    public String getRuleMatching() {
        return rMatching;
    }

    public Date getPoolLastModifiedAt() {
        return pLastModifiedAt;
    }

    public String getPoolName() {
        return pName;
    }

    public Integer getPoolSize() {
        return pSize;
    }

    public String getBalancePolicyName() {
        return bpName;
    }

    public Date getTargetLastModifiedAt() {
        return tLastModifiedAt;
    }

    public String getTargetName() {
        return tName;
    }

    public String getHealthStatusLastModifiedAt() {
        return hsLastModifiedAt;
    }

    public String getHealthStatusStatus() {
        return hsStatus;
    }

    public Long getRuleId() {
        return rId;
    }

    public Long getTargetId() {
        return tId;
    }
}
