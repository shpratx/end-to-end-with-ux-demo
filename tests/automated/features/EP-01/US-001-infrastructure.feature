@epic-EP01 @story-US001 @regression @critical
Feature: Cloud Infrastructure Provisioning
  As a platform engineer
  I want the cloud infrastructure provisioned via IaC
  So that all environments are reproducible and the team can deploy services from Sprint 1

  Background:
    Given the infrastructure provisioning has completed

  @smoke
  Scenario: Kubernetes cluster is accessible with minimum nodes
    When I query the Kubernetes cluster via kubectl
    Then the cluster should be running
    And there should be at least 3 nodes in "Ready" state

  @critical
  Scenario Outline: PostgreSQL required tables exist
    When I query the PostgreSQL database for table "<table>"
    Then the table "<table>" should exist
    And the table should have encryption at rest enabled

    Examples:
      | table         |
      | customers     |
      | points_ledger |
      | tiers         |

  @high
  Scenario: Redis cache cluster is accessible with failover
    When I attempt to connect to the Redis cluster
    Then the connection should succeed
    And failover should be configured

  @critical
  Scenario Outline: Kafka topics are created
    When I list Kafka topics
    Then the topic "<topic>" should exist

    Examples:
      | topic           |
      | points.earned   |
      | points.redeemed |
      | points.expired  |
