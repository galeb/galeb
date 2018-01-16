@rule
@ignore
Feature: Rule Support
    The manager have than
    to support REST standard

    Background:
        Given a REST client authenticated as admin
        When request json body has:
            | name | urlPath |
        And send POST /ruletype
        And a REST client authenticated as admin
        When request json body has:
            | name | cookie |
        And send POST /ruletype
        Then the response status is 201
        And a REST client authenticated as admin
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
            | name  | projOne             |
            | teams    | [ Team=teamOne ] |
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
        When request json body has:
            | name        | virtOne            |
            | environment | Environment=envOne |
            | project     | Project=projOne    |
        And send POST /virtualhost
        Then the response status is 201
        And a REST client authenticated as accountOne
        When request json body has:
            | name        | poolOne            |
            | environment | Environment=envOne |
            | project     | Project=projOne    |
        And send POST /pool
        Then the response status is 201
        And a REST client authenticated as accountOne
        And request json body has:
            | name         | ruleOne                 |
            | ruleType     | RuleType=urlPath        |
            | parents      | [ VirtualHost=virtOne ] |
            | pool         | Pool=poolOne            |
            | default      | true                    |
            | order        | 0                       |
        And send POST /rule

    Scenario: Create Rule
        Then the response status is 201

    Scenario: Create duplicated Rule
        Given a REST client authenticated as accountOne
        When request json body has:
            | name         | ruleOne                 |
            | ruleType     | RuleType=urlPath        |
            | parents      | [ VirtualHost=virtOne ] |
            | pool         | Pool=poolOne            |
        And send POST /rule
        Then the response status is 409

    Scenario: Create Rule with parent and pool in different Farms
        Given a REST client authenticated as admin
        When request json body has:
            | name | envTwo |
        And send POST /environment
        Then the response status is 201
        And a REST client authenticated as admin
        When request json body has:
            | name        | farmTwo              |
            | domain      | domain               |
            | api         | api                  |
            | environment | Environment=envTwo   |
            | provider    | Provider=providerOne |
        And send POST /farm
        Then the response status is 201
        And a REST client authenticated as accountOne
        When request json body has:
            | name        | poolTwo            |
            | environment | Environment=envTwo |
            | project     | Project=projOne    |
        And send POST /pool
        Then the response status is 201
        And a REST client authenticated as accountOne
        And request json body has:
            | name         | ruleTwo                 |
            | ruleType     | RuleType=urlPath        |
            | parents      | [ VirtualHost=virtOne ] |
            | pool         | Pool=poolTwo            |
        And send POST /rule
        Then the response status is 400

    Scenario: Get Rule
        Given a REST client authenticated as accountOne
        When send GET Rule=ruleOne
        Then the response status is 200
        And property name contains ruleOne

    Scenario: Get null Rule
        Given a REST client authenticated as accountOne
        When send GET Rule=NULL
        Then the response status is 404

    Scenario: Update Rule
        Given a REST client authenticated as accountOne
        When request json body has:
            | name         | ruleOne                 |
            | ruleType     | RuleType=urlPath        |
            | parents      | [ VirtualHost=virtOne ] |
            | pool         | Pool=poolOne            |
        And send PUT Rule=ruleOne
        Then the response status is 204
        And a REST client authenticated as accountOne
        When send GET Rule=ruleOne
        Then the response status is 200
        And property name contains ruleOne

    Scenario: Update name field of Rule
        Given a REST client authenticated as accountOne
        When request json body has:
            | name | ruleOne |
        And send PATCH Rule=ruleOne
        Then the response status is 204
        And a REST client authenticated as accountOne
        When send GET Rule=ruleOne
        Then the response status is 200
        And property name contains ruleOne

    Scenario: Update ruleType field of Rule
        Given a REST client authenticated as accountOne
        When request json body has:
            | ruleType | RuleType=cookie |
        And send PATCH Rule=ruleOne
        Then the response status is 204
        And a REST client authenticated as accountOne
        When send GET Rule=ruleOne
        Then the response status is 200
        And property name contains ruleOne

    Scenario: Delete Rule
        Given a REST client authenticated as accountOne
        When send DELETE Rule=ruleOne
        Then the response status is 204
        And a REST client authenticated as accountOne
        When send GET Rule=ruleOne
        Then the response status is 404
        And a REST client authenticated as accountOne
        When send GET RuleType=urlPath
        Then the response status is 200
        And a REST client authenticated as accountOne
        When send GET VirtualHost=virtOne
        Then the response status is 200
        And a REST client authenticated as accountOne
        When send GET Pool=poolOne
        Then the response status is 200

    Scenario: Search Rule by Name
        Given a REST client authenticated as admin
        When send GET /rule/search/findByName?name=ruleOne
        Then the response search at '_embedded.rule[0].name' equal to ruleOne

    Scenario: Search Rule by NameContaining
        Given a REST client authenticated as admin
        When send GET /rule/search/findByNameContaining?name=One
        Then the response search at '_embedded.rule[0].name' equal to ruleOne
