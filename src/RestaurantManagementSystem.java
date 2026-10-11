import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Restaurant Management & Billing System.
 * Console-only Java application demonstrating core OOP concepts.
 * Compile: javac -d out src/RestaurantManagementSystem.java
 * Run:     java -cp out RestaurantManagementSystem
 */
public class RestaurantManagementSystem {
    private final Scanner scanner = new Scanner(System.in);
    private final Restaurant restaurant = new Restaurant("Campus Restaurant");
    private final List<Bill> completedBills = new ArrayList<>();

    public static void main(String[] args) {
        new RestaurantManagementSystem().run();
    }

    private void run() {
        seedMenu();
        System.out.println("==========================================");
        System.out.println("  " + restaurant.getName());
        System.out.println("  Restaurant Management & Billing System");
        System.out.println("==========================================");
        boolean running = true;
        while (running) {
            System.out.println("\n1. Display menu\n2. Add food item\n3. Update food item price"
                    + "\n4. Remove food item\n5. Create order and bill\n0. Exit");
            int choice = readInt("Choose an option: ", 0, 5);
            switch (choice) {
                case 1: restaurant.getMenu().display(); break;
                case 2: addFoodItem(); break;
                case 3: updateFoodPrice(); break;
                case 4: removeFoodItem(); break;
                case 5: createOrder(); break;
                case 6: viewOrderHistory(); break;
                case 0: running = false; System.out.println("Thank you. Goodbye!"); break;
                default: System.out.println("Invalid choice.");
            }
        }
        scanner.close();
    }

    private void seedMenu() {
        restaurant.getMenu().addItem(new VegItem(1, "Veg Burger", new BigDecimal("120.00")));
        restaurant.getMenu().addItem(new VegItem(2, "Paneer Wrap", new BigDecimal("160.00")));
        restaurant.getMenu().addItem(new NonVegItem(3, "Chicken Biryani", new BigDecimal("280.00")));
        restaurant.getMenu().addItem(new NonVegItem(4, "Chicken Burger", new BigDecimal("190.00")));
        restaurant.getMenu().addItem(new VegItem(5, "Lime Soda", new BigDecimal("50.00")));
    }

    private void addFoodItem() {
        int id = readInt("New item ID: ", 1, Integer.MAX_VALUE);
        if (restaurant.getMenu().findById(id) != null) {
            System.out.println("That ID already exists."); return;
        }
        String name = readNonEmpty("Food name: ");
        BigDecimal price = readMoney("Price (INR): ");
        int type = readInt("Type (1 = Vegetarian, 2 = Non-vegetarian): ", 1, 2);
        FoodItem item = type == 1 ? new VegItem(id, name, price) : new NonVegItem(id, name, price);
        restaurant.getMenu().addItem(item);
        System.out.println("Item added.");
    }

    private void updateFoodPrice() {
        restaurant.getMenu().display();
        int id = readInt("Item ID to update: ", 1, Integer.MAX_VALUE);
        FoodItem item = restaurant.getMenu().findById(id);
        if (item == null) { System.out.println("Item not found."); return; }
        item.setPrice(readMoney("New price (INR): "));
        System.out.println("Price updated.");
    }

    private void removeFoodItem() {
        restaurant.getMenu().display();
        int id = readInt("Item ID to remove: ", 1, Integer.MAX_VALUE);
        System.out.println(restaurant.getMenu().removeItem(id) ? "Item removed." : "Item not found.");
    }

    private void viewOrderHistory() {
        if (completedBills.isEmpty()) {
            System.out.println("No completed orders in this session yet.");
            return;
        }
        System.out.println("\\n================ ORDER HISTORY ================");
        for (Bill bill : completedBills) {
            System.out.printf("Order #%d | %s | Customer: %s | Total: ₹%s | Payment: %s%n",
                    bill.getOrderNumber(), bill.getCreatedAt(), bill.getCustomerName(),
                    bill.getTotal().toPlainString(), bill.getPaymentMethod());
        }
        System.out.println("Note: order history is kept in memory for this session only.");
    }

    private void createOrder() {
        if (restaurant.getMenu().isEmpty()) { System.out.println("Menu is empty."); return; }
        String name = readNonEmpty("Customer name: ");
        String phone = readNonEmpty("Customer contact (or 'N/A'): ");
        Customer customer = new Customer(name, phone);
        Order order = new Order(customer);
        boolean adding = true;
        while (adding) {
            restaurant.getMenu().display();
            int id = readInt("Enter item ID (0 to finish order): ", 0, Integer.MAX_VALUE);
            if (id == 0) break;
            FoodItem item = restaurant.getMenu().findById(id);
            if (item == null) { System.out.println("Item not found. Try again."); continue; }
            int quantity = readInt("Quantity: ", 1, 999);
            order.addItem(item, quantity);
            System.out.println("Added " + quantity + " x " + item.getName());
            int more = readInt("Add another item? (1 = Yes, 0 = No): ", 0, 1);
            adding = more == 1;
        }
        if (order.getItems().isEmpty()) { System.out.println("No items selected; order cancelled."); return; }
        Bill bill = new Bill(order);
        bill.print();
        System.out.println("\nPayment methods: 1. Cash  2. UPI  3. Card");
        int method = readInt("Select payment method: ", 1, 3);
        Payment payment;
        if (method == 1) payment = new CashPayment();
        else if (method == 2) payment = new UPIPayment();
        else payment = new CardPayment();
        payment.process(bill.getTotal());
        bill.setPaymentMethod(payment.getMethodName());
        completedBills.add(bill);
        System.out.println("Order #" + bill.getOrderNumber() + " recorded in this session.");
        System.out.println("Note: this is a classroom simulation; no real money was transferred.");
    }

    private int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) return value;
            } catch (NumberFormatException ignored) { }
            System.out.println("Enter a whole number from " + min + " to " + max + ".");
        }
    }

    private BigDecimal readMoney(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                BigDecimal amount = new BigDecimal(input).setScale(2, RoundingMode.HALF_UP);
                if (amount.signum() > 0) return amount;
            } catch (NumberFormatException ignored) { }
            System.out.println("Enter a valid amount greater than zero (for example, 125.00).");
        }
    }

    private String readNonEmpty(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            if (!value.isEmpty()) return value;
            System.out.println("This field cannot be empty.");
        }
    }
}

abstract class FoodItem {
    private final int id;
    private String name;
    private BigDecimal price;

    protected FoodItem(int id, String name, BigDecimal price) {
        if (id <= 0) throw new IllegalArgumentException("ID must be positive.");
        this.id = id;
        setName(name);
        setPrice(price);
    }
    public int getId() { return id; }
    public String getName() { return name; }
    public BigDecimal getPrice() { return price; }
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Name is required.");
        this.name = name.trim();
    }
    public void setPrice(BigDecimal price) {
        if (price == null || price.signum() <= 0) throw new IllegalArgumentException("Price must be positive.");
        this.price = price.setScale(2, RoundingMode.HALF_UP);
    }
    public abstract String getCategory();
    @Override public String toString() {
        return String.format("%-4d %-24s %-18s ₹%s", id, name, getCategory(), price.toPlainString());
    }
}
class VegItem extends FoodItem {
    public VegItem(int id, String name, BigDecimal price) { super(id, name, price); }
    @Override public String getCategory() { return "Vegetarian"; }
}
class NonVegItem extends FoodItem {
    public NonVegItem(int id, String name, BigDecimal price) { super(id, name, price); }
    @Override public String getCategory() { return "Non-vegetarian"; }
}

class Menu {
    private final Map<Integer, FoodItem> items = new LinkedHashMap<>();
    public void addItem(FoodItem item) {
        if (items.containsKey(item.getId())) throw new IllegalArgumentException("Duplicate item ID.");
        items.put(item.getId(), item);
    }
    public FoodItem findById(int id) { return items.get(id); }
    public boolean removeItem(int id) { return items.remove(id) != null; }
    public boolean isEmpty() { return items.isEmpty(); }
    public List<FoodItem> getItems() { return new ArrayList<>(items.values()); }
    public void display() {
        System.out.println("\nID   Food                     Category           Price");
        System.out.println("---------------------------------------------------------------");
        if (items.isEmpty()) { System.out.println("Menu is empty."); return; }
        for (FoodItem item : items.values()) System.out.println(item);
    }
}
class Customer {
    private final String name;
    private final String contact;
    public Customer(String name, String contact) {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Customer name is required.");
        this.name = name.trim();
        this.contact = contact == null ? "N/A" : contact.trim();
    }
    public String getName() { return name; }
    public String getContact() { return contact; }
}
class OrderItem {
    private final FoodItem foodItem;
    private final int quantity;
    public OrderItem(FoodItem foodItem, int quantity) {
        if (foodItem == null) throw new IllegalArgumentException("Food item is required.");
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive.");
        this.foodItem = foodItem;
        this.quantity = quantity;
    }
    public FoodItem getFoodItem() { return foodItem; }
    public int getQuantity() { return quantity; }
    public BigDecimal getLineTotal() {
        return foodItem.getPrice().multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
    }
}
class Order {
    private final Customer customer;
    private final List<OrderItem> items = new ArrayList<>();
    public Order(Customer customer) {
        if (customer == null) throw new IllegalArgumentException("Customer is required.");
        this.customer = customer;
    }
    public Customer getCustomer() { return customer; }
    public List<OrderItem> getItems() { return new ArrayList<>(items); }
    public void addItem(FoodItem item, int quantity) { items.add(new OrderItem(item, quantity)); }
    public BigDecimal getSubtotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem item : items) total = total.add(item.getLineTotal());
        return total.setScale(2, RoundingMode.HALF_UP);
    }
}
class Bill {
    private static int nextOrderNumber = 1001;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("500.00");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal TAX_RATE = new BigDecimal("0.05");
    private final Order order;
    private final int orderNumber;
    private final LocalDateTime createdAt;
    private String paymentMethod = "Not recorded";
    private final BigDecimal subtotal;
    private final BigDecimal discount;
    private final BigDecimal tax;
    private final BigDecimal total;

    public Bill(Order order) {
        if (order == null || order.getItems().isEmpty()) throw new IllegalArgumentException("Cannot bill an empty order.");
        this.order = order;
        this.orderNumber = nextOrderNumber++;
        this.createdAt = LocalDateTime.now();
        this.subtotal = order.getSubtotal();
        this.discount = subtotal.compareTo(DISCOUNT_THRESHOLD) >= 0
                ? subtotal.multiply(DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO.setScale(2);
        BigDecimal taxableAmount = subtotal.subtract(discount);
        this.tax = taxableAmount.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        this.total = taxableAmount.add(tax).setScale(2, RoundingMode.HALF_UP);
    }
    public int getOrderNumber() { return orderNumber; }
    public String getCreatedAt() { return createdAt.format(DATE_FORMAT); }
    public String getCustomerName() { return order.getCustomer().getName(); }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) {
        if (paymentMethod == null || paymentMethod.trim().isEmpty()) throw new IllegalArgumentException("Payment method is required.");
        this.paymentMethod = paymentMethod;
    }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getDiscount() { return discount; }
    public BigDecimal getTax() { return tax; }
    public BigDecimal getTotal() { return total; }
    public void print() {
        System.out.println("\n===================== CUSTOMER BILL =====================");
        System.out.println("Customer: " + order.getCustomer().getName());
        System.out.println("Contact : " + order.getCustomer().getContact());
        System.out.println("---------------------------------------------------------");
        System.out.printf("%-24s %8s %8s %12s%n", "Item", "Price", "Qty", "Amount");
        for (OrderItem line : order.getItems()) {
            System.out.printf("%-24s %8s %8d %12s%n", line.getFoodItem().getName(),
                    line.getFoodItem().getPrice().toPlainString(), line.getQuantity(), line.getLineTotal().toPlainString());
        }
        System.out.println("---------------------------------------------------------");
        System.out.printf("%-35s ₹%10s%n", "Subtotal:", subtotal.toPlainString());
        System.out.printf("%-35s ₹%10s%n", "Discount:", discount.toPlainString());
        System.out.printf("%-35s ₹%10s%n", "Tax (5% after discount):", tax.toPlainString());
        System.out.printf("%-35s ₹%10s%n", "TOTAL:", total.toPlainString());
        System.out.println("=========================================================");
    }
}
interface Payment {
    void process(BigDecimal amount);
    String getMethodName();
}
class CashPayment implements Payment {
    @Override public void process(BigDecimal amount) { System.out.println("Cash selected. Amount due: ₹" + amount.toPlainString()); }
    @Override public String getMethodName() { return "Cash"; }
}
class UPIPayment implements Payment {
    @Override public void process(BigDecimal amount) { System.out.println("UPI selected. Simulated amount: ₹" + amount.toPlainString()); }
    @Override public String getMethodName() { return "UPI"; }
}
class CardPayment implements Payment {
    @Override public void process(BigDecimal amount) { System.out.println("Card selected. Simulated amount: ₹" + amount.toPlainString()); }
    @Override public String getMethodName() { return "Card"; }
}
class Restaurant {
    private final String name;
    private final Menu menu = new Menu();
    public Restaurant(String name) { this.name = name; }
    public String getName() { return name; }
    public Menu getMenu() { return menu; }
}
