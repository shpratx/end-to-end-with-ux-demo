@epic-EP01 @story-US009 @story-US010 @regression @critical
Feature: Push Notifications & In-App Notification Center
  As a loyalty customer
  I want to receive push notifications and review them in-app
  So that I stay informed about my points and rewards

  Background:
    Given I am authenticated as a test customer
    And the notification service is running

  @smoke @critical
  Scenario: Event triggers notification delivery within 30 seconds
    When a "points.earned" event is published for the test customer
    Then a push notification should be delivered within 30 seconds
    And the notification body should contain the substituted points value

  @high
  Scenario: Failed delivery retries with exponential backoff
    Given the push delivery endpoint is configured to fail
    When a "points.earned" event is published for the test customer
    Then the service should retry delivery 3 times
    And the retry intervals should follow exponential backoff of 1s, 5s, 30s
    And the notification should be marked as failed after all retries

  @story-US010 @high
  Scenario: Notification is stored in the notification center
    When a "points.earned" event is published for the test customer
    And I wait for notification processing to complete
    And I send a GET request to "/notifications"
    Then the response status should be 200
    And the response should contain the notification with correct title and body
    And notifications should be ordered newest first

  @story-US010 @high
  Scenario: Mark as read updates unread count
    Given a notification exists for the test customer
    When I get the unread notification count
    And I mark the notification as read
    And I get the unread notification count again
    Then the unread count should have decremented by 1
