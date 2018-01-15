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

package io.galeb.oldapi.controllers;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@SuppressWarnings("unused")
@RestController
@RequestMapping("/")
public class IndexController {

    private static final String index =
    "{\n" +
        "  \"_links\": {\n" +
        "    \"farm\": {\n" +
        "      \"href\": \"@SERVER@farm{?page,size,sort}\",\n" +
        "      \"templated\": true\n" +
        "    },\n" +
        "    \"team\": {\n" +
        "      \"href\": \"@SERVER@team{?page,size,sort}\",\n" +
        "      \"templated\": true\n" +
        "    },\n" +
        "    \"virtualhost\": {\n" +
        "      \"href\": \"@SERVER@virtualhost{?page,size,sort}\",\n" +
        "      \"templated\": true\n" +
        "    },\n" +
        "    \"balancepolicy\": {\n" +
        "      \"href\": \"@SERVER@balancepolicy{?page,size,sort}\",\n" +
        "      \"templated\": true\n" +
        "    },\n" +
        "    \"ruletype\": {\n" +
        "      \"href\": \"@SERVER@ruletype{?page,size,sort}\",\n" +
        "      \"templated\": true\n" +
        "    },\n" +
        "    \"balancepolicytype\": {\n" +
        "      \"href\": \"@SERVER@balancepolicytype{?page,size,sort}\",\n" +
        "      \"templated\": true\n" +
        "    },\n" +
        "    \"rule\": {\n" +
        "      \"href\": \"@SERVER@rule{?page,size,sort}\",\n" +
        "      \"templated\": true\n" +
        "    },\n" +
        "    \"provider\": {\n" +
        "      \"href\": \"@SERVER@provider{?page,size,sort}\",\n" +
        "      \"templated\": true\n" +
        "    },\n" +
        "    \"environment\": {\n" +
        "      \"href\": \"@SERVER@environment{?page,size,sort}\",\n" +
        "      \"templated\": true\n" +
        "    },\n" +
        "    \"project\": {\n" +
        "      \"href\": \"@SERVER@project{?page,size,sort}\",\n" +
        "      \"templated\": true\n" +
        "    },\n" +
        "    \"target\": {\n" +
        "      \"href\": \"@SERVER@target{?page,size,sort}\",\n" +
        "      \"templated\": true\n" +
        "    },\n" +
        "    \"pool\": {\n" +
        "      \"href\": \"@SERVER@pool{?page,size,sort}\",\n" +
        "      \"templated\": true\n" +
        "    },\n" +
        "    \"account\": {\n" +
        "      \"href\": \"@SERVER@account{?page,size,sort}\",\n" +
        "      \"templated\": true\n" +
        "    },\n" +
        "    \"profile\": {\n" +
        "      \"href\": \"@SERVER@alps\"\n" +
        "    }\n" +
        "  }\n" +
        "}\n";

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> get(HttpServletRequest request) {
        String requestUrl = request.getRequestURL().toString();
        requestUrl = requestUrl.endsWith("/") ? requestUrl : requestUrl + "/";
        return ResponseEntity.ok(index.replaceAll("@SERVER@", requestUrl));
    }
}
