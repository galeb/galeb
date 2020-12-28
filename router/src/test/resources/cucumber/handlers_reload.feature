Feature: Handlers and pools creation

  Scenario: Load default config
    Given a vhost __ping__ request to DEFAULT backend with CONFIG_DEFAULT
    When Do GET /
    Then the response status is 200
    And body is OUTDATED
    And a vhost __ping__ request to DEFAULT backend with CONFIG_DEFAULT
    When Do GET /
    Then the response status is 200
    And body is not OUTDATED
    And wait 5000 ms
    And a vhost __ping__ request to DEFAULT backend with CONFIG_DEFAULT
    When Do GET /
    Then the response status is 200
    And body is WORKING

  Scenario: Check rule target
    Given a vhost test.com request to DEFAULT backend
    When Do GET /
    And with headers:
        | X-Check-Pool | check |
    Then the response status is 200
    And body is POOL_REACHABLE

  Scenario: Check rule path
    Given a vhost test.com request to DEFAULT backend
    When Do GET /__galeb_rule_path_check__
    Then the response status is 200
    And body is RULE_PATH_REACHABLE

  Scenario: Load config with alias
    Given a vhost __ping__ request to DEFAULT backend with CONFIG_ALIAS
    When Do GET /
    Then the response status is 200
    And body is WORKING
    And a vhost __ping__ request to DEFAULT backend with CONFIG_ALIAS
    When Do GET /
    Then the response status is 200
    And body is not OUTDATED
    And wait 5000 ms
    And a vhost __ping__ request to DEFAULT backend with CONFIG_ALIAS
    When Do GET /
    Then the response status is 200
    And body is WORKING

  Scenario: Check alias target
    Given a vhost alias.test.com request to DEFAULT backend
    When Do GET /__galeb_rule_path_check__
    Then the response status is 200
    And body is RULE_PATH_REACHABLE

  Scenario: Load config with multi vhosts
    Given a vhost __ping__ request to DEFAULT backend with CONFIG_MULTIVHOSTS
    When Do GET /
    Then the response status is 200
    And body is WORKING
    And a vhost __ping__ request to DEFAULT backend with CONFIG_MULTIVHOSTS
    When Do GET /
    Then the response status is 200
    And body is not OUTDATED
    And wait 5000 ms
    And a vhost __ping__ request to DEFAULT backend with CONFIG_MULTIVHOSTS
    When Do GET /
    Then the response status is 200
    And body is WORKING

  Scenario: Check alias target fails
    Given a vhost alias.test.com request to DEFAULT backend
    When Do GET /__galeb_rule_path_check__
    Then the response status is 503

  Scenario: Check default vhost
    Given a vhost test.com request to DEFAULT backend
    When Do GET /__galeb_rule_path_check__
    Then the response status is 200
    And body is RULE_PATH_REACHABLE

  @DirtyContextAfter
  Scenario: Check other vhost
    Given a vhost other.com request to DEFAULT backend
    When Do GET /__galeb_rule_path_check__
    Then the response status is 200
    And body is RULE_PATH_REACHABLE