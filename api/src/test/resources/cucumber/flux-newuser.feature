@active
Feature: Flux
  Background: Flux
    Given a REST client authenticated as adminTeamOne with password pass
    When request json body has:
      | name  | envOne |
    And send POST /environment
    Then the response status is 201

    Given a REST client authenticated as adminTeamOne with password pass
    When request json body has:
      | name  | balancePolicyOne |
    And send POST /balancepolicy
    Then the response status is 201

    Given a REST client authenticated as adminTeamOne with password pass
    When request json body has:
      | username    | accountOne              |
      | email    	| accountone@test.com     |
    And send POST /account
    Then the response status is 201
    When request json body has:
      | name     | teamOne      |
    And send POST /team
    Then the response status is 201
    When request json body has:
      | name  | projOne 	   |
      | teams | [Team=teamOne] |
    And send POST /project
    Then the response status is 201

  Scenario: validate permissions for Account with role default
	Given a REST client authenticated as user1 with password ""
	Then the response status is 200
	And send GET Account=user1
	Then the response status is 200
	And send GET Account=accountOne
	Then the response status is 200
	And send DELETE Account=accountOne
	Then the response status is 403
	When request json body has:
	 | username     | accountTwo              |
	 | email        | accounttwo@test.com     |
	And send POST /account
	Then the response status is 403
	When request json body has:
	 | username   | accountThree           |
	 | email      | accountthree@test.com  |
	And send PUT Account=accountTwo
	Then the response status is 403
	
  Scenario: validate permissions for Team with role default
	Given a REST client authenticated as user2 with password ""
	When request json body has:
	  | name     | teamUser2   |
	And send POST /team
	Then the response status is 201
	Given a REST client authenticated as user1 with password ""
	When request json body has:
	  | name     | teamTwo              |
	  | accounts | [Account=user1]      |
	And send POST /team
	Then the response status is 201
	When request json body has:
	  | name  | projTwo 									 |
	  | teams | [Team=teamTwo, Team=teamUser2, Team=teamOne] |
	And send POST /project
	Then the response status is 201
	And send GET Team=teamTwo
	Then the response status is 200
	And send GET Team=teamOne
	Then the response status is 200
	When request json body has:
	  | name     | teamTwo   |
	And send PUT Team=teamTwo
	Then the response status is 200
	When request json body has:
	  | name     | teamOne   |
	And send PUT Team=teamOne
	Then the response status is 403
	And send DELETE Project=projTwo

  Scenario: validate permissions for Project with role default
    Given a REST client authenticated as user1 with password ""
    When request json body has:
      | name     | teamTwo           |
      | accounts | [Account=user1]   |
    And send POST /team
    Then the response status is 201
    When request json body has:
      | name  | projTwo        |
      | teams | [Team=teamTwo] |
    And send POST /project
    Then the response status is 201
    And send GET /balancepolicy
    Then the response status is 200
    And the response search at '_embedded.balancepolicy[0].name' equal to balancePolicyOne
    When request json body has:
      | name  		   | poolOne 			  			|
      | environment    | Environment=EnvOne 			|
      | balancepolicy  | BalancePolicy=balancePolicyOne |
      | project  	   | Project=projTwo 				|
    And send POST /pool
    Then the response status is 201
    And send GET /pool
    Then the response status is 200
    And the response search at '_embedded.pool[0].name' equal to poolOne
    When request json body has:
      | name  		 | ruleOne 		   |
      | matching 	 | / 			   |
      | pools  		 | [Pool=poolOne]  |
      | project 	 | Project=projTwo |
    And send POST /rule
    Then the response status is 201
    And send GET /rule
    Then the response status is 200
    And the response search at '_embedded.rule[0].name' equal to ruleOne
    When request json body has:
      | name  	 	  | vhOne 				 |
      | project  	  | Project=projTwo 	 |
      | environments  | [Environment=EnvOne] |
    And send POST /virtualhost
    Then the response status is 201
    And send GET /virtualhost
    Then the response status is 200
    And the response search at '_embedded.virtualhost[0].name' equal to vhOne
    When request json body has:
      | order 			 | 1     						|
      | rule 			 | Rule=ruleOne			 		|
      | environment 	 | Environment=EnvOne 			|
      | virtualhostgroup | VirtualhostGroup=vhOne	    |
    And send POST /ruleordered
    Then the response status is 201
    And send GET /ruleordered
    Then the response status is 200
    And the response search at '_embedded.ruleordered[0].order' has number 1
    When request json body has:
      | name  | targetOne 	 |
      | pool  | Pool=poolOne |
    And send POST /target
    Then the response status is 201
    And send GET /target
    Then the response status is 200
    And the response search at '_embedded.target[0].name' equal to targetOne
    When request json body has:
      | name  | targetTwo 	 |
      | pool  | Pool=poolOne |
    And send POST /target
    Then the response status is 201
    And send DELETE Target=targetTwo
    Then the response status is 204
    And send DELETE /ruleordered/1
    Then the response status is 204
    And send DELETE VirtualHost=vhOne
    Then the response status is 204
    And send DELETE Rule=ruleOne
    Then the response status is 204
    
    Given a REST client authenticated as user1 with password ""
    Then the response status is 200
    And send DELETE Pool=poolOne
    