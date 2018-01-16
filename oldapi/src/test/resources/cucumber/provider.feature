@provider
@ignore
Feature: Provider Support
    The manager have than
    to support REST standard

    Background:
        Given a REST client authenticated as admin
        When request json body has:
            | name | provOne |
        And send POST /provider

    Scenario: Create Provider
        Then the response status is 201

    Scenario: Create duplicated Provider
        Given a REST client authenticated as admin
        When request json body has:
            | name | provOne |
        And send POST /provider
        Then the response status is 409

    Scenario: Get Provider
        Given a REST client authenticated as admin
        When send GET /provider/1
        Then the response status is 200
        And property name contains provOne

    Scenario: Get null Provider
        Given a REST client authenticated as admin
        When send GET /provider/2
        Then the response status is 404

    Scenario: Update Provider
        Given a REST client authenticated as admin
        When request json body has:
            | name | provTwo |
        And send PUT /provider/1
        Then the response status is 204
        And a REST client authenticated as admin
        When send GET /provider/1
        Then the response status is 200
        And property name contains provTwo

    Scenario: Update one field of Provider
        Given a REST client authenticated as admin
        When request json body has:
            | name | provThree |
        And send PATCH /provider/1
        Then the response status is 204
        And a REST client authenticated as admin
        When send GET /provider/1
        Then the response status is 200
        And property name contains provThree

    Scenario: Delete Provider
        Given a REST client authenticated as admin
        When send DELETE /provider/1
        Then the response status is 204
        And a REST client authenticated as admin
        When send GET /provider/1
        Then the response status is 404

    Scenario: Search Provider by Name
        Given a REST client authenticated as admin
        When send GET /provider/search/findByName?name=provOne
        Then the response search at '_embedded.provider[0].name' equal to provOne

    Scenario: Search Provider by NameContaining
        Given a REST client authenticated as admin
        When send GET /provider/search/findByNameContaining?name=One
        Then the response search at '_embedded.provider[0].name' equal to provOne
