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

package io.galeb.health.externaldata;

import io.galeb.manager.entity.Environment;
import io.galeb.manager.entity.Pool;
import io.galeb.manager.entity.Target;

import java.io.Serializable;

@SuppressWarnings("unused")
final class ManagerSpringRestResponse {

    static class TargetList {
        Embedded _embedded;

        static class Embedded {
            Target[] target;
        }
    }

    static class PoolList {
        Embedded _embedded;

        static class Embedded {
            PoolExtended[] pool;

            static class PoolExtended extends Pool {
                Links _links;

                static class Links implements Serializable {
                    private static final long serialVersionUID = 1L;
                    Href self;
                    Href rules;
                    Href environment;
                    Href project;
                    Href balancePolicy;
                    Href targets;

                    static class Href implements Serializable {
                        private static final long serialVersionUID = 1L;
                        String href;
                    }

                }
            }
        }
    }

    static class Token {
        Boolean admin;
        Boolean hasTeam;
        String account;
        String email;
        String token;
    }

    static class EnvironmentFindByName {
        Links _links;
        Embedded _embedded;
        Page page;

        static class Links {
            Self self;

            static class Self {
                String href;
                boolean templated;
            }
        }

        static class Page {
            int size;
            int totalElements;
            int totalpages;
            int number;
        }

        static class Embedded {
            Environment[] environment;
        }
    }
}
