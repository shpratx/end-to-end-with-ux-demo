@epic-EP01 @story-US004 @story-US005 @regression @high
Feature: Structured Logging, Metrics & Alerting
  As a platform engineer
  I want structured logging and metrics collection deployed
  So that all services are observable from day one

  Background:
    Given the observability stack is deployed

  @smoke @high
  Scenario: Structured logs are emitted in JSON format
    Given I send a GET request to "/health"
    When I query the log aggregator for the latest entry
    Then the log entry should be in JSON format
    And the log entry should contain field "timestamp"
    And the log entry should contain field "level"
    And the log entry should contain field "service"
    And the log entry should contain field "correlation_id"
    And the log entry should contain field "message"

  @high
  Scenario: Metrics endpoint returns required metrics
    When I scrape the metrics endpoint
    Then the response should contain metric "transaction_count"
    And the response should contain metric "error_count"
    And the response should contain metric "latency_histogram"

  @story-US005 @critical
  Scenario Outline: Alert fires when SLA threshold is breached
    Given the alerting rules are configured
    When the "<metric>" exceeds "<threshold>" for "<duration>"
    Then a "<severity>" alert should fire
    And the alert should contain the affected service name

    Examples:
      | metric       | threshold | duration  | severity |
      | p95_latency  | 2s        | 1 minute  | warning  |
      | error_rate   | 1%        | 2 minutes | critical |
      | availability | < 99.9%   | 1 hour    | critical |
