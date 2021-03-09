@active
Feature: Flux
  Scenario: token with user admin
    Given a REST client authenticated as adminTeamOne with password pass
    And send GET /token
    Then the response status is 200
    And the response search at 'username' equal to adminTeamOne
    And the response search at 'admin' has boolean false

  Scenario: token with user default
    Given a REST client authenticated as user1 with password ""
    And send GET /token
    Then the response status is 200
    And the response search at 'username' equal to user1
    And the response search at 'admin' has boolean false