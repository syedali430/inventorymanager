Feature: Inventory Manager

  Scenario: The initial state of the view
    Given the database contains an item with id "1" name "Monitor" quantity 2 price 199.99 description "HD monitor"
    When the Inventory Manager is shown
    Then the list contains an element with name "Monitor" quantity 2 price 199.99
