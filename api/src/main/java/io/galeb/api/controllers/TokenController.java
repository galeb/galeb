package io.galeb.api.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.galeb.api.services.LocalAdminService;
import io.galeb.core.entity.Account;
import io.galeb.core.entity.RoleGroup;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
public class TokenController {

    private static final Log LOGGER = LogFactory.getLog(TokenController.class);

    private final ObjectMapper mapper = new ObjectMapper();

    @RequestMapping(value = "/token", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public String token() {

        Account account = (Account) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Map<String, Object> tokenInfo = new HashMap<>();
        tokenInfo.put("token", account.getApitoken());
        String loginName = account.getUsername();
        tokenInfo.put("account", loginName);
        tokenInfo.put("email", account.getEmail());
        Set<String> roles = account.getRolegroups().stream().map(RoleGroup::getName).collect(Collectors.toSet());
        tokenInfo.put("admin", roles.contains(RoleGroup.ROLEGROUP_SUPER_ADMIN) || LocalAdminService.NAME.equals(loginName));
        String json = "{}";
        try {
            json = mapper.writeValueAsString(tokenInfo);
        } catch (JsonProcessingException e) {
            LOGGER.error(e);
        }

        return json;
    }
}
