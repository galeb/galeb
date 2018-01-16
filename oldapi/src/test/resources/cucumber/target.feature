@target
@ignore
Feature: Target Support
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
            | name        | poolOne            |
            | environment | Environment=envOne |
            | project     | Project=projOne    |
        And send POST /pool
        Then the response status is 201
        And a REST client authenticated as accountOne
        And request json body has:
            | name        | targetOne          |
            | parent      | Pool=poolOne       |
        And send POST /target

    Scenario: Create Target
        Then the response status is 201

    Scenario: Target with Parent
        Given a REST client authenticated as accountOne
        When send GET /target/1/environment/1
        Then the response status is 200
        And property name contains envOne
        And a REST client authenticated as accountOne
        When send GET /target/1/project/1
        Then the response status is 200
        And property name contains projOne

    Scenario: Create Target with Parent and Project inconsistent
        Given a REST client authenticated as accountOne
        When request json body has:
            | name  | projTwo          |
            | teams | [ Team=teamOne ] |
        And send POST /project
        Then the response status is 201
        And a REST client authenticated as accountOne
        And request json body has:
            | name       | newTargetTwo    |
            | parent     | Pool=poolOne    |
            | project    | Project=projTwo |
        And send POST /target
        Then the response status is 400

    Scenario: Create Target with Parent and Environment inconsistent
        Given a REST client authenticated as admin
        When request json body has:
            | name  | envTwo |
        And send POST /environment
        Then the response status is 201
        And a REST client authenticated as accountOne
        And request json body has:
            | name        | newTargetTwo       |
            | parent      | Pool=poolOne       |
            | environment | Environment=envTwo |
        And send POST /target
        Then the response status is 400

    Scenario: Create duplicated Target without parent
        Given a REST client authenticated as accountOne
        When request json body has:
            | name        | targetTwo          |
            | environment | Environment=envOne |
            | project     | Project=projOne    |
        And send POST /target
        Then the response status is 201
        And a REST client authenticated as accountOne
        When request json body has:
            | name        | targetTwo          |
            | environment | Environment=envOne |
            | project     | Project=projOne    |
        And send POST /target
        Then the response status is 409

    Scenario: Create duplicated Target with parent
        Given a REST client authenticated as accountOne
        When request json body has:
            | name        | targetTwo    |
            | parent      | Pool=poolOne |
        And send POST /target
        Then the response status is 201
        And a REST client authenticated as accountOne
        When request json body has:
            | name        | targetTwo    |
            | parent      | Pool=poolOne |
        And send POST /target
        Then the response status is 409

    Scenario: Get Target
        Given a REST client authenticated as accountOne
        When send GET Target=targetOne
        Then the response status is 200
        And property name contains targetOne

    Scenario: Get null Target
        Given a REST client authenticated as accountOne
        When send GET Target=NULL
        Then the response status is 404

    Scenario: Update Target
        Given a REST client authenticated as accountOne
        When request json body has:
            | name        | targetTwo       |
            | parent      | Pool=poolOne    |
        And send PUT Target=targetOne
        Then the response status is 204
        And a REST client authenticated as accountOne
        When send GET Target=targetTwo
        Then the response status is 200

    Scenario: Update name field of Target
        Given a REST client authenticated as accountOne
        When request json body has:
            | name | targetTwo |
        And send PATCH Target=targetOne
        Then the response status is 204
        And a REST client authenticated as accountOne
        When send GET Target=targetTwo
        Then the response status is 200
        And property name contains targetTwo

    Scenario: Delete Target
        Given a REST client authenticated as accountOne
        When send DELETE Target=targetOne
        Then the response status is 204
        And a REST client authenticated as accountOne
        When send GET Target=targetOne
        Then the response status is 404
        And a REST client authenticated as accountOne
        When send GET Pool=poolOne
        Then the response status is 200
        And a REST client authenticated as accountOne
        When send GET Environment=envOne
        Then the response status is 200
        And a REST client authenticated as accountOne
        When send GET Project=projOne
        Then the response status is 200

    Scenario: Add a new parent to a target
        Given a REST client authenticated as accountOne
        And request json body has:
            | name        | parentTwo          |
            | environment | Environment=envOne |
            | project     | Project=projOne    |
        And send POST /pool
        Then the response status is 201
        And a REST client authenticated as accountOne
        When request json body has:
            | parent | Pool=poolTwo |
        And send PATCH Target=targetOne
        Then the response status is 204
        And a REST client authenticated as accountOne
        When send GET /pool/2/targets/1
        Then the response status is 200
        And property name contains targetOne

    Scenario: Invalid environment is Forbidden
        Given a REST client authenticated as admin
        When request json body has:
          | name | envTwo |
        And send POST /environment
        Then the response status is 201
        And a REST client authenticated as accountOne
        And request json body has:
          | name        | targetTwo          |
          | parent      | Pool=poolOne       |
          | environment | Environment=envTwo |
          | project     | Project=projOne    |
        And send POST /target
        Then the response status is 400

    Scenario: Search Target by Name
        Given a REST client authenticated as admin
        When send GET /target/search/findByName?name=targetOne
        Then the response search at '_embedded.target[0].name' equal to targetOne

    Scenario: Search Target by NameContaining
        Given a REST client authenticated as admin
        When send GET /target/search/findByNameContaining?name=One
        Then the response search at '_embedded.target[0].name' equal to targetOne
