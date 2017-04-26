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

package io.galeb.router.tests.mocks;

import io.galeb.core.services.HttpClientService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({ "test" })
public class HttpClientServiceConfigurationMock {

    @Bean
    HttpClientService httpClientService() {
        return new HttpClientService() {

            @Override
            public void getResponseBodyWithToken(String url, String token, OnCompletedCallBack callBack) {
                // TODO: implementation
            }

            @Override
            public boolean patchResponse(String url, String body, String token) {
                // TODO: implementation
                return false;
            }

            @Override
            public String getResponseBodyWithAuth(String user, String pass, String url) {
                // TODO: implementation
                return null;
            }
        };
    }
}
