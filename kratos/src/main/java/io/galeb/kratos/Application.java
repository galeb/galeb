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

package io.galeb.kratos;

import io.galeb.core.configuration.RedisConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;

@SpringBootApplication
@EntityScan(basePackages = {"io.galeb.core.entity"})
@ComponentScan(basePackages = {"io.galeb.core.configuration", "io.galeb.kratos"}, excludeFilters = {@Filter(type = ASSIGNABLE_TYPE, value = {RedisConfiguration.class})})
@EnableJpaRepositories(basePackages = "io.galeb.kratos.repository")
public class Application {

    public static void main(String args[]) {
        SpringApplication.run(Application.class, args);
    }

}
