Feature: Testing resource service functionality.
  Scenario: Save song
    When Client send file.mp3 to the resources endpoint
    Then Client received status code 200
    And  Client received response with id=1
  Scenario: Save not mp3 file
    When Client send test.csv to the resources endpoint
    Then Client received status code 400