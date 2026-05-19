@epic-EP01 @regression
Feature: CI/CD Pipeline with Security Scanning
  As a developer
  I want an automated CI/CD pipeline that builds, tests, scans, and deploys
  So that I can ship changes safely and frequently

  @story-US008 @critical @smoke
  Scenario: Pipeline blocks merge when test coverage below 80%
    Given a feature branch with unit test coverage at 75%
    When the CI pipeline runs
    Then the build should fail
    And the failure reason should mention "coverage below threshold"

  @story-US008 @critical
  Scenario: Pipeline blocks merge on critical SAST findings
    Given a feature branch with a known SQL injection vulnerability
    When the SAST scan runs
    Then the build should fail
    And the scan report should flag "critical" severity finding

  @story-US008 @high @smoke
  Scenario: Successful merge to main auto-deploys to staging
    Given a feature branch with passing tests and no SAST findings
    When the branch is merged to main
    Then the staging environment should be updated within 5 minutes
    And the health endpoint on staging should return 200

  @story-US008 @high
  Scenario: Production rollback restores previous version
    Given a failed production deployment
    When rollback is triggered
    Then the previous version should be restored within 5 minutes
    And the health endpoint should return 200 with the previous version number
