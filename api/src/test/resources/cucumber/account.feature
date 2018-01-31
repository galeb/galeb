@ignore
@account
Feature: Account Support
    The manager have than
    to support REST standard

    Background:
      Given a REST client authenticated as admin with password pass
      When request json body has:
        | username      | adminTeamOne                |
        | password      | password                |
        | email         | adminone@test.com       |
      And send POST /account
      Then the response status is 201
      Given a REST client authenticated as admin with password pass
      When request json body has:
        | username     | accountOne              |
        | password | password                    |
        | email    | accountone@test.com               |
      And send POST /account
      Then the response status is 201
      Given a REST client authenticated as admin with password pass
      When request json body has:
        | accounts         | [Account=adminTeamOne, Account=accountOne]      |
      And send PATCH /team/1
      Then the response status is 200

    Scenario: Get existent Account
      Given a REST client authenticated as adminTeamOne with token
      When send GET /account/2
      Then the response status is 200
      And property username contains adminTeamOne

    Scenario: Get null Account
      Given a REST client authenticated as adminTeamOne with token
      When send GET /account/4
      Then the response status is 404

    Scenario: Create duplicated Account
      Given a REST client authenticated as admin with password pass
      When request json body has:
        | username     | accountOne              |
        | password | password                    |
        | email    | accountOne@test.com               |
      And send POST /account
      Then the response status is 409

    Scenario: Update Account name
      Given a REST client authenticated as adminTeamOne with token
      When request json body has:
        | username     | accountThree            |
        | password | password                    |
        | email    | test3@teste.com             |
      And send PUT /account/3
      Then the response status is 200
      And a REST client authenticated as adminTeamOne with token
      When send GET /account/3
      Then the response status is 200
      And property username contains accountThree

    Scenario: Update Account email
      Given a REST client authenticated as adminTeamOne with token
      When request json body has:
        | username     | accountOne                  |
        | password | password                    |
        | email    | accountTwo@test.com         |
      And send PUT /account/3
      Then the response status is 200
      And a REST client authenticated as adminTeamOne with token
      When send GET /account/3
      Then the response status is 200
      And property email contains accountTwo@test.com

    Scenario: Delete Account Denied
      Given a REST client authenticated as adminTeamOne with token
      When send DELETE /account/3
      Then the response status is 403

    Scenario: Delete Account
      Given a REST client authenticated as admin with password pass
      When send DELETE /account/3
      Then the response status is 204
      And  a REST client authenticated as adminTeamOne with token
      When send GET /account/3
      Then the response status is 404