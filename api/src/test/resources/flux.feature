@account
Feature: Flux


      Scenario: Flux
        # Create environment envOne
        Given a REST client authenticated as admin with password pass
        When request json body has:
          | name  | envOne |
        And send POST /environment
        Then the response status is 201
        # Create project projOne
        And a REST client authenticated as admin with password pass
        When request json body has:
          | name  | projOne |
        And send POST /project
        Then the response status is 201
        # Create balance policy balancePolicyOne
        And a REST client authenticated as admin with password pass
        When request json body has:
          | name  | balancePolicyOne |
        And send POST /balancepolicy
        Then the response status is 201





