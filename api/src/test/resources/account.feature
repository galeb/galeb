@account
Feature: Account Support
    The manager have than
    to support REST standard

    Background:
      Given reset

    Scenario: Create Team
      Given a REST client authenticated
      When request json body has:
        | name  | teamOne |
      And send POST /team
      Then the response status is 201