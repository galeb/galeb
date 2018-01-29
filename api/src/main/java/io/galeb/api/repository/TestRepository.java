package io.galeb.api.repository;

import io.galeb.api.repository.TestRepository;
import io.galeb.core.entity.Account;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;

public interface TestRepository extends JpaRepository<Account, String>{

    @Override
    @ApiOperation(value="aaaaaa", notes = "aaaaaa",
            httpMethod = "POST"
    )
    Account save(Account account);

}
