@virtualhost
@ignore
Feature: VirtualHost Support
    The manager have than
    to support REST standard

    Background:
        Given a REST client authenticated as admin
        When request json body has:
            | name | envOne |
        And send POST /environment
        Then the response status is 201
        And a REST client authenticated as admin
        When request json body has:
            | name | teamOne |
        And send POST /team
        Then the response status is 201
        And a REST client authenticated as admin
        When request json body has:
            | name     | accountOne                  |
            | password | password                    |
            | roles    | [ ROLE_USER ]               |
            | teams    | [ http://localhost/team/1 ] |
            | email    | test@fake.com               |
        And send POST /account
        Then the response status is 201
        And a REST client authenticated as admin
        When request json body has:
            | name  | projOne                     |
            | teams | [ http://localhost/team/1 ] |
        And send POST /project
        Then the response status is 201
        And a REST client authenticated as admin
        When request json body has:
            | name  | projTwo                     |
            | teams | [ http://localhost/team/1 ] |
        And send POST /project
        Then the response status is 201
        And a REST client authenticated as admin
        When request json body has:
            | name | providerOne |
        And send POST /provider
        Then the response status is 201
        And a REST client authenticated as admin
        When request json body has:
            | name        | farmOne              |
            | domain      | domain               |
            | api         | api                  |
            | environment | Environment=envOne   |
            | provider    | Provider=providerOne |
        And send POST /farm
        Then the response status is 201
        And a REST client authenticated as accountOne
        When request json body has:
            | name        | one                |
            | environment | Environment=envOne |
            | project     | Project=projOne    |
        And send POST /virtualhost
        Then the response status is 201

    Scenario: Create duplicated Virtualhost
        Given a REST client authenticated as accountOne
        When request json body has:
            | name        | one                |
            | environment | Environment=envOne |
            | project     | Project=projOne    |
        And send POST /virtualhost
        Then the response status is 409

    Scenario: Create duplicated alias name (1)
        Given a REST client authenticated as accountOne
        When request json body has:
            | name        | three              |
            | aliases     | [ one ]            |
            | environment | Environment=envOne |
            | project     | Project=projOne    |
        And send POST /virtualhost
        Then the response status is 400

    Scenario: Create duplicated alias name (2)
        Given a REST client authenticated as accountOne
        When request json body has:
            | name        | four               |
            | aliases     | [ four ]         |
            | environment | Environment=envOne |
            | project     | Project=projOne    |
        And send POST /virtualhost
        Then the response status is 400

    Scenario: Get VirtualHost
        Given a REST client authenticated as accountOne
        When send GET /virtualhost/1
        Then the response status is 200
        And property name contains one

    Scenario: Get null VirtualHost
        Given a REST client authenticated as accountOne
        When send GET /virtualhost/999
        Then the response status is 404

    Scenario: Update VirtualHost
        Given a REST client authenticated as accountOne
        When request json body has:
            | name        | one                |
            | environment | Environment=envOne |
            | project     | Project=projOne    |
        And send PUT /virtualhost/1
        Then the response status is 204
        And a REST client authenticated as accountOne
        When send GET /virtualhost/1
        Then the response status is 200
        And property name contains one

    Scenario: Update name field of VirtualHost
        Given a REST client authenticated as accountOne
        When request json body has:
            | name | one |
        And send PATCH /virtualhost/1
        Then the response status is 204
        And a REST client authenticated as accountOne
        When send GET /virtualhost/1
        Then the response status is 200
        And property name contains one

    Scenario: Update project field of VirtualHost (name update is ignored)
        Given a REST client authenticated as accountOne
        When request json body has:
            | project | Project=projTwo |
        And send PATCH /virtualhost/1
        Then the response status is 204
        And a REST client authenticated as accountOne
        When send GET /virtualhost/1
        Then the response status is 200
        And property name contains one

    Scenario: Delete VirtualHost
        Given a REST client authenticated as accountOne
        When send DELETE /virtualhost/1
        Then the response status is 204
        And a REST client authenticated as accountOne
        When send GET /virtualhost/1
        Then the response status is 404
        And a REST client authenticated as accountOne
        When send GET Environment=envOne
        Then the response status is 200
        And property name contains envOne
        And a REST client authenticated as accountOne
        When send GET Project=projOne
        Then the response status is 200
        And property name contains projOne

    Scenario: Invalid environment return Bad Request
        Given a REST client authenticated as admin
        When request json body has:
          | name | two |
        And send POST /environment
        Then the response status is 201
        And a REST client authenticated as accountOne
        When request json body has:
          | name        | three           |
          | environment | Environment=two |
          | project     | Project=projOne |
        And send POST /virtualhost
        Then the response status is 400

    Scenario: Search VirtualHost by Name
        Given a REST client authenticated as admin
        When send GET /virtualhost/search/findByName?name=one
        Then the response search at '_embedded.virtualhost[0].name' equal to one

    Scenario: Search VirtualHost by NameContaining
        Given a REST client authenticated as admin
        When send GET /virtualhost/search/findByNameContaining?name=n
        Then the response search at '_embedded.virtualhost[0].name' equal to one

