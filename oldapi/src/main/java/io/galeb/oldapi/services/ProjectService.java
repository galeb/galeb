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

package io.galeb.oldapi.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.galeb.core.entity.Team;
import io.galeb.oldapi.entities.v1.AbstractEntity;
import io.galeb.oldapi.entities.v1.Project;
import io.galeb.oldapi.services.http.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class ProjectService extends AbstractConverterService<Project> {

    private static final Logger LOGGER = LogManager.getLogger(ProjectService.class);

    private static final String[] ADD_REL = {"targets"};
    private static final String[] DEL_REL = {"rolegroups","project","rules"};

    @Override
    String[] addRel() {
        return ADD_REL;
    }

    @Override
    String[] delRel() {
        return DEL_REL;
    }

    private JsonNode rebuildProjectV2Json(io.galeb.core.entity.Project project) {
        ArrayNode arrayOfTeams = new ArrayNode(JsonNodeFactory.instance);
        JsonNode jsonNodeProject = convertFromJsonObjToJsonNode(project);
        for (io.galeb.core.entity.Team team: project.getTeams()) {
            arrayOfTeams.add("http://localhost/team/" + team.getId());
        }
        ((ObjectNode) jsonNodeProject).replace("teams", arrayOfTeams);
        return jsonNodeProject;
    }

    private Set<Team> extracTeams(Set<String> teams) {
        return teams.stream()
                .map(teamUrl -> {
                    try {
                        long teamId = Long.parseLong(teamUrl.replaceAll("^.*/", ""));
                        Team teamV2 = new Team();
                        teamV2.setId(teamId);
                        return teamV2;
                    } catch (Exception ignored) { }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    @Override
    public ResponseEntity<Resource<? extends AbstractEntity>> post(String body, Class<? extends io.galeb.core.entity.AbstractEntity> v2entityClass) {
        Project projectV1 = convertFromJsonStringToV1(body);

        if (projectV1 != null) {
            try {
                io.galeb.core.entity.Project projectV2 = new io.galeb.core.entity.Project();
                projectV2.setName(projectV1.getName());
                projectV2.setTeams(extracTeams(projectV1.getTeams()));

                JsonNode jsonNode = rebuildProjectV2Json(projectV2);
                String projectResourcePath = Project.class.getSimpleName().toLowerCase();
                Response response = httpClientService.post(apiUrl + "/" + projectResourcePath, jsonNode.toString());
                return processResponse(response, -1, HttpMethod.POST, v2entityClass);
            } catch (ExecutionException | InterruptedException | IOException e) {
                LOGGER.error(e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
            }
        }
        return ResponseEntity.badRequest().build();
    }

}
