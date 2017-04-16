Feature: Request Support
  The router have than to support simples requests

  Scenario: Sending GET to / with valid host header
    Given a valid host request to FASTTER backend
    When Do GET /
    Then the response status is 200
    And has 0 active connections
    And has 1 active requests

  Scenario: Sending GET to / with invalid host header
    Given a invalid host request to FASTTER backend
    When Do GET /
    Then the response status is 503

  Scenario: Sending GET to / without server backend
    Given a valib host request to BROKEN backend
    When Do GET /
    Then the response status is 503