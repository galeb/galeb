@active
Feature: Validate permissions for create, update (specific and all fields), get and delete projects.
    Background: Creates three users: accountOne, accountTwo and accountSuperAdmin
                Creates also teamOne, teamTwo, projectOne and relationships as below:
                  accountOne -> teamOne -> projectOne
                  accountTwo -> teamTwo
      Given a REST client authenticated as user1 with password ""
      And send GET /
      Then the response status is 200
      When request json body has:
        | name     | teamOne              |
        | accounts         | [Account=user1]      |
      And send POST /team
      Then the response status is 201
      Given a REST client authenticated as user2 with password ""
      And send GET /
      Then the response status is 200
      When request json body has:
        | name     | teamTwo              |
        | accounts         | [Account=user2]      |
      And send POST /team
      Then the response status is 201
      Given a REST client authenticated as superadmin with password ""
      And send GET /
      Then the response status is 200
      Given a REST client authenticated as admin with password pass
      When request json body has:
        | accounts         | [Account=superadmin]      |
      And send PATCH RoleGroup=SUPER_ADMIN
      Then the response status is 200
      Given a REST client authenticated as user1 with password ""
      When request json body has:
        | name     | projectOne              |
        | teams         | [Team=teamOne]      |
      And send POST /project
      Then the response status is 201

    Scenario Outline: Accounts create project with rolegroup default (obs.: with accountTwo, the error is 400. Investigate it.)
      Given a REST client authenticated as <user> with password ""
      When request json body has:
        | name     | projectTwo              |
        | teams         | [Team=teamOne]      |
      And send POST /project
      Then the response status is <status>
      Examples:
        | user               | status |
        | user1         | 201    |
        | user2         | 400    |
        | superadmin    | 201    |

    Scenario Outline: Accounts view project with rolegroup default
      Given a REST client authenticated as <user> with password ""
      When send GET /project/1
      Then the response status is <status>
      Examples:
        | user          | status |
        | user1         | 200    |
        | user2         | 403    |
        | superadmin    | 200    |

    Scenario Outline: Accounts update all field project with rolegroup default
      Given a REST client authenticated as <user> with password ""
      When request json body has:
        | name     | projectOne              |
        | teams         | [Team=teamOne]      |
      And send PUT /project/1
      Then the response status is <status>
      Examples:
        | user               | status |
        | user1         | 200    |
        | user2         | 403    |
        | superadmin         | 200    |

    Scenario Outline: Accounts update specific field project with rolegroup default
      Given a REST client authenticated as <user> with password ""
      When request json body has:
        | name     | projectOne              |
      And send PATCH /project/1
      Then the response status is <status>
      Examples:
        | user               | status |
        | user1         | 200    |
        | user2         | 403    |
        | superadmin         | 200    |

    Scenario Outline: Accounts delete project with rolegroup default
      Given a REST client authenticated as <user> with password  ""
      And send DELETE /project/1
      Then the response status is <status>
      Examples:
        | user               | status |
        | user1         | 204    |
        | user2         | 403    |
        | superadmin         | 204    |