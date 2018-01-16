@balancepolicy
@ignore
Feature: BalancePolicy Support
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
            | name     | accountOne       |
            | password | password         |
            | email    | test@test.com    |
            | roles    | [ ROLE_USER ]    |
            | teams    | [ Team=teamOne ] |
        And send POST /account
        Then the response status is 201
        And a REST client authenticated as admin
        When request json body has:
            | name | tBalancePolicyTypeOne |
        And send POST /balancepolicytype
        Then the response status is 201
        And a REST client authenticated as admin
        When request json body has:
            | name              | tBalancePolicyOne                       |
            | balancePolicyType | BalancePolicyType=tBalancePolicyTypeOne |
        And send POST /balancepolicy

    Scenario: Create BalancePolicy
        Then the response status is 201

    Scenario: Create duplicated BalancePolicy
        Given a REST client authenticated as admin
        When request json body has:
            | name              | tBalancePolicyOne                       |
            | balancePolicyType | BalancePolicyType=tBalancePolicyTypeOne |
        And send POST /balancepolicy
        Then the response status is 409

    Scenario: Get BalancePolicy
        Given a REST client authenticated as accountOne
        When send GET BalancePolicy=tBalancePolicyOne
        Then the response status is 200
        And property name contains tBalancePolicyOne

    Scenario: Get null BalancePolicy
        Given a REST client authenticated as accountOne
        When send GET BalancePolicy=NULL
        Then the response status is 404

    Scenario: Update BalancePolicy
        Given a REST client authenticated as admin
        When request json body has:
            | name              | tBalancePolicyTwo                       |
            | balancePolicyType | BalancePolicyType=tBalancePolicyTypeOne |
        And send PUT BalancePolicy=tBalancePolicyOne
        Then the response status is 204
        And a REST client authenticated as admin
        When send GET BalancePolicy=tBalancePolicyTwo
        Then the response status is 200
        And property name contains tBalancePolicyTwo

    Scenario: Update one field of BalancePolicy
        Given a REST client authenticated as admin
        When request json body has:
            | name | tBalancePolicyThree |
        And send PATCH BalancePolicy=tBalancePolicyOne
        Then the response status is 204
        And a REST client authenticated as admin
        When send GET BalancePolicy=tBalancePolicyThree
        Then the response status is 200
        And property name contains tBalancePolicyThree

    Scenario: Delete BalancePolicy
        Given a REST client authenticated as admin
        When send DELETE BalancePolicy=tBalancePolicyOne
        Then the response status is 204
        And a REST client authenticated as admin
        When send GET BalancePolicy=tBalancePolicyOne
        Then the response status is 404

    Scenario: Search BalancePolicy by Name
        Given a REST client authenticated as admin
        When send GET /balancepolicy/search/findByName?name=tBalancePolicyOne
        Then the response search at '_embedded.balancepolicy[0].name' equal to tBalancePolicyOne

    Scenario: Search BalancePolicy by NameContaining
        Given a REST client authenticated as admin
        When send GET /balancepolicy/search/findByNameContaining?name=lancePolicyOn
        Then the response search at '_embedded.balancepolicy[0].name' equal to tBalancePolicyOne
