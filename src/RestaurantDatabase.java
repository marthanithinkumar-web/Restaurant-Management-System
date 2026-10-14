import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC persistence layer for the Restaurant Management & Billing System.
 * Add MySQL Connector/J to the project's classpath before running.
 *
 * Configure with environment variables:
 * RESTAURANT_DB_URL (default jdbc:mysql://localhost:3306/restaurant_management)
 * RESTAURANT_DB_USER (default root)
 * RESTAURANT_DB_PASSWORD (no default)
 */
public final class RestaurantDatabase {
    private RestaurantDatabase() {}

    private static String env(String key, String fallback) {
        String value = System.getenv(key);
        return value == null ? fallback : value;
    }

    public static Connection connect() throws SQLException {
        String url = env("RESTAURANT_DB_URL",
                "jdbc:mysql://localhost:3306/restaurant_management?serverTimezone=Asia/Kolkata");
        String user = env("RESTAURANT_DB_USER", "root");
        String password = env("RESTAURANT_DB_PASSWORD", "");
        return DriverManager.getConnection(url, user, password);
    }

    public static final class MenuRow {
        public final int id;
        public final String name;
        public final String category;
        public final BigDecimal price;
        public MenuRow(int id, String name, String category, BigDecimal price) {
            this.id = id; this.name = name; this.category = category; this.price = price;
        }
    }

    public static List<MenuRow> loadMenu() throws SQLException {
        String sql = "SELECT item_id, item_name, category, price FROM menu_items WHERE active = TRUE ORDER BY category, item_name";
        List<MenuRow> rows = new ArrayList<>();
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) rows.add(new MenuRow(rs.getInt("item_id"),
                    rs.getString("item_name"), rs.getString("category"), rs.getBigDecimal("price")));
        }
        return rows;
    }

    public static int addMenuItem(String name, String category, BigDecimal price) throws SQLException {
        String sql = "INSERT INTO menu_items (item_name, category, price) VALUES (?, ?, ?)";
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name.trim());
            ps.setString(2, category);
            ps.setBigDecimal(3, price);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("MySQL did not return a menu item ID.");
    }

    public static void updateMenuPrice(int id, BigDecimal price) throws SQLException {
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE menu_items SET price = ? WHERE item_id = ? AND active = TRUE")) {
            ps.setBigDecimal(1, price);
            ps.setInt(2, id);
            if (ps.executeUpdate() == 0) throw new SQLException("Menu item not found: " + id);
        }
    }

    /** Soft-delete keeps old order references and receipts valid. */
    public static void removeMenuItem(int id) throws SQLException {
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE menu_items SET active = FALSE WHERE item_id = ? AND active = TRUE")) {
            ps.setInt(1, id);
            if (ps.executeUpdate() == 0) throw new SQLException("Menu item not found: " + id);
        }
    }

    /**
     * Saves the completed bill and all its item snapshots in one transaction.
     * Call after the payment choice is selected in the GUI.
     */
    public static long saveCompletedOrder(String customerName, String contact,
            BigDecimal subtotal, BigDecimal discount, BigDecimal tax, BigDecimal total,
            String paymentMethod, List<LineToSave> lines) throws SQLException {
        if (lines == null || lines.isEmpty()) throw new IllegalArgumentException("Order must contain items.");
        String orderSql = "INSERT INTO orders (customer_name, customer_contact, subtotal, discount, tax, total, payment_method) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String lineSql = "INSERT INTO order_items (order_id, menu_item_id, item_name, category, unit_price, quantity, line_total) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = connect()) {
            c.setAutoCommit(false);
            try {
                long orderId;
                try (PreparedStatement ps = c.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, customerName.trim());
                    ps.setString(2, contact == null || contact.trim().isEmpty() ? "N/A" : contact.trim());
                    ps.setBigDecimal(3, subtotal);
                    ps.setBigDecimal(4, discount);
                    ps.setBigDecimal(5, tax);
                    ps.setBigDecimal(6, total);
                    ps.setString(7, paymentMethod);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("MySQL did not return the order ID.");
                        orderId = keys.getLong(1);
                    }
                }
                try (PreparedStatement ps = c.prepareStatement(lineSql)) {
                    for (LineToSave line : lines) {
                        ps.setLong(1, orderId);
                        if (line.menuItemId > 0) ps.setInt(2, line.menuItemId);
                        else ps.setNull(2, Types.INTEGER);
                        ps.setString(3, line.itemName);
                        ps.setString(4, line.category);
                        ps.setBigDecimal(5, line.unitPrice);
                        ps.setInt(6, line.quantity);
                        ps.setBigDecimal(7, line.lineTotal);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                c.commit();
                return orderId;
            } catch (SQLException | RuntimeException ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    public static final class LineToSave {
        public final int menuItemId;
        public final String itemName;
        public final String category;
        public final BigDecimal unitPrice;
        public final int quantity;
        public final BigDecimal lineTotal;
        public LineToSave(int menuItemId, String itemName, String category,
                BigDecimal unitPrice, int quantity, BigDecimal lineTotal) {
            this.menuItemId = menuItemId; this.itemName = itemName; this.category = category;
            this.unitPrice = unitPrice; this.quantity = quantity; this.lineTotal = lineTotal;
        }
    }

    public static ResultSet openOrderHistory(Connection connection) throws SQLException {
        // Caller owns and must close the Connection, Statement, and ResultSet.
        Statement statement = connection.createStatement();
        return statement.executeQuery(
                "SELECT order_id, order_time, customer_name, customer_contact, subtotal, discount, tax, total, payment_method FROM orders ORDER BY order_time DESC");
    }
}
