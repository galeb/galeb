Feature: Validate permissions for create, update (specific and all fields), get and delete projects.
    Background: Creates three users: accountOne, accountTwo and accountSuperAdmin
                Creates also teamOne, teamTwo, projectOne and relationships as below:
                  accountOne -> teamOne -> projectOne
                  accountTwo -> teamTwo
      Given a REST client authenticated as admin with password pass
      When request json body has:
        | username     | accountOne              |
        | password | password                    |
        | email    | accountone@test.com               |
      And send POST /account
      When request json body has:
        | name     | teamOne              |
        | accounts         | [Account=accountOne]      |
      And send POST /team
      Then the response status is 201
      When request json body has:
        | username     | accountTwo              |
        | password | password                    |
        | email    | accounttwo@test.com               |
      And send POST /account
      Then the response status is 201
      When request json body has:
        | username     | accountSuperAdmin              |
        | password | password                    |
        | email    | accountsuperadmin@test.com               |
      And send POST /account
      Then the response status is 201
      When request json body has:
        | name     | teamTwo              |
        | accounts         | [Account=accountTwo]      |
      And send POST /team
      Then the response status is 201
      When request json body has:
        | accounts         | [Account=accountSuperAdmin]      |
      And send PATCH RoleGroup=SUPER_ADMIN
      Then the response status is 200
      Given a REST client authenticated as accountOne with token
      When request json body has:
        | name     | projectOne              |
        | teams         | [Team=teamOne]      |
      And send POST /project
      Then the response status is 201
      Given a REST client authenticated as admin with password pass
      When request json body has:
        | teams     | [Team=teamOne,Team=teamTwo] |
      And send PATCH RoleGroup=TEAM_DEFAULT
      Then the response status is 200

    Scenario Outline: Accounts create project with rolegroup default (obs.: with accountTwo, the error is 400. Investigate it.)
      Given a REST client authenticated as <user> with token
      When request json body has:
        | name     | projectTwo              |
        | teams         | [Team=teamOne]      |
      And send POST /project
      Then the response status is <status>
      Examples:
        | user               | status |
        | accountOne         | 201    |
        | accountTwo         | 400    |
        | accountSuperAdmin  | 201    |

    Scenario Outline: Accounts view project with rolegroup default
      Given a REST client authenticated as <user> with token
      When send GET /project/1
      Then the response status is <status>
      Examples:
        | user               | status |
        | accountOne         | 200    |
        | accountTwo         | 403    |
        | accountSuperAdmin  | 200    |

    Scenario Outline: Accounts update all field project with rolegroup default
      Given a REST client authenticated as <user> with token
      When request json body has:
        | name     | projectOne              |
        | teams         | [Team=teamOne]      |
      And send PUT /project/1
      Then the response status is <status>
      Examples:
        | user               | status |
        | accountOne         | 200    |
        | accountTwo         | 403    |
        | accountSuperAdmin  | 200    |

    Scenario Outline: Accounts update specific field project with rolegroup default
      Given a REST client authenticated as <user> with token
      When request json body has:
        | name     | projectOne              |
      And send PATCH /project/1
      Then the response status is <status>
      Examples:
        | user               | status |
        | accountOne         | 200    |
        | accountTwo         | 403    |
        | accountSuperAdmin  | 200    |

    Scenario Outline: Accounts delete project with rolegroup default
      Given a REST client authenticated as <user> with token
      And send DELETE /project/1
      Then the response status is <status>
      Examples:
        | user               | status |
        | accountOne         | 204    |
        | accountTwo         | 403    |
        | accountSuperAdmin  | 204    |