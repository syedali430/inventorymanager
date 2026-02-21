Feature: Inventory View High Level
  Specifications of the behavior of the Inventory View

  Background: 
    Given The database contains a few items
    And The Inventory View is shown

  Scenario: Add a new item
    Given The user provides item data in the text fields
    When The user clicks the "Add Item" button
    Then The list contains the new inventory item

  Scenario: Add a new item with an existing id
    Given The user provides item data in the text fields, specifying an existing id
    When The user clicks the "Add Item" button
    Then An error is shown containing the name of the existing item

  Scenario: Update an existing item
    Given The database contains the items with the following values
      | id | name   | quantity | price | description   |
      |  1 | Laptop |       10 | 999.9 | Gaming Laptop |
    And The user selects the item with id "1" from the list
    When The user updates the item details with the following values
      | id | name           | quantity | price | description             |
      |  1 | Updated Laptop |       15 | 899.9 | High-performance laptop |
    And The user clicks the "Update Selected" button
    Then The list reflects the updated details for the item with id "1"

  Scenario: Delete a item
    Given The user selects an item from the list
    When The user clicks the "Delete Selected" button
    Then The item is removed from the list

  Scenario: Delete a not existing item
    Given The user selects an item from the list
    But The item is in the meantime removed from the database
    When The user clicks the "Delete Selected" button
    Then An error is shown containing the name of the selected item
    And The item is removed from the list
