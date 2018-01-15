#@account
#Feature: Account Support
#    The manager have than
#    to support REST standard
#
#    Background:
#      Given a REST client authenticated as admin with password pass
#      When request json body has:
#        | name  | teamOne |
#      And send POST /team
#      Then the response status is 201
#      And a REST client authenticated as admin with password pass
#      When request json body has:
#        | name     | accountOne                  |
#        | password | password                    |
#        | email    | test@test.com               |
#        | roles    | [ ROLE_USER ]               |
#        | teams    | [ http://localhost/team/1 ] |
#      And send POST /account
#      Then the response status is 201
#
#
#    Scenario: Create Account
#      Then the response status is 201
#
#
#    Scenario: Get existent Account
#      Given a REST client authenticated as admin with password pass
#      When send GET /account/
#      Then the response status is 201
#
#  Scenario: Get null Account
#      Given a REST client authenticated as admin with password pass
#      When send GET /account/4
#      Then the response status is 404
