@ignore
Feature: Pool tests
  Background:
    # Create environment envOne
    Given a REST client authenticated as admin with password pass
    When request json body has:
      | name  | envOne |
    And send POST /environment
    Then the response status is 201
    # Create balance policy balancePolicyOne
    Given a REST client authenticated as admin with password pass
    When request json body has:
      | name  | balancePolicyOne |
    And send POST /balancepolicy
    Then the response status is 201
    # Create projOne
    Given a REST client authenticated with token and role TEAM_ADMIN
    When request json body has:
      | name  | projOne |
      | teams | [Team=teamlocal] |
    And send POST /project
    Then the response status is 201
    # Create pool poolOne
    When request json body has:
      | name  | poolOne |
      | environment  | Environment=envOne |
      | balancepolicy  | BalancePolicy=balancePolicyOne |
      | project  | Project=projOne |
    And send POST /pool
    Then the response status is 201
    # Create rule ruleOne
    When request json body has:
      | name  | ruleOne |
      | matching  | / |
      | pools  | [Pool=poolOne] |
      | project  | Project=projOne |
    And send POST /rule
    Then the response status is 201

  Scenario: Should have balance policy on create rule
    Given a REST client authenticated with token and role TEAM_ADMIN
    When request json body has:
      | name  | poolTwo |
      | environment  | Environment=envOne |
      | project  | Project=projOne |
    And send POST /pool
    Then the response status is 409

  Scenario: Should have envinronment on create rule
    Given a REST client authenticated with token and role TEAM_ADMIN
    When request json body has:
      | name  | poolTwo |
      | balancepolicy  | BalancePolicy=balancePolicyOne |
      | project  | Project=projOne |
    And send POST /pool
    Then the response status is 409

  Scenario: Should have project on create rule
    Given a REST client authenticated with token and role TEAM_ADMIN
    When request json body has:
      | name  | poolTwo |
      | environment  | Environment=envOne |
      | balancepolicy  | BalancePolicy=balancePolicyOne |
    And send POST /pool
    Then the response status is 409

  Scenario: Create duplicated Pool
    Given a REST client authenticated with token and role TEAM_ADMIN
    When request json body has:
      | name  | poolOne |
      | environment  | Environment=envOne |
      | balancepolicy  | BalancePolicy=balancePolicyOne |
      | project  | Project=projOne |
    And send POST /pool
    Then the response status is 409

  Scenario: Get Pool
    Given a REST client authenticated with token and role TEAM_ADMIN
    When send GET Pool=poolOne
    Then the response status is 200
    And property name contains poolOne

  Scenario: Get null Pool
    Given a REST client authenticated with token and role TEAM_ADMIN
    When send GET Pool=NULL
    Then the response status is 404

  Scenario: Update Pool
    Given a REST client authenticated with token and role TEAM_ADMIN
    When request json body has:
      | name        | poolOneChanged            |
      | environment | Environment=envOne |
      | project     | Project=projOne    |
    And send PUT Pool=poolOne
    Then the response status is 200
    Given a REST client authenticated with token and role TEAM_ADMIN
    When send GET Pool=poolOneChanged
    Then the response status is 200
    And property name contains poolOneChanged

  Scenario: Update name field of Pool
    Given a REST client authenticated with token and role TEAM_ADMIN
    When request json body has:
      | name | poolTwo |
    And send PATCH Pool=poolOne
    Then the response status is 200
    When send GET Pool=poolTwo
    Then the response status is 200
    And property name contains poolTwo

  Scenario: Delete Pool
    Given a REST client authenticated with token and role TEAM_ADMIN
    When send DELETE Pool=poolOne
    Then the response status is 204
    When send GET Pool=poolOne
    Then the response status is 200
    And the response search at 'status.1' equal to DELETED
    When send GET Environment=envOne
    Then the response status is 200
    When send GET Project=projOne
    Then the response status is 200

  Scenario: Search Pool by Name
    Given a REST client authenticated with token and role TEAM_ADMIN
    When send GET /pool/search/findByName?name=poolOne
    Then the response search at '_embedded.pool[0].name' equal to poolOne

  Scenario: Search Pool by NameContaining
    Given a REST client authenticated with token and role TEAM_ADMIN
    When send GET /pool/search/findByNameContaining?name=One
    Then the response search at '_embedded.pool[0].name' equal to poolOne