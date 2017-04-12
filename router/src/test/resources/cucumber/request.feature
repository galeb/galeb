Feature: Request Support
  The router have than to support simples requests

  Scenario: Sending GET to / with valid host header
    Given a valid host request
    When Do GET /
    Then the response status is 200

  Scenario: Sending GET to / with invalid host header
    Given a invalid host request
    When Do GET /
    Then the response status is 500