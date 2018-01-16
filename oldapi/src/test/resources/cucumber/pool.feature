@pool
@ignore
Feature: Pool Support
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
            | name     | accountOne       |
            | password | password         |
            | roles    | [ ROLE_USER ]    |
            | teams    | [ Team=teamOne ] |
            | email    | test@fake.com    |
        And send POST /account
        Then the response status is 201
        And a REST client authenticated as accountOne
        When request json body has:
            | name  | projOne          |
            | teams | [ Team=teamOne ] |
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
        And request json body has:
            | name        | poolOne                |
            | environment | Environment=envOne     |
            | project     | Project=projOne        |
        And send POST /pool

    Scenario: Create Pool
        Then the response status is 201

    Scenario: Create duplicated Pool
        Given a REST client authenticated as accountOne
        When request json body has:
            | name        | poolOne                |
            | environment | Environment=envOne     |
            | project     | Project=projOne        |
        And send POST /pool
        Then the response status is 409

    Scenario: Get Pool
        Given a REST client authenticated as accountOne
        When send GET Pool=poolOne
        Then the response status is 200
        And property name contains poolOne

    Scenario: Get null Pool
        Given a REST client authenticated as accountOne
        When send GET Pool=NULL
        Then the response status is 404

    Scenario: Update Pool
        Given a REST client authenticated as accountOne
        When request json body has:
            | name        | poolOne            |
            | environment | Environment=envOne |
            | project     | Project=projOne    |
        And send PUT Pool=poolOne
        Then the response status is 204
        And a REST client authenticated as accountOne
        When send GET Pool=poolOne
        Then the response status is 200
        And property name contains poolOne

    Scenario: Update name field of Pool
        Given a REST client authenticated as accountOne
        When request json body has:
            | name | poolTwo |
        And send PATCH Pool=poolOne
        Then the response status is 204
        And a REST client authenticated as accountOne
        When send GET Pool=poolTwo
        Then the response status is 200
        And property name contains poolTwo

    Scenario: Delete Pool
        Given a REST client authenticated as accountOne
        When send DELETE Pool=poolOne
        Then the response status is 204
        And a REST client authenticated as accountOne
        When send GET Pool=poolOne
        Then the response status is 404
        And a REST client authenticated as accountOne
        When send GET Environment=envOne
        Then the response status is 200
        And a REST client authenticated as accountOne
        When send GET Project=projOne
        Then the response status is 200

    Scenario: Invalid environment return Bad Request
        Given a REST client authenticated as admin
        When request json body has:
          | name | envTwo |
        And send POST /environment
        Then the response status is 201
        And a REST client authenticated as accountOne
        And request json body has:
          | name        | poolTwo            |
          | environment | Environment=envTwo |
          | project     | Project=projOne    |
        And send POST /pool
        Then the response status is 400

    Scenario: Search Pool by Name
        Given a REST client authenticated as admin
        When send GET /pool/search/findByName?name=poolOne
        Then the response search at '_embedded.pool[0].name' equal to poolOne

    Scenario: Search Pool by NameContaining
        Given a REST client authenticated as admin
        When send GET /pool/search/findByNameContaining?name=One
        Then the response search at '_embedded.pool[0].name' equal to poolOne

    Scenario: Search Pool by findByFarmId
        Given a REST client authenticated as admin
        When send GET /pool/search/findByName?name=poolOne
        Then the response search at '_embedded.pool[0].name' equal to poolOne

    Scenario: find Especial Pool NoParent
        Given a REST client authenticated as admin
        When send GET /pool/search/findByNameContaining?name=One
        Then the response search at '_embedded.pool[0].name' equal to poolOne
