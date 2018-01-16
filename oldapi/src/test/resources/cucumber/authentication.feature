@authentication
@ignore
Feature: Authentication work
    An account should only access to resources
    if authenticated.

    Background:
        Given a REST client authenticated as admin
        When request json body has:
            | name  | teamOne |
        And send POST /team
        And a REST client authenticated as admin
        When request json body has:
            | name  | teamTwo |
        And send POST /team
        And a REST client authenticated as admin
        When request json body has:
            | name     | accountOne                  |
            | password | password                    |
            | email    | accOne@fake.local           |
            | roles    | [ ROLE_USER ]               |
            | teams    | [ http://localhost/team/1 ] |
        And send POST /account
        And a REST client authenticated as admin
        When request json body has:
            | name     | accountTwo                  |
            | password | password                    |
            | email    | accTwo@fake.local           |
            | roles    | [ ROLE_USER ]               |
            | teams    | [ http://localhost/team/2 ] |
        And send POST /account
        And a REST client authenticated as admin
        When request json body has:
            | name     | adminOne                    |
            | password | password                    |
            | email    | accTwo@fake.local           |
            | roles    | [ ROLE_USER, ROLE_ADMIN ]   |
            | teams    | [ http://localhost/team/1 ] |
        And send POST /account

    Scenario: AccountOne can access your own account
        Given a REST client authenticated as accountOne
        And send GET /account/1
        Then the response status is 200

    Scenario: A nonexistent Account can not access any restricted resource
        Given a REST client authenticated as accountThree
        And send GET /account/1
        Then the response status is 401

    Scenario: An unauthenticated request can not access any restricted resource
        Given a REST client unauthenticated
        And send GET /account/1
        Then the response status is 401

    Scenario: Admins can change their roles
        Given a REST client authenticated as adminOne
        When request json body has:
            | roles | [ ROLE_USER ] |
        And send PATCH Account=adminOne
        Then the response status is 204

    Scenario: Common users can't change their owers roles
        Given a REST client authenticated as accountOne
        When request json body has:
            | roles | [ ROLE_USER, ROLE_ADMIN ] |
        And send PATCH Account=accountOne
        Then the response status is 403

    Scenario: Admins can enter new teams for yourself
        Given a REST client authenticated as adminOne
        When request json body has:
            | teams | [ Team=teamOne, Team=teamTwo ] |
        And send PATCH Account=adminOne
        Then the response status is 204

    Scenario: Common users can't enter new teams for yourself
        Given a REST client authenticated as accountOne
        When request json body has:
            | teams | [ Team=teamOne, Team=teamTwo ] |
        And send PATCH Account=accountOne
        Then the response status is 403

