@epic-EP01 @regression
Feature: Design System Component Library
  As a frontend developer
  I want a shared component library with accessible UI components
  So that I can build consistent screens without recreating common patterns

  Background:
    Given the web application is loaded

  @story-US006 @high @smoke
  Scenario: Button component renders in all variants
    When I render a Button component with variant "primary"
    Then the button should be visible with minimum 44px touch target
    And the button should have contrast ratio of at least 4.5:1

  @story-US006 @high @accessibility
  Scenario: Form input shows accessible error state
    When I render a TextInput with error "Email is required"
    Then the input should have aria-invalid attribute set to "true"
    And the error message should be associated via aria-describedby
    And the error message should be visible in red

  @story-US006 @medium @accessibility
  Scenario: All interactive components are keyboard navigable
    Given the component showcase page is loaded
    When I press Tab repeatedly
    Then focus should move through all interactive elements in logical order
    And each focused element should have a visible 2px focus outline

  @story-US006 @medium
  Scenario: Components reflow on mobile viewport
    Given the viewport is set to 320px width
    When I render the component showcase
    Then no horizontal scrollbar should appear
    And all components should be fully visible

  @story-US007 @high
  Scenario: Modal traps focus and is dismissible
    When I open a confirmation modal
    Then focus should be trapped within the modal
    And pressing Escape should close the modal
    And focus should return to the trigger element

  @story-US007 @medium
  Scenario: Skeleton loading component renders placeholder shapes
    When I render a skeleton loader for "loyalty-card"
    Then animated placeholder shapes should be visible
    And they should match the expected content layout dimensions
