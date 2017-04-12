Feature: Internal healthcheck support

  Scenario: Check frontend router port
    Given a __ping__ host request
    When Do GET /
    Then the response status is 200
    And body is WORKING

  Scenario: Check rule internal
    Given a valid host request
    When Do GET /
    And with headers:
        | X-Check-Pool | check |
    Then the response status is 200
    And body is POOL_REACHABLE