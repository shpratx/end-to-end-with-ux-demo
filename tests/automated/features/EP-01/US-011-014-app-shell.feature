@epic-EP01 @story-US011 @story-US014 @regression @high
Feature: Mobile App Shell, Navigation & Error Handling
  As a loyalty customer
  I want a mobile app with clear navigation and graceful error handling
  So that I can easily access my loyalty features without crashes

  @smoke @critical
  Scenario: App loads within 3 seconds on 4G connection
    Given the network is throttled to 4G speed
    When I launch the application
    Then the app should be fully loaded within 3 seconds
    And a splash screen should be visible during initialization

  @high
  Scenario: Navigation tabs render with correct labels and icons
    Given the app is loaded
    When I view the bottom navigation bar
    Then I should see 4 tabs
    And the tabs should be "Home", "History", "QR Code", "Profile"
    And each tab should have an icon and a label

  @high
  Scenario Outline: Deep link navigates to correct screen
    Given the app is loaded
    When I open the deep link "<deep_link>"
    Then I should be navigated to the "<screen>" screen

    Examples:
      | deep_link          | screen              |
      | loyalty://history  | Transaction History |
      | loyalty://profile  | Profile             |
      | loyalty://home     | Home                |

  @story-US012 @medium
  Scenario: Offline banner shows when network is lost
    Given the app is loaded
    When the device loses network connectivity
    Then an offline banner should be visible with text "You're offline — some features may be limited"
    When the device regains network connectivity
    Then the offline banner should disappear

  @story-US014 @high
  Scenario: Error boundary catches unhandled crash
    Given the app is loaded
    When an unhandled error occurs in a component
    Then a friendly error screen should be displayed with text "Something went wrong — tap to retry"
    And the error should be reported to the monitoring service
