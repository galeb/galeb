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