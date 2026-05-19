@epic-EP01 @story-US002 @regression @critical
Feature: API Gateway with Authentication & Rate Limiting
  As a backend developer
  I want an API gateway with authentication and rate limiting configured
  So that all services have a secure, consistent entry point

  Background:
    Given the API gateway is running

  @smoke @critical
  Scenario: Health endpoint returns 200 with service status
    When I send a GET request to "/health"
    Then the response status should be 200
    And the response body should contain "status"
    And the response time should be less than 100 milliseconds

  @critical
  Scenario: Unauthenticated request to protected endpoint returns 401
    When I send a GET request to "/notifications" without authentication
    Then the response status should be 401
    And the response body should not contain any customer data
    And the response content type should be "application/problem+json"

  @high
  Scenario: Rate limiting returns 429 after threshold exceeded
    Given I am authenticated as a test user
    When I send 1001 requests to "/health" within 1 minute
    Then the last response status should be 429
    And the response should contain a "Retry-After" header

  @high
  Scenario Outline: Error responses follow RFC 7807 format
    When I trigger a "<error_type>" error
    Then the response status should be <status_code>
    And the response content type should be "application/problem+json"
    And the response body should contain field "type"
    And the response body should contain field "title"
    And the response body should contain field "status"
    And the response body should contain field "detail"
    And the response body should contain field "traceId"

    Examples:
      | error_type      | status_code |
      | bad_request     | 400         |
      | unauthorized    | 401         |
      | not_found       | 404         |
      | validation      | 422         |
