@environment
@ignore
Feature: Environment Support
    The manager have than
    to support REST standard

    Background:
        Given a REST client authenticated as admin
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
            | name | envOne |
        And send POST /environment

    Scenario: Create Environment
        Then the response status is 201

    Scenario: Create duplicated Environment
        Given a REST client authenticated as admin
        When request json body has:
            | name | envOne |
        And send POST /environment
        Then the response status is 409

    Scenario: Get Environment
        Given a REST client authenticated as accountOne
        When send GET Environment=envOne
        Then the response status is 200
        And property name contains envOne

    Scenario: Get null Environment
        Given a REST client authenticated as accountOne
        When send GET Environment=NULL
        Then the response status is 404

    Scenario: Update Environment
        Given a REST client authenticated as admin
        When request json body has:
            | name | envTwo |
        And send PUT Environment=envOne
        Then the response status is 204
        And a REST client authenticated as admin
        When send GET Environment=envTwo
        Then the response status is 200
        And property name contains envTwo

    Scenario: Update one field of Environment
        Given a REST client authenticated as admin
        When request json body has:
            | name | envThree |
        And send PATCH Environment=envOne
        Then the response status is 204
        And a REST client authenticated as admin
        When send GET Environment=envThree
        Then the response status is 200
        And property name contains envThree

    Scenario: Delete Environment
        Given a REST client authenticated as admin
        When send DELETE Environment=envOne
        Then the response status is 204
        And a REST client authenticated as admin
        When send GET Environment=envOne
        Then the response status is 404

    Scenario: Search Environment by Name
        Given a REST client authenticated as admin
        When send GET /environment/search/findByName?name=envOne
        Then the response search at '_embedded.environment[0].name' equal to envOne

    Scenario: Search Environment by NameContaining
        Given a REST client authenticated as admin
        When send GET /environment/search/findByNameContaining?name=One
        Then the response search at '_embedded.environment[0].name' equal to envOne
