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

package io.galeb.core.rest.structure;

import java.io.Serializable;

public class PoolLinks implements Serializable {
    private static final long serialVersionUID = 1L;
    public Href self;
    public Href rules;
    public Href environment;
    public Href project;
    public Href balancePolicy;
    public Href targets;

}
