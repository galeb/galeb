@ignore
Feature: RuleOrdered tests
  Background:
    Given a REST client authenticated as admin with password pass
    When request json body has:
      | name  | envOne |
    And send POST /environment
    Then the response status is 201
    Given a REST client authenticated as admin with password pass
    When request json body has:
      | name  | balancePolicyOne |
    And send POST /balancepolicy
    Then the response status is 201
    Given a REST client authenticated with token and role TEAM_ADMIN
    When request json body has:
      | name  | projOne |
      | teams | [Team=teamlocal] |
    And send POST /project
    Then the response status is 201
    When request json body has:
      | name  | ruleOne |
      | matching  | / |
      | project  | Project=projOne |
    And send POST /rule
    Then the response status is 201
    When request json body has:
      | name  | vhOne |
      | project  | Project=projOne |
      | environments  | [Environment=envOne] |
    And send POST /virtualhost
    Then the response status is 201

  Scenario: Conflict when create rule ordered without VirtualHostGroup
    Given a REST client authenticated with token and role TEAM_ADMIN
    When request json body has:
      | name  | roOne |
      | order | 1     |
      | rule  | Rule=ruleOne |
      | environment  | Environment=EnvOne |
    And send POST /ruleordered
    Then the response status is 409

  Scenario: Conflict when create rule ordered without Rule
    Given a REST client authenticated with token and role TEAM_ADMIN
    When request json body has:
      | name  | roOne |
      | order | 1     |
      | environment  | Environment=EnvOne |
      | virtualhostgroup | VirtualhostGroup=vhOne |
    And send POST /ruleordered
    Then the response status is 409

  Scenario: Conflict when create rule ordered without Environment
    Given a REST client authenticated with token and role TEAM_ADMIN
    When request json body has:
      | name  | roOne |
      | order | 1     |
      | rule  | Rule=ruleOne |
      | virtualhostgroup | VirtualhostGroup=vhOne |
    And send POST /ruleordered
    Then the response status is 409

  Scenario: Conflit when duplicate rule ordered
    Given a REST client authenticated with token and role TEAM_ADMIN
    When request json body has:
      | order | 1     |
      | rule  | Rule=ruleOne |
      | environment  | Environment=EnvOne |
      | virtualhostgroup | VirtualhostGroup=vhOne |
    And send POST /ruleordered
    Then the response status is 201
    When request json body has:
      | order | 1     |
      | rule  | Rule=ruleOne |
      | environment  | Environment=EnvOne |
      | virtualhostgroup | VirtualhostGroup=vhOne |
    And send POST /ruleordered
    Then the response status is 409

  Scenario: Should patch the field order
    Given a REST client authenticated with token and role TEAM_ADMIN
    When request json body has:
      | order | 1     |
      | rule  | Rule=ruleOne |
      | environment  | Environment=EnvOne |
      | virtualhostgroup | VirtualhostGroup=vhOne |
    And send POST /ruleordered
    When request json body has:
      | order | 2     |
    And send PATCH /ruleordered/1
    And send GET /ruleordered/1
    Then property order contains 2

  Scenario: Should patch the relationship rule
    Given a REST client authenticated with token and role TEAM_ADMIN
    When request json body has:
      | order | 1     |
      | rule  | Rule=ruleOne |
      | environment  | Environment=envOne |
      | virtualhostgroup | VirtualhostGroup=vhOne |
    And send POST /ruleordered
    When request json body has:
      | name  | ruleTwo |
      | matching  | / |
      | project  | Project=projOne |
    And send POST /rule
    Then the response status is 201
    When request json body has:
      | rule  | Rule=ruleTwo |
    And send PATCH /ruleordered/1
    And send GET /ruleordered/1/rule
    Then property name contains ruleTwo

  Scenario: Should patch the relationship environment
    Given a REST client authenticated as admin with password pass
    When request json body has:
      | name  | envTwo |
    And send POST /environment
    Then the response status is 201
    Given a REST client authenticated with token and role TEAM_ADMIN
    When request json body has:
      | order | 1     |
      | rule  | Rule=ruleOne |
      | environment  | Environment=envOne |
      | virtualhostgroup | VirtualhostGroup=vhOne |
    And send POST /ruleordered
    When request json body has:
      | environment  | Environment=envTwo |
    And send PATCH /ruleordered/1
    And send GET /ruleordered/1/environment
    Then property name contains envTwo

  Scenario: Should patch the relationship virtualhostgroup
    Given a REST client authenticated as admin with password pass
    When request json body has:
      | name  | vhTwo |
      | project  | Project=projOne |
      | environments  | [Environment=envOne] |
    And send POST /virtualhost
    Then the response status is 201
    Given a REST client authenticated with token and role TEAM_ADMIN
    When request json body has:
      | order | 1     |
      | rule  | Rule=ruleOne |
      | environment  | Environment=envOne |
      | virtualhostgroup | VirtualhostGroup=vhOne |
    And send POST /ruleordered
    When request json body has:
      | virtualhostgroup | VirtualhostGroup=vhTwo |
    And send PATCH /ruleordered/1
    And send GET /ruleordered/1/virtualhostgroup
    Then property id contains 2

  Scenario: Should delete roleordered
    Given a REST client authenticated with token and role TEAM_ADMIN
    When request json body has:
      | order | 1     |
      | rule  | Rule=ruleOne |
      | environment  | Environment=envOne |
      | virtualhostgroup | VirtualhostGroup=vhOne |
    And send POST /ruleordered
    Then the response status is 201
    And send DELETE /ruleordered/1
    Then the response status is 204
    And send GET /ruleordered/1
    Then the response status is 404