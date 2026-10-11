import java.math.BigDecimal;

/**
 * Dependency-free regression tests for the restaurant domain model.
 * Run with: java -cp out RestaurantManagementSystemTest
 */
public class RestaurantManagementSystemTest {
    private static int passed = 0;

    public static void main(String[] args) {
        testFoodItemValidation();
        testMenuOperations();
        testOrderSubtotal();
        testOrderItemPriceSnapshot();
        testBillingBelowDiscountThreshold();
        testBillingAtDiscountThreshold();
        testBillingAboveDiscountThreshold();
        testPaymentMethodRecording();
        System.out.println("PASS: " + passed + " regression tests");
    }

    private static void testFoodItemValidation() {
        FoodItem item = new VegItem(1, "  Idli  ", new BigDecimal("45"));
        check("Idli".equals(item.getName()), "food name is trimmed");
        check(new BigDecimal("45.00").compareTo(item.getPrice()) == 0, "price uses two decimal places");
        expectIllegalArgument(() -> new VegItem(0, "Bad ID", new BigDecimal("10")), "non-positive food ID rejected");
        expectIllegalArgument(() -> new VegItem(2, " ", new BigDecimal("10")), "blank food name rejected");
        expectIllegalArgument(() -> new VegItem(3, "Bad price", BigDecimal.ZERO), "zero price rejected");
    }

    private static void testMenuOperations() {
        Menu menu = new Menu();
        FoodItem item = new VegItem(1, "Dosa", new BigDecimal("80"));
        menu.addItem(item);
        check(menu.findById(1) == item, "menu finds an item by ID");
        expectIllegalArgument(() -> menu.addItem(new VegItem(1, "Duplicate", new BigDecimal("90"))),
                "duplicate item ID rejected");
        check(menu.removeItem(1), "existing menu item removed");
        check(!menu.removeItem(99), "unknown menu item is not removed");
        check(menu.isEmpty(), "menu empty state is correct");
    }

    private static void testOrderSubtotal() {
        Customer customer = new Customer("Test Customer", "N/A");
        Order order = new Order(customer);
        order.addItem(new VegItem(1, "Idli", new BigDecimal("40")), 2);
        order.addItem(new VegItem(2, "Tea", new BigDecimal("20")), 1);
        checkMoney("100.00", order.getSubtotal(), "order subtotal combines quantities and lines");
        checkMoney("80.00", order.getItems().get(0).getLineTotal(), "line total uses quantity");
    }

    private static void testOrderItemPriceSnapshot() {
        FoodItem menuItem = new VegItem(1, "Dosa", new BigDecimal("80.00"));
        Order order = new Order(new Customer("Snapshot Test", "N/A"));
        order.addItem(menuItem, 2);
        Bill bill = new Bill(order);

        menuItem.setName("Premium Dosa");
        menuItem.setPrice(new BigDecimal("125.00"));

        OrderItem line = order.getItems().get(0);
        check("Dosa".equals(line.getItemName()), "order line preserves item name at order time");
        checkMoney("80.00", line.getUnitPrice(), "order line preserves unit price at order time");
        checkMoney("160.00", line.getLineTotal(), "line total remains stable after menu price changes");
        checkMoney("160.00", bill.getSubtotal(), "bill subtotal remains stable after menu price changes");
    }

    private static void testBillingBelowDiscountThreshold() {
        Bill bill = billFor("400.00");
        checkMoney("0.00", bill.getDiscount(), "no discount below ₹500");
        checkMoney("20.00", bill.getTax(), "tax calculated on ₹400");
        checkMoney("420.00", bill.getTotal(), "total below discount threshold");
    }

    private static void testBillingAtDiscountThreshold() {
        Bill bill = billFor("500.00");
        checkMoney("50.00", bill.getDiscount(), "10% discount at ₹500 threshold");
        checkMoney("22.50", bill.getTax(), "tax calculated after discount");
        checkMoney("472.50", bill.getTotal(), "total at discount threshold");
    }

    private static void testBillingAboveDiscountThreshold() {
        Bill bill = billFor("600.00");
        checkMoney("60.00", bill.getDiscount(), "10% discount above threshold");
        checkMoney("27.00", bill.getTax(), "tax calculated after discount");
        checkMoney("567.00", bill.getTotal(), "total above discount threshold");
        check(bill.getOrderNumber() >= 1001, "order number is assigned");
        check(!bill.getCreatedAt().isBlank(), "creation date/time is assigned");
    }

    private static void testPaymentMethodRecording() {
        Bill bill = billFor("100.00");
        bill.setPaymentMethod("UPI");
        check("UPI".equals(bill.getPaymentMethod()), "payment method is recorded");
        expectIllegalArgument(() -> bill.setPaymentMethod(" "), "blank payment method rejected");
    }

    private static Bill billFor(String subtotal) {
        Order order = new Order(new Customer("Regression Test", "N/A"));
        order.addItem(new VegItem(1, "Test item", new BigDecimal(subtotal)), 1);
        return new Bill(order);
    }

    private static void checkMoney(String expected, BigDecimal actual, String description) {
        check(new BigDecimal(expected).compareTo(actual) == 0, description + " (expected " + expected + ", got " + actual + ")");
    }

    private static void expectIllegalArgument(Runnable action, String description) {
        try {
            action.run();
            throw new AssertionError("FAIL: " + description + " (no exception)");
        } catch (IllegalArgumentException expected) {
            passed++;
            System.out.println("PASS: " + description);
        }
    }

    private static void check(boolean condition, String description) {
        if (!condition) throw new AssertionError("FAIL: " + description);
        passed++;
        System.out.println("PASS: " + description);
    }
}
