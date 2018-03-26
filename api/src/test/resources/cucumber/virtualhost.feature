@active
Feature: VirtualHost Support
  The manager have than
  to support REST standard

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
    When request json body has:
      | name        | one                |
      | environments | [Environment=envOne] |
      | project     | Project=projOne    |
    And send POST /virtualhost
    Then the response status is 201

    Scenario: Create duplicated Virtualhost
      Given a REST client authenticated as user1 with password ""
      And send GET /
      When request json body has:
        | name        | one                |
        | environments | [Environment=envOne] |
        | project     | Project=projOne    |
      And send POST /virtualhost
      Then the response status is 409

    Scenario: Get VirtualHost
      Given a REST client authenticated as user1 with password ""
      And send GET /
      When send GET /virtualhost/1
      Then the response status is 200
      And property name contains one

#    Scenario: Get null VirtualHost
#      Given a REST client authenticated with token and role TEAM_ADMIN
#      When send GET /virtualhost/999
#      Then the response status is 404

    Scenario: Update VirtualHost
      Given a REST client authenticated as user1 with password ""
      And send GET /
      When request json body has:
        | name        | two                |
        | environments | [Environment=envOne] |
        | project     | Project=projOne    |
      And send PUT /virtualhost/1
      Then the response status is 200
      When send GET /virtualhost/1
      Then the response status is 200
      And property name contains two

    Scenario: Update name field of VirtualHost
      Given a REST client authenticated as user1 with password ""
      And send GET /
      When request json body has:
        | name | two |
      And send PATCH /virtualhost/1
      Then the response status is 200
      When send GET /virtualhost/1
      Then the response status is 200
      And property name contains two

    Scenario: Update project field of VirtualHost (name update is ignored)
      Given a REST client authenticated as user1 with password ""
      And send GET /
      When request json body has:
        | name  | projTwo |
        | teams | [Team=teamlocal] |
      And send POST /project
      Then the response status is 201
      When request json body has:
        | project | Project=projTwo |
      And send PATCH /virtualhost/1
      Then the response status is 200
      When send GET /virtualhost/1
      Then the response status is 200
      And property name contains one

    Scenario: Delete VirtualHost
      Given a REST client authenticated as user1 with password ""
      And send GET /
      When send DELETE /virtualhost/1
      Then the response status is 204
      When send GET /virtualhost/1
      Then the response status is 200
      Then the response search at 'status.1' equal to DELETED
      When send GET Environment=envOne
      Then the response status is 200
      And property name contains envOne
      When send GET Project=projOne
      Then the response status is 200
      And property name contains projOne

    Scenario: Invalid environment return Bad Request
      Given a REST client authenticated as user1 with password ""
      And send GET /
      When request json body has:
        | name        | three           |
        | environments | [Environment=two] |
        | project     | Project=projOne |
      And send POST /virtualhost
      Then the response status is 409

    Scenario: Search VirtualHost by Name
      Given a REST client authenticated as user1 with password ""
      And send GET /
      When send GET /virtualhost/search/findByName?name=one
      Then the response search at '_embedded.virtualhost[0].name' equal to one

    Scenario: Search VirtualHost by NameContaining
      Given a REST client authenticated as user1 with password ""
      And send GET /
      When send GET /virtualhost/search/findByNameContaining?name=n
      Then the response search at '_embedded.virtualhost[0].name' equal to one