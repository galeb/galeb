@ruletype
@ignore
Feature: RuleType Support
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
            | name | rTypeOne |
        And send POST /ruletype

    Scenario: Create RuleType
        Then the response status is 201

    Scenario: Create duplicated RuleType
        Given a REST client authenticated as admin
        When request json body has:
            | name | rTypeOne |
        And send POST /ruletype
        Then the response status is 409

    Scenario: Get RuleType
        Given a REST client authenticated as accountOne
        When send GET /ruletype/1
        Then the response status is 200
        And property name contains rTypeOne

    Scenario: Get null RuleType
        Given a REST client authenticated as accountOne
        When send GET /ruletype/2
        Then the response status is 404

    Scenario: Update RuleType
        Given a REST client authenticated as admin
        When request json body has:
            | name | rTypeTwo |
        And send PUT /ruletype/1
        Then the response status is 204
        And a REST client authenticated as admin
        When send GET /ruletype/1
        Then the response status is 200
        And property name contains rTypeTwo

    Scenario: Update one field of RuleType
        Given a REST client authenticated as admin
        When request json body has:
            | name | rTypeThree |
        And send PATCH /ruletype/1
        Then the response status is 204
        And a REST client authenticated as admin
        When send GET /ruletype/1
        Then the response status is 200
        And property name contains rTypeThree

    Scenario: Delete RuleType
        Given a REST client authenticated as admin
        When send DELETE /ruletype/1
        Then the response status is 204
        And a REST client authenticated as admin
        When send GET /ruletype/1
        Then the response status is 404

    Scenario: Search RuleType by Name
        Given a REST client authenticated as admin
        When send GET /ruletype/search/findByName?name=rTypeOne
        Then the response search at '_embedded.ruletype[0].name' equal to rTypeOne

    Scenario: Search RuleType by NameContaining
        Given a REST client authenticated as admin
        When send GET /ruletype/search/findByNameContaining?name=One
        Then the response search at '_embedded.ruletype[0].name' equal to rTypeOne
