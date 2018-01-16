@team
@ignore
Feature: Team Support
    The manager have than
    to support REST standard

    Background:
        Given a REST client authenticated as admin
        When request json body has:
            | name | one |
        And send POST /team
        Then the response status is 201
        And a REST client authenticated as admin
        When request json body has:
            | name     | accountOne                  |
            | password | password                    |
            | email    | accOne@fake.com             |
            | roles    | [ ROLE_USER ]               |
            | teams    | [ http://localhost/team/1 ] |
        And send POST /account
        Then the response status is 201

    Scenario: Create duplicated Team
        Given a REST client authenticated as admin
        When request json body has:
            | name | one |
        And send POST /team
        Then the response status is 409

    Scenario: Get Team
        Given a REST client authenticated as accountOne
        When send GET /team/1
        Then the response status is 200
        And property name contains one

    Scenario: Get null Team
        Given a REST client authenticated as admin
        When send GET /team/2
        Then the response status is 404

    Scenario: Update Team
        Given a REST client authenticated as admin
        When request json body has:
            | name | two |
        And send PUT /team/1
        Then the response status is 204
        And a REST client authenticated as admin
        When send GET /team/1
        Then the response status is 200
        And property name contains two

    Scenario: Update one field of Team
        Given a REST client authenticated as admin
        When request json body has:
            | name | two |
        And send PATCH /team/1
        Then the response status is 204
        And a REST client authenticated as admin
        When send GET /team/1
        Then the response status is 200
        And property name contains two

    Scenario: Delete Team
        Given a REST client authenticated as admin
        When send DELETE /team/1
        Then the response status is 204
        And a REST client authenticated as admin
        When send GET /account/1
        Then the response status is 200
        And property name contains accountOne

    Scenario: Search Team by Name
        Given a REST client authenticated as admin
        When send GET /team/search/findByName?name=one
        Then the response search at '_embedded.team[0].name' equal to one

    Scenario: Search Team by NameContaining
        Given a REST client authenticated as admin
        When send GET /team/search/findByNameContaining?name=n
        Then the response search at '_embedded.team[0].name' equal to one
