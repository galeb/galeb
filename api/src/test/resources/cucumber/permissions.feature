Feature: Account Support
    The manager have than
    to support REST standard

    Scenario:
      Given a REST client authenticated as admin with password pass
      When request json body has:
        | username     | accountOne              |
        | password | password                    |
        | email    | accountone@test.com               |
      And send POST /account
      Then the response status is 201
      When request json body has:
        | name     | teamOne              |
      And send POST /team
      Then the response status is 201
      When request json body has:
        | name     | teamTwo             |
      And send POST /team
      Then the response status is 201
      When request json body has:
        | teams         | [Team=teamOne]      |
      And send PATCH /rolegroup/2
      Then the response status is 200
      When request json body has:
        | teams         | [Team=teamTwo]      |
      And send PATCH /rolegroup/3
      Then the response status is 200
      When request json body has:
        | accounts         | [Account=accountOne]      |
      And send PATCH /team/2
      Then the response status is 200
      When request json body has:
        | accounts         | [Account=accountOne]      |
      And send PATCH /team/3
      Then the response status is 200
      Given a REST client authenticated with accountOne
      When request json body has:
        | name     | projectOne              |
        | teams         | [Team=teamOne]      |
      And send POST /project
      Then the response status is 201
      Given a REST client authenticated with accountOne
      When request json body has:
        | name     | projectTwo              |
        | teams         | [Team=teamTwo]      |
      And send POST /project
      Then the response status is 201
