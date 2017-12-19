@account
Feature: Account Support
    The manager have than
    to support REST standard

    Background:
      Given reset
      Given a REST client unauthenticated
      When request json body has:
          | name  | teamOne |
      And send POST /team
      Then the response status is 201
      And a REST client unauthenticated
      When request json body has:
        | name     | accountOne                  |
        | email    | test@test.com               |
        | teams    | [ http://localhost/team/1 ] |
      And send POST /account
      Then the response status is 201


    Scenario: Get Account
      Given a REST client unauthenticated
      And send GET /account/1
      Then the response status is 201


#    Scenario: Create duplicated account
#      Given a REST client unauthenticated
#      When request json body has:
#        | name     | accountOne                  |
#        | email    | test@test.com               |
#        | teams    | [ http://localhost/team/1 ] |
#      And send POST /account
#      Then the response status is 409



