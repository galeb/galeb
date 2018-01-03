@account
Feature: Account Support
    The manager have than
    to support REST standard

    Background:
      Given reset
      And a REST client authenticated as admin with password pass
      When request json body has:
        | name  | teamOne |
      And send POST /team
      Then the response status is 201


    Scenario: Create Account
      Then the response status is 201