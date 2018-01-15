@index
Feature: index access

  Scenario: check index root
    Given a REST client authenticated as admin with password pass
    And send GET /
    Then the response status is 200
