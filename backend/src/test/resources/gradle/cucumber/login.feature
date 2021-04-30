Feature: U1 - Log in

  Scenario: Valid login details.
    Given The user's details exist in the database.
    When The user supplies an email and password which matches the details in the database
    And attempts to log in
    Then They should be logged in

  Scenario: User email does not exist.
    Given The user is not existing in the database.
    When The user enters an email that is not registered
    And any password is supplied
    And attempts to log in
    Then They should not be logged in
    And An error message stating the email or password is incorrect is displayed

  Scenario: Incorrect password entered
    Given The user exists in the database
    When The user enters a registered email
    And An incorrect password is supplied
    And attempts to log in
    Then An error message stating the email or password is incorrect is displayed

  Scenario: No email entered
    Given No email is entered in the login page
    When The user attempts to login
    Then An error message stating the email or password is incorrect is displayed

  Scenario: No password entered
    Given No password is entered in the login page
    When The user attempts to login
    Then An error message stating the email or password is incorrect is displayed


