CREATE DATABASE IF NOT EXISTS restaurant_management
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE restaurant_management;

CREATE TABLE IF NOT EXISTS menu_items (
  item_id INT PRIMARY KEY AUTO_INCREMENT,
  item_name VARCHAR(100) NOT NULL,
  category ENUM('Vegetarian','Non-vegetarian','Beverage','Dessert') NOT NULL,
  price DECIMAL(10,2) NOT NULL CHECK (price > 0),
  active BOOLEAN NOT NULL DEFAULT TRUE,
  UNIQUE KEY uq_menu_item_name (item_name)
);

CREATE TABLE IF NOT EXISTS orders (
  order_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  customer_name VARCHAR(100) NOT NULL,
  customer_contact VARCHAR(30) NOT NULL DEFAULT 'N/A',
  order_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  subtotal DECIMAL(10,2) NOT NULL,
  discount DECIMAL(10,2) NOT NULL,
  tax DECIMAL(10,2) NOT NULL,
  total DECIMAL(10,2) NOT NULL,
  payment_method ENUM('Cash','UPI','Card') NOT NULL,
  status ENUM('COMPLETED') NOT NULL DEFAULT 'COMPLETED'
);

CREATE TABLE IF NOT EXISTS order_items (
  order_item_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  menu_item_id INT NULL,
  item_name VARCHAR(100) NOT NULL,
  category VARCHAR(30) NOT NULL,
  unit_price DECIMAL(10,2) NOT NULL,
  quantity INT NOT NULL CHECK (quantity > 0),
  line_total DECIMAL(10,2) NOT NULL,
  CONSTRAINT fk_order_items_order FOREIGN KEY (order_id)
    REFERENCES orders(order_id) ON DELETE CASCADE,
  CONSTRAINT fk_order_items_menu FOREIGN KEY (menu_item_id)
    REFERENCES menu_items(item_id) ON DELETE SET NULL
);

INSERT INTO menu_items (item_name, category, price) VALUES
('Idli (2 pcs)','Vegetarian',40.00),
('Plain Dosa','Vegetarian',70.00),
('Masala Dosa','Vegetarian',95.00),
('Onion Uttapam','Vegetarian',100.00),
('Veg Burger','Vegetarian',120.00),
('Paneer Wrap','Vegetarian',160.00),
('Veg Fried Rice','Vegetarian',140.00),
('Paneer Butter Masala','Vegetarian',220.00),
('Veg Biryani','Vegetarian',180.00),
('French Fries','Vegetarian',90.00),
('Chicken Burger','Non-vegetarian',190.00),
('Chicken Biryani','Non-vegetarian',280.00),
('Chicken 65','Non-vegetarian',220.00),
('Chicken Fried Rice','Non-vegetarian',200.00),
('Egg Fried Rice','Non-vegetarian',150.00),
('Grilled Chicken Sandwich','Non-vegetarian',180.00),
('Lime Soda','Beverage',50.00),
('Tea','Beverage',20.00),
('Coffee','Beverage',35.00),
('Fresh Lime Water','Beverage',40.00),
('Chocolate Brownie','Dessert',90.00),
('Vanilla Ice Cream','Dessert',70.00),
('Gulab Jamun (2 pcs)','Dessert',60.00)
ON DUPLICATE KEY UPDATE category=VALUES(category), price=VALUES(price), active=TRUE;
