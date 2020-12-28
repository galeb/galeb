Feature: Internal healthcheck support

  Scenario: Check frontend router port
    Given a __ping__ host request to FASTTER backend
    When Do GET /
    Then the response status is 200
    And body is OUTDATED
    And a __ping__ host request to FASTTER backend
    When Do GET /
    Then the response status is 200
    And body is not OUTDATED
    And wait 5000 ms
    And a __ping__ host request to FASTTER backend
    When Do GET /
    Then the response status is 200
    And body is WORKING

  Scenario: Check rule target
    Given a valid host request to FASTTER backend
    When Do GET /
    And with headers:
        | X-Check-Pool | check |
    Then the response status is 200
    And body is POOL_REACHABLE

  @DirtyContextAfter
  Scenario: Check rule path
    Given a valid host request to FASTTER backend
    When Do GET /__galeb_rule_path_check__
    Then the response status is 200
    And body is RULE_PATH_REACHABLE
