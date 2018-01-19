package io.galeb.legba.services;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Set;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RoutersServiceTest {

    @Autowired
    private RoutersService routersService;

    @Test
    public void shouldRegisterRouter() {
        //Arrange
        String groupId = "group-local";
        String localIP = "127.0.0.1";
        String version = "1";
        String envId = "1";

        //Action
        routersService.put(groupId, localIP, version, envId);
        Set<JsonSchema.Env> envs = routersService.get(envId);

        //Assert
        JsonSchema.Env env = envs.stream().filter(e -> e.getEnvId().equals(envId)).findAny().orElse(null);
        Assert.assertNotNull(env);
        JsonSchema.GroupID groupID = env.getGroupIDs().stream().filter(g -> g.getGroupID().equals(groupId)).findAny().orElse(null);
        Assert.assertNotNull(groupID);
        boolean containsRouterAndVersion = groupID.getRouters().stream().anyMatch(r -> r.getLocalIp().equals(localIP) && r.getEtag().equals(version));
        Assert.assertTrue(containsRouterAndVersion);

    }
}
