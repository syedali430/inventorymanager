Feature: Inventory View
  Specifications of the behavior of the Inventory View

  Scenario: The initial state of the view
    Given The database contains the items with the following values
      | id | name   | quantity | price | description   |
      |  1 | Laptop |       10 | 999.9 | Gaming Laptop |
      |  2 | Mobile |        5 | 599.9 | Smart Phone   |
    When The Inventory View is shown
    Then The list contains elements with the following values
      | 1 | Laptop | 10 | 999.9 | Gaming Laptop |
      | 2 | Mobile |  5 | 599.9 | Smart Phone   |

  Scenario: Add a new item
    Given The Inventory View is shown
    When The user enters the following values in the text fields
      | id | name   | quantity | price | description   |
      |  1 | Laptop |        5 | 899.9 | Simple laptop |
    And The user clicks the "Add Item" button
    Then The list contains elements with the following values
      | 1 | Laptop | 5 | 899.9 | Simple laptop |

  Scenario: Add a new item with an existing id
    Given The database contains the items with the following values
      | id | name   | quantity | price | description   |
      |  1 | Laptop |        5 | 899.9 | Simple laptop |
    And The Inventory View is shown
    When The user enters the following values in the text fields
      | id | name       | quantity | price | description   |
      |  1 | New Laptop |       15 | 899.9 | Simple laptop |
    And The user clicks the "Add Item" button
    Then An error is shown containing the following values
      | 1 | Laptop | 5 | 899.9 | Simple laptop |
