# Class Design

```mermaid
classDiagram
    class FoodItem {
      <<abstract>>
      -int id
      -String name
      -BigDecimal price
      +getCategory()*
    }
    class VegItem
    class NonVegItem
    FoodItem <|-- VegItem
    FoodItem <|-- NonVegItem

    class Menu {
      -Map~Integer,FoodItem~ items
      +addItem(FoodItem)
      +findById(int) FoodItem
      +removeItem(int) boolean
      +display()
    }
    Menu o-- FoodItem

    class Customer {
      -String name
      -String contact
    }
    class Order {
      -Customer customer
      -List~OrderItem~ items
      +addItem(FoodItem,int)
      +getSubtotal() BigDecimal
    }
    class OrderItem {
      -FoodItem foodItem
      -int quantity
      +getLineTotal() BigDecimal
    }
    Order --> Customer
    Order o-- OrderItem
    OrderItem --> FoodItem

    class Bill {
      -Order order
      -BigDecimal subtotal
      -BigDecimal discount
      -BigDecimal tax
      -BigDecimal total
      +print()
    }
    Bill --> Order

    class Payment {
      <<interface>>
      +process(BigDecimal)
      +getMethodName() String
    }
    class CashPayment
    class UPIPayment
    class CardPayment
    Payment <|.. CashPayment
    Payment <|.. UPIPayment
    Payment <|.. CardPayment

    class Restaurant {
      -String name
      -Menu menu
    }
    Restaurant o-- Menu
    class RestaurantManagementSystem
    RestaurantManagementSystem --> Restaurant
```

The main class currently contains the console controller and menu actions. The project is deliberately kept in one Java source file for easy compilation in a beginner environment; all supporting classes are package-private classes in that file.