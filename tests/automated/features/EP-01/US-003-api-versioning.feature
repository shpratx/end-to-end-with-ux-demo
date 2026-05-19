@epic-EP01 @regression
Feature: API Versioning & Standardized Error Responses
  As an integration developer
  I want versioned API endpoints with standardized error responses
  So that I can build reliable integrations without breaking changes

  Background:
    Given the loyalty API is running

  @story-US003 @high @smoke
  Scenario: API endpoints are versioned under /api/v1
    When I call "GET" "/api/v1/health"
    Then the response status should be 200
    And the response should contain "status"

  @story-US003 @high
  Scenario: Non-existent endpoint returns RFC 7807 error format
    When I call "GET" "/api/v1/nonexistent-endpoint"
    Then the response status should be 404
    And the response content-type should be "application/problem+json"
    And the response body should contain "type"
    And the response body should contain "title"
    And the response body should contain "status"
    And the response body should contain "traceId"

  @story-US003 @medium
  Scenario Outline: All error responses follow RFC 7807 format
    When I trigger a "<error_type>" error on "<endpoint>"
    Then the response status should be <status>
    And the response body should match RFC 7807 schema

    Examples:
      | error_type     | endpoint              | status |
      | validation     | /api/v1/auth/register | 422    |
      | unauthorized   | /api/v1/notifications | 401    |
      | not_found      | /api/v1/customers/me  | 404    |
