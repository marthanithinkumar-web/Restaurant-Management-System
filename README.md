# Restaurant Management & Billing System (Java OOP)

A console-based college project built using **standard Java only**. No web framework, JavaScript, database, or external library is required.

## Requirements
- JDK 8 or later (JDK 11+ recommended)
- Terminal/Command Prompt or any Java IDE

## Run from the repository root

### Windows PowerShell / Command Prompt
```powershell
javac -d out src/RestaurantManagementSystem.java
java -cp out RestaurantManagementSystem
```

### macOS / Linux
```bash
mkdir -p out
javac -d out src/RestaurantManagementSystem.java
java -cp out RestaurantManagementSystem
```

## Features
- Display, add, update, and remove menu items
- Vegetarian and non-vegetarian item subclasses
- Customer orders with multiple items and quantities
- Itemized bill with unique order number and date/time
- Completed order history for the current application session
- 10% discount when subtotal is at least ₹500
- 5% tax calculated after discount
- Currency calculations use `BigDecimal`
- Cash, UPI, and card payment selection (simulation only)
- Input validation and retry on invalid console input

## OOP concepts demonstrated
- **Encapsulation:** private fields and controlled accessors/mutators
- **Inheritance:** `VegItem` and `NonVegItem` extend `FoodItem`
- **Abstraction:** abstract `FoodItem` and `Payment` interface
- **Polymorphism:** payment implementations used through the `Payment` interface
- **Method overriding:** category and payment behaviour
- **Collections:** `ArrayList` and `LinkedHashMap`
- **Composition:** orders contain order lines; restaurant owns a menu

## Billing formula
1. Subtotal = sum of each item price × quantity.
2. Discount = 10% of subtotal if subtotal ≥ ₹500; otherwise ₹0.
3. Tax = 5% × (subtotal − discount).
4. Total = subtotal − discount + tax.

Example for a ₹600 subtotal: discount ₹60.00, taxable amount ₹540.00, tax ₹27.00, final total ₹567.00.

## Project structure
```
Restaurant-Management-System/
├── src/
│   └── RestaurantManagementSystem.java
├── docs/
│   ├── ALGORITHM.md
│   └── CLASS_DIAGRAM.md
├── README.md
└── TESTING.md
```

## Important
Payment choices are simulated; this program does not transfer money. Menu changes and order history are held in memory and are not saved after exit. Order numbers start at 1001 each time the application starts.