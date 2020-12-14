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
    
  Scenario: Sending GET to /search?query=query{%22key%22:%22value%22} with valid host header
    Given a valid host request to FASTTER backend
    When Do GET /search?query=query{"key":"value"}
    Then the response status is 200
    
  Scenario: Sending GET to /search?query=query{%22key%22:%22value%22} with valid host header
    Given a valid host request to FASTTER backend
    When Do GET /search?query=query{"key":"value"}
    And with cookies: 
        | glb_uid | CpFïfJvI�� {�O>0�����RF�w��t��L�9RԂ�= |
        | JSESSIONID | xxxxXXXXXXxxxxx |
    Then the response status is 200
    
