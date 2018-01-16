@balancepolicytype
@ignore
Feature: BalancePolicyType Support
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
            | name     | accountOne                  |
            | password | password                    |
            | email    | test@test.com               |
            | roles    | [ ROLE_USER ]               |
            | teams    | [ http://localhost/team/1 ] |
        And send POST /account
        Then the response status is 201
        And a REST client authenticated as admin
        When request json body has:
            | name | tBalancePolicyOne |
        And send POST /balancepolicytype

    Scenario: Create BalancePolicyType
        Then the response status is 201

    Scenario: Create duplicated BalancePolicyType
        Given a REST client authenticated as admin
        When request json body has:
            | name | tBalancePolicyOne |
        And send POST /balancepolicytype
        Then the response status is 409

    Scenario: Get BalancePolicyType
        Given a REST client authenticated as accountOne
        When send GET BalancePolicyType=tBalancePolicyOne
        Then the response status is 200
        And property name contains tBalancePolicyOne

    Scenario: Get null BalancePolicyType
        Given a REST client authenticated as accountOne
        When send GET BalancePolicyType=NULL
        Then the response status is 404

    Scenario: Update BalancePolicyType
        Given a REST client authenticated as admin
        When request json body has:
            | name | tBalancePolicyTwo |
        And send PUT BalancePolicyType=tBalancePolicyOne
        Then the response status is 204
        And a REST client authenticated as admin
        When send GET BalancePolicyType=tBalancePolicyTwo
        Then the response status is 200
        And property name contains tBalancePolicyTwo

    Scenario: Update one field of BalancePolicyType
        Given a REST client authenticated as admin
        When request json body has:
            | name | tBalancePolicyThree |
        And send PATCH BalancePolicyType=tBalancePolicyOne
        Then the response status is 204
        And a REST client authenticated as admin
        When send GET BalancePolicyType=tBalancePolicyThree
        Then the response status is 200
        And property name contains tBalancePolicyThree

    Scenario: Delete BalancePolicyType
        Given a REST client authenticated as admin
        When send DELETE BalancePolicyType=tBalancePolicyOne
        Then the response status is 204
        And a REST client authenticated as admin
        When send GET BalancePolicyType=tBalancePolicyOne
        Then the response status is 404

    Scenario: Search BalancePolicyType by Name
        Given a REST client authenticated as admin
        When send GET /balancepolicytype/search/findByName?name=tBalancePolicyOne
        Then the response search at '_embedded.balancepolicytype[0].name' equal to tBalancePolicyOne

    Scenario: Search BalancePolicyType by NameContaining
        Given a REST client authenticated as admin
        When send GET /balancepolicytype/search/findByNameContaining?name=lancePolicyOn
        Then the response search at '_embedded.balancepolicytype[0].name' equal to tBalancePolicyOne
