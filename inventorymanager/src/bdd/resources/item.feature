Feature: Inventory Manager

  Scenario: The initial state of the view
    Given the database contains the items with the following values
      | 1 | Monitor  | 2 | 199.99 | HD monitor |
      | 2 | Keyboard | 3 | 49.99  | Mechanical |
    When the Inventory Manager is shown
    Then the list contains elements with the following values
      | Monitor  | 2 | 199.99 |
      | Keyboard | 3 | 49.99  |
