@active
Feature: Rule tests
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
    Given a REST client authenticated as user1 with password ""
    Then the response status is 200
    When request json body has:
      | name     | teamlocal              |
      | accounts         | [Account=user1]      |
    And send POST /team
    Then the response status is 201
    When request json body has:
      | name  | projOne |
      | teams | [Team=teamlocal] |
    And send POST /project
    Then the response status is 201
    # Create pool poolOne
    When request json body has:
      | name  | poolOne |
      | environment  | Environment=EnvOne |
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

  Scenario: Should does not create duplicate rule
    Given a REST client authenticated as user1 with password ""
    When request json body has:
      | name  | ruleOne |
      | matching  | / |
      | pools  | [Pool=poolOne] |
      | project  | Project=projOne |
    And send POST /rule
    Then the response status is 409

  Scenario: Should create rule in another pool and another project
    Given a REST client authenticated as user1 with password ""
    When request json body has:
      | name  | projTwo |
      | teams | [Team=teamlocal] |
    And send POST /project
    Then the response status is 201
    When request json body has:
      | name  | poolTwo |
      | environment  | Environment=envOne |
      | balancepolicy  | BalancePolicy=balancePolicyOne |
      | project  | Project=projTwo |
    And send POST /pool
    When request json body has:
      | name  | ruleTwo |
      | matching  | / |
      | pools  | [Pool=poolTwo] |
      | project  | Project=projTwo |
    And send POST /rule
    Then the response status is 201

  Scenario: Should create rule with same pool and another project
    Given a REST client authenticated as user1 with password ""
    When request json body has:
      | name  | projTwo |
      | teams | [Team=teamlocal] |
    And send POST /project
    Then the response status is 201
    When request json body has:
      | name  | ruleTwo |
      | matching  | / |
      | pools  | [Pool=poolOne] |
      | project  | Project=projTwo |
    And send POST /rule
    Then the response status is 201

  Scenario: Get null Rule
    Given a REST client authenticated as user1 with password ""
    When send GET Rule=NULL
    Then the response status is 404

  Scenario: Update all fields of Rule
    Given a REST client authenticated as user1 with password ""
    When request json body has:
      | name  | projTwo |
      | teams | [Team=teamlocal] |
    And send POST /project
    Then the response status is 201
    When request json body has:
      | name  | poolTwo |
      | environment  | Environment=envOne |
      | balancepolicy  | BalancePolicy=balancePolicyOne |
      | project  | Project=projTwo |
    And send POST /pool
    Then the response status is 201
    When request json body has:
      | name  | ruleTwo |
      | matching  | /match |
      | global  | true |
      | pools  | [Pool=poolTwo] |
    And send PATCH /rule/1
    Then the response status is 200
    When send GET Rule=ruleTwo
    Then the response status is 200
    And property name contains ruleTwo
    And property matching contains /match
    And property global contains true
    When send GET /rule/1/pools/2
    Then the response status is 200
    And property name contains poolTwo

  Scenario: Should delete rule
    Given a REST client authenticated as user1 with password ""
    And send DELETE /rule/1
    Then the response status is 204
    And send GET Rule=ruleOne
    Then the response status is 200
    And the response search at 'status.1' equal to DELETED

  Scenario: Search Rule by Name
    Given a REST client authenticated as user1 with password ""
    When send GET /rule/search/findByName?name=ruleOne
    Then the response search at '_embedded.rule[0].name' equal to ruleOne

  Scenario: Search Rule by NameContaining
    Given a REST client authenticated as user1 with password ""
    When send GET /rule/search/findByNameContaining?name=One
    Then the response search at '_embedded.rule[0].name' equal to ruleOne