@epic-EP01 @regression
Feature: Web App Scaffold with Responsive Layout
  As a loyalty customer using the website
  I want a responsive web application with consistent navigation
  So that I can manage my loyalty account from any device

  Background:
    Given the loyalty web application is loaded

  @story-US013 @high @smoke
  Scenario: Web app renders with header, content, and footer
    Then the page should have a header with logo and navigation links
    And the page should have a main content area
    And the page should have a footer

  @story-US013 @high
  Scenario: Mobile viewport collapses navigation to hamburger menu
    Given the viewport is set to 375px width
    Then the navigation should collapse to a hamburger menu icon
    And tapping the hamburger should reveal navigation links
    And content should be single-column layout

  @story-US013 @high @security
  Scenario: Unauthenticated user is redirected to login
    Given I am not logged in
    When I navigate to "/profile"
    Then I should be redirected to "/auth/login"
    And the return URL should be preserved as "?returnTo=/profile"

  @story-US013 @medium @accessibility
  Scenario: Web app is fully keyboard navigable
    When I navigate using only keyboard (Tab, Enter, Space)
    Then all interactive elements should be reachable
    And focus order should follow logical reading order
