@active
Feature: Tests Target
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
    And send GET /
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
        # Create target targetOne
    When request json body has:
      | name  | targetOne |
      | pool  | Pool=poolOne |
    And send POST /target
    Then the response status is 201

  Scenario: Should does not create duplicate target in same pool
    Given a REST client authenticated as user1 with password ""
    And send GET /
    When request json body has:
      | name  | targetOne |
      | pool  | Pool=poolOne |
    And send POST /target
    Then the response status is 409

  Scenario: Should does not create target without pool
    Given a REST client authenticated as user1 with password ""
    And send GET /
    When request json body has:
      | name  | targetTwo |
    And send POST /target
    Then the response status is 400

  Scenario: Should change pool
    Given a REST client authenticated as user1 with password ""
    And send GET /
    When request json body has:
      | name  | poolTwo |
      | environment  | Environment=EnvOne |
      | balancepolicy  | BalancePolicy=balancePolicyOne |
      | project  | Project=projOne |
    And send POST /pool
    Then the response status is 201
    When request json body has:
      | pool  | Pool=poolTwo |
    And send PATCH /target/1
    Then the response status is 200
    And send GET /target/1/pool
    Then the response status is 200
    And property name contains poolTwo

  Scenario: Should change target name
    Given a REST client authenticated as user1 with password ""
    And send GET /
    When request json body has:
      | name  | targetTwo |
    And send PATCH /target/1
    Then the response status is 200
    And send GET Target=targetTwo
    Then the response status is 200
    And property name contains targetTwo

  Scenario: Should does change name duplicate target in same pool
    Given a REST client authenticated as user1 with password ""
    And send GET /
    When request json body has:
      | name  | targetTwo |
      | pool  | Pool=poolOne |
    And send POST /target
    Then the response status is 201
    When request json body has:
      | name  | targetTwo |
    And send PATCH /target/1
    Then the response status is 409

  Scenario: Get Target
    Given a REST client authenticated as user1 with password ""
    And send GET /
    When send GET Target=targetOne
    Then the response status is 200
    And property name contains targetOne

  Scenario: Get null Target
    Given a REST client authenticated as user1 with password ""
    And send GET /
    When send GET Target=NULL
    Then the response status is 404

  Scenario: Delete Target
    Given a REST client authenticated as admin with password pass
    When request json body has:
      | statusDetailed  | statusdetailed |
      | source  | source1 |
      | target  | Target=targetOne |
    And send POST /healthstatus
    Then the response status is 201
    Given a REST client authenticated as user1 with password ""
    And send GET /
    When send DELETE Target=targetOne
    Then the response status is 204
    When send GET Target=targetOne
    Then the response status is 200
    And the response search at 'status.1' equal to DELETED
    When send GET Pool=poolOne
    Then the response status is 200
    When send GET Environment=envOne
    Then the response status is 200
    When send GET Project=projOne
    Then the response status is 200

  Scenario: Search Target by Name
    Given a REST client authenticated as user1 with password ""
    And send GET /
    When send GET /target/search/findByName?name=targetOne
    Then the response search at '_embedded.target[0].name' equal to targetOne

  Scenario: Search Target by NameContaining
    Given a REST client authenticated as user1 with password ""
    And send GET /
    When send GET /target/search/findByNameContaining?name=One
    Then the response search at '_embedded.target[0].name' equal to targetOne