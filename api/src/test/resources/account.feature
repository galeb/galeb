@account
Feature: Account Support
    The manager have than
    to support REST standard

    Scenario: Create Account
       Given a REST client unauthenticated
       When request json body has:
          | name  | teamOne |
       And send POST /team
       Then the response status is 201


    Scenario: Create duplicated account
      Given a REST client unauthenticated
      When request json body has:
        | name     | accountOne                  |
        | password | password                    |
        | email    | test@test.com               |
        | roles    | [ ROLE_USER ]               |
        | teams    | [ http://localhost/team/1 ] |
      And send POST /account
      Then the response status is 201
      And a REST client unauthenticated
      When request json body has:
        | name     | accountOne                  |
        | password | password                    |
        | email    | test@test.com               |
        | roles    | [ ROLE_USER ]               |
        | teams    | [ http://localhost/team/1 ] |
      And send POST /account
      Then the response status is 409
