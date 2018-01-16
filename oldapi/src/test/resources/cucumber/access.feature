@access
@ignore
Feature: Access policy
    An account must have access to resources
    in accordance with established policy

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

    Scenario: AccountOne can create ProjOne
        Given a REST client authenticated as accountOne
        When request json body has:
            | name  | projOne                     |
            | teams | [ http://localhost/team/1 ] |
        And send POST /project
        Then the response status is 201

    Scenario: AccountOne can access ProjOne
        Given a REST client authenticated as accountOne
        When request json body has:
            | name  | projOne                     |
            | teams | [ http://localhost/team/1 ] |
        And send POST /project
        And a REST client authenticated as accountOne
        And send GET Project=projOne
        Then the response status is 200
        And property name contains projOne

    Scenario: AccountTwo can't access ProjOne
        Given a REST client authenticated as accountOne
        When request json body has:
            | name  | projOne                     |
            | teams | [ http://localhost/team/1 ] |
        And send POST /project
        And a REST client authenticated as accountTwo
        And send GET Project=projOne
        Then the response status is 404
