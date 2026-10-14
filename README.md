# Restaurant Management & Billing System (Java OOP)

This repository contains the original console-based OOP project and an additional **database-backed Java Swing GUI**. The GUI is a separate entry point so the original console project and its tests remain intact.

## 1. Original console application (no database)

Requires JDK 8 or later.

```powershell
javac -d out src/RestaurantManagementSystem.java
java -cp out RestaurantManagementSystem
```

Run the existing regression tests:

```powershell
javac -encoding UTF-8 -d out src/RestaurantManagementSystem.java tests/RestaurantManagementSystemTest.java
java -cp out RestaurantManagementSystemTest
```

## 2. Swing GUI connected to MySQL

### Features
- Food menu seeded with vegetarian dishes, non-vegetarian dishes, beverages, and desserts.
- View menu items; add food, update prices, and remove items from the active menu.
- Create an order, select quantities, calculate subtotal/discount/tax/total, and show a receipt.
- Save completed orders and individual order lines in MySQL.
- Load saved order history after restarting the program.
- Cash, UPI, and card are **classroom simulations only**; no real payment is processed.
- SQL statements use prepared statements; saving an order and its lines uses a transaction.

### Step A — Create the database
1. Open MySQL Workbench and connect to your local MySQL Server.
2. Open and execute `database/restaurant_management.sql`.
3. Confirm the `restaurant_management` schema has `menu_items`, `orders`, and `order_items` tables.

The SQL seeds the initial menu. The application also lets you add menu items later.

### Step B — Add MySQL Connector/J
Download MySQL Connector/J from the official MySQL website and note the path to its JAR file, for example `mysql-connector-j-9.x.x.jar`. Do not commit the connector JAR or your database password to GitHub.

### Step C — Configure database credentials
The connection defaults are:
- URL: `jdbc:mysql://localhost:3306/restaurant_management?serverTimezone=Asia/Kolkata`
- User: `root`
- Password: empty string

If your MySQL account has a password, set environment variables in PowerShell before launching (replace the example password locally; do not share or commit it):

```powershell
$env:RESTAURANT_DB_URL = "jdbc:mysql://localhost:3306/restaurant_management?serverTimezone=Asia/Kolkata"
$env:RESTAURANT_DB_USER = "root"
$env:RESTAURANT_DB_PASSWORD = "YOUR_LOCAL_MYSQL_PASSWORD"
```

### Step D — Compile and run the GUI
Replace the example JAR path with the actual Connector/J path on your computer.

```powershell
javac -encoding UTF-8 -cp ".;C:\path\to\mysql-connector-j.jar" -d out src/RestaurantDatabase.java src/RestaurantManagementGUI.java
java -cp "out;C:\path\to\mysql-connector-j.jar" RestaurantManagementGUI
```

On macOS/Linux, use `:` instead of `; ` in the classpath separator.

### Database tables
- `menu_items`: active menu and prices.
- `orders`: customer, timestamp, subtotal, discount, tax, total, and payment method.
- `order_items`: item name/category/price snapshots and quantities, so older receipts remain accurate when menu prices change or an item is removed.

### Billing formula
1. Subtotal = sum of item price × quantity.
2. Discount = 10% when subtotal is at least ₹500; otherwise ₹0.
3. Tax = 5% of subtotal after discount.
4. Total = subtotal − discount + tax.

Example: subtotal ₹600.00, discount ₹60.00, tax ₹27.00, total ₹567.00.

## OOP concepts
Encapsulation, inheritance (`VegItem` and `NonVegItem` in the console model), abstraction, polymorphism, composition, and collections are demonstrated in the project. The GUI uses separate classes for its Swing presentation and JDBC persistence.

## Project structure
```
src/
  RestaurantManagementSystem.java       # Existing console version
  RestaurantManagementGUI.java          # Swing desktop GUI
  RestaurantDatabase.java               # JDBC database access
database/
  restaurant_management.sql             # Schema + initial menu data
tests/
  RestaurantManagementSystemTest.java   # Existing console model tests
```

## Important
The original console application still keeps menu changes and history in memory. Use `RestaurantManagementGUI` for the new MySQL-backed GUI. Configure credentials for your local server before running it. Payment selection is simulated only.
