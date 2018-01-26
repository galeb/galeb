@index
Feature: index access

  Scenario: check index root
    Given a REST client authenticated with token and role LOCAL_ADMIN
    And send GET /
    Then the response status is 200
