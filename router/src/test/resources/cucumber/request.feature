Feature: Request Support
  The router have than to support simples requests

  Scenario: Sending GET to / with valid host header
    Given a valid host request to FASTTER backend
    When Do GET /
    Then the response status is 200
    And jmx has ActiveConnections
    And jmx has ActiveRequests

  Scenario: Sending GET to / with invalid host header
    Given a invalid host request to FASTTER backend
    When Do GET /
    Then the response status is 503

  Scenario: Sending GET to / without server backend
    Given a valid host request to BROKEN backend
    When Do GET /
    Then the response status is 503