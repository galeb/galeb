@account
@ignore
Feature: Account Support
    The manager have than
    to support REST standard

    Background:
        Given a REST client authenticated as admin
        When request json body has:
            | name  | teamOne |
        And send POST /team
        Then the response status is 201
        And a REST client authenticated as admin
        When request json body has:
            | name  | teamAdmin |
        And send POST /team
        Then the response status is 201
        And a REST client authenticated as admin
        When request json body has:
            | name     | adminOne                    |
            | password | password                    | 
            | email    | adminone@test.com           |
            | roles    | [ ROLE_USER, ROLE_ADMIN ]   |
            | teams    | [ http://localhost/team/2 ] |
        And send POST /account
        Then the response status is 201
        And a REST client authenticated as adminOne
        When request json body has:
            | name     | accountOne                  |
            | password | password                    |
            | email    | test@test.com               |
            | roles    | [ ROLE_USER ]               |
            | teams    | [ http://localhost/team/1 ] |
        And send POST /account
        Then the response status is 201
        And a REST client authenticated as adminOne
        When request json body has:
            | name     | accountTwo                  |
            | password | password                    |
            | email    | accountTwo@test.com         |
            | roles    | [ ROLE_USER ]               |
            | teams    | [ http://localhost/team/1 ] |
        And send POST /account
        Then the response status is 201

    Scenario: Create Account
        Then the response status is 201

    Scenario: Create duplicated Account
        Given a REST client authenticated as adminOne
        When request json body has:
            | name     | accountOne                  |
            | password | password                    |
            | email    | test@test.com               |
            | roles    | [ ROLE_USER ]               |
            | teams    | [ http://localhost/team/1 ] |
        And send POST /account
        Then the response status is 409

    Scenario: Get Account
        Given a REST client authenticated as adminOne
        When send GET /account/1
        Then the response status is 200
        And property name contains adminOne

    Scenario: Get null Account
        Given a REST client authenticated as adminOne
        When send GET /account/4
        Then the response status is 404

    Scenario: Update Account name
        Given a REST client authenticated as adminOne
        When request json body has:
            | name     | accountThree                |
            | password | password                    |
            | email    | test3@teste.com             |
            | roles    | [ ROLE_USER ]               |
            | teams    | [ http://localhost/team/1 ] |
        And send PUT /account/2
        Then the response status is 204
        And a REST client authenticated as adminOne
        When send GET /account/2
        Then the response status is 200
        And property name contains accountThree

    Scenario: Update Account email
        Given a REST client authenticated as adminOne
        When request json body has:
            | name     | accountOne                  |
            | password | password                    |
            | email    | accountTwo@test.com         |
            | roles    | [ ROLE_USER ]               |
            | teams    | [ http://localhost/team/1 ] |
        And send PUT /account/2
        Then the response status is 204
        And a REST client authenticated as adminOne
        When send GET /account/2
        Then the response status is 200
        And property email contains accountTwo@test.com

    Scenario: Update one field of Account
        Given a REST client authenticated as adminOne
        When request json body has:
            | name | accountThree |
        And send PATCH /account/2
        Then the response status is 204
        And a REST client authenticated as adminOne
        When send GET /account/2
        Then the response status is 200
        And property name contains accountThree

    Scenario: Delete Account
        Given a REST client authenticated as adminOne
        When send DELETE /account/2
        Then the response status is 204
        And  a REST client authenticated as adminOne
        When send GET /account/2
        Then the response status is 404

    Scenario: Get Account as its owner
        Given a REST client authenticated as accountOne
        When send GET /account/2
        Then the response status is 200
        And property name contains accountOne

    Scenario: Get null Account as its owner
        Given a REST client authenticated as accountOne
        When send GET /account/4
        Then the response status is 403

    Scenario: Update Account name as its owner is not permitted
        Given a REST client authenticated as accountOne
        When request json body has:
            | name     | accountTwo                  |
            | password | password                    |
            | email    | test2@test.com              |
            | roles    | [ ROLE_USER ]               |
            | teams    | [ http://localhost/team/1 ] |
        And send PUT /account/2
        Then the response status is 403
        And a REST client authenticated as adminOne
        When send GET /account/2
        Then the response status is 200
        And property name contains accountOne

    Scenario: Update field name of Account as its owner is not permitted
        Given a REST client authenticated as accountOne
        When request json body has:
            | name | accountTwo |
        And send PATCH /account/2
        Then the response status is 403
        And a REST client authenticated as adminOne
        When send GET /account/2
        Then the response status is 200
        And property name contains accountOne

    Scenario: Delete Account as its owner is not permitted
        Given a REST client authenticated as accountOne
        When send DELETE /account/2
        Then the response status is 403
        And  a REST client authenticated as adminOne
        When send GET /account/2
        Then the response status is 200

    Scenario: Create Account as anonymous is not permitted
        Given a REST client unauthenticated
        When request json body has:
            | name     | accountTwo                  |
            | password | password                    |
            | email    | test2@test.com              |
            | roles    | [ ROLE_USER ]               |
            | teams    | [ http://localhost/team/1 ] |
        And send POST /account
        Then the response status is 401

    Scenario: Get Account as anonymous is not permitted
        Given a REST client unauthenticated
        When send GET /account/2
        Then the response status is 401

    Scenario: Update Account name as anonymous is not permitted
        Given a REST client unauthenticated
        When request json body has:
            | name     | accountTwo                  |
            | password | password                    |
            | email    | test2@teste.com             |
            | roles    | [ ROLE_USER ]               |
            | teams    | [ http://localhost/team/1 ] |
        And send PUT /account/2
        Then the response status is 401

    Scenario: Update Account email as anonymous is not permitted
        Given a REST client unauthenticated
        When request json body has:
            | name     | accountOne                  |
            | password | password                    |
            | email    | accountTwo@test.com         |
            | roles    | [ ROLE_USER ]               |
            | teams    | [ http://localhost/team/1 ] |
        And send PUT /account/2
        Then the response status is 401

    Scenario: Update field name of Account as anonymous is not permitted
        Given a REST client unauthenticated
        When request json body has:
            | name | accountTwo |
        And send PATCH /account/2
        Then the response status is 401

    Scenario: Delete Account as anonymous is not permitted
        Given a REST client unauthenticated
        When send DELETE /account/2
        Then the response status is 401

    Scenario: Get Account as accountTwo account is not permitted
        Given a REST client authenticated as accountTwo
        When send GET /account/2
        Then the response status is 403

    Scenario: Update Account name as accountTwo account is not permitted
        Given a REST client authenticated as accountTwo
        When request json body has:
            | name     | accountTwo                  |
            | password | password                    |
            | email    | test@teste.com              |
            | roles    | [ ROLE_USER ]               |
            | teams    | [ http://localhost/team/1 ] |
        And send PUT /account/2
        Then the response status is 403

    Scenario: Update Account email as accountTwo account is not permitted
        Given a REST client authenticated as accountTwo
        When request json body has:
            | name     | accountOne                  |
            | password | password                    |
            | email    | accountTwo@test.com         |
            | roles    | [ ROLE_USER ]               |
            | teams    | [ http://localhost/team/1 ] |
        And send PUT /account/2
        Then the response status is 403

    Scenario: Update field name of Account as accountTwo account is not permitted
        Given a REST client authenticated as accountTwo
        When request json body has:
            | name | accountTwo |
        And send PATCH /account/2
        Then the response status is 403

    Scenario: Delete Account as accountTwo account is not permitted
        Given a REST client authenticated as accountTwo
        When send DELETE /account/2
        Then the response status is 403

    Scenario: Search Account by Name
        Given a REST client authenticated as admin
        When send GET /account/search/findByName?name=accountOne
        Then the response search at '_embedded.account[0].name' equal to accountOne

    Scenario: Search Account by NameContaining
        Given a REST client authenticated as admin
        When send GET /account/search/findByNameContaining?name=untO
        Then the response search at '_embedded.account[0].name' equal to accountOne
