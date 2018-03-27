@active
Feature: Flux
  Background: Flux
    Given a REST client authenticated as admin with password pass
    When request json body has:
      | name  | envOne |
    And send POST /environment
    Then the response status is 201
    When request json body has:
      | name  | balancePolicyOne |
    And send POST /balancepolicy
    Then the response status is 201

  Scenario: validate audit for creation object
    Given a REST client authenticated as user1 with password ""
    When request json body has:
      | name     | teamTwo              |
      | accounts | [Account=user1]        |
    And send POST /team
    Then the response status is 201
    And the response search at '_created_by' equal to user1
    And the response search at '_last_modified_by' equal to user1