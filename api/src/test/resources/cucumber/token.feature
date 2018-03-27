@active
Feature: Flux
  Scenario: token with user admin
    Given a REST client authenticated as admin with password pass
    And send GET /token
    Then the response status is 200
    And the response search at 'account' equal to admin
    And the response search at 'admin' has boolean true

  Scenario: token with user default
    Given a REST client authenticated as user1 with password ""
    And send GET /token
    Then the response status is 200
    And the response search at 'account' equal to user1
    And the response search at 'admin' has boolean false