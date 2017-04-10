Feature: Simple Request Support
  The router have than to support simples requests

  Background:
    Given a http client

  Scenario: Sending GET to /
    When send GET /
    Then the response status is 200