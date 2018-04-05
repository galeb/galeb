@active
Feature: Account Support
    The manager have than
    to support REST standard

    Background:
      Given a REST client authenticated as adminTeamOne with password ""
      Then the response status is 200
      Given a REST client authenticated as accountOne with password ""
      Then the response status is 200
      Given a REST client authenticated as admin with password pass
      When request json body has:
        | name     | teamOne              |
      And send POST /team
      Then the response status is 201
      Given a REST client authenticated as admin with password pass
      When request json body has:
        | accounts         | [Account=adminTeamOne, Account=accountOne]      |
      And send PATCH /team/1
      Then the response status is 200

    Scenario: Get existent Account
      Given a REST client authenticated as adminTeamOne with password ""
      When send GET Account=adminTeamOne
      Then the response status is 200
      And property username contains adminTeamOne

    Scenario: Get null Account
      Given a REST client authenticated as adminTeamOne with password ""
      When send GET /account/99
      Then the response status is 404

    Scenario: Create duplicated Account
      Given a REST client authenticated as admin with password pass
      When request json body has:
        | username     | accountOne              |
        | email    | accountOne@test.com               |
      And send POST /account
      Then the response status is 409

    Scenario: Update Account name
      Given a REST client authenticated as adminTeamOne with password ""
      When request json body has:
        | username     | accountThree            |
        | email    | test3@teste.com             |
      And send PUT Account=adminTeamOne
      Then the response status is 200
      And a REST client authenticated as adminTeamOne with password ""
      When send GET Account=adminTeamOne
      Then the response status is 200
      And property username contains adminTeamOne

    Scenario: Update Account email
      Given a REST client authenticated as adminTeamOne with password ""
      When request json body has:
        | email    | accountTwo@test.com         |
      And send PATCH Account=adminTeamOne
      Then the response status is 200
      And a REST client authenticated as adminTeamOne with password ""
      When send GET Account=adminTeamOne
      Then the response status is 200
      And property email contains accountTwo@test.com

    Scenario: Delete Account Denied
      Given a REST client authenticated as adminTeamOne with password ""
      When send DELETE Account=accountOne
      Then the response status is 403

    Scenario: Delete Account
      Given a REST client authenticated as admin with password pass
      When send DELETE Account=adminTeamOne
      Then the response status is 204
      Given a REST client authenticated as admin with password pass
      When send GET Account=adminTeamOne
      Then the response status is 404