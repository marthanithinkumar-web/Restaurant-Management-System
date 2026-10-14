import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class RestaurantManagementGUI extends JFrame {
    private final DefaultTableModel menuModel = new DefaultTableModel(new Object[]{"ID","Food","Category","Price (₹)"},0) {
        @Override public boolean isCellEditable(int row,int col){return false;}
    };
    private final DefaultTableModel orderMenuModel = new DefaultTableModel(new Object[]{"ID","Food","Category","Price (₹)"},0) {
        @Override public boolean isCellEditable(int row,int col){return false;}
    };
    private final DefaultTableModel historyModel = new DefaultTableModel(new Object[]{"Order ID","Date/time","Customer","Contact","Subtotal","Discount","Tax","Total","Payment"},0) {
        @Override public boolean isCellEditable(int row,int col){return false;}
    };
    private final JTable menuTable=new JTable(menuModel), orderMenuTable=new JTable(orderMenuModel), historyTable=new JTable(historyModel);
    private final JTextField customer=new JTextField(), contact=new JTextField("N/A"), quantity=new JTextField("1",4);
    private final JLabel status=new JLabel("MySQL database: restaurant_management");
    private final List<CartLine> cart=new ArrayList<>();

    public RestaurantManagementGUI(){
        super("Campus Restaurant | Management & Billing");
        setDefaultCloseOperation(EXIT_ON_CLOSE); setSize(1000,680); setLocationRelativeTo(null);
        setLayout(new BorderLayout(8,8));
        JLabel heading=new JLabel("  CAMPUS RESTAURANT — Management & Billing");
        heading.setFont(heading.getFont().deriveFont(java.awt.Font.BOLD,21f)); add(heading,BorderLayout.NORTH);
        JTabbedPane tabs=new JTabbedPane();
        tabs.addTab("Food menu",menuPanel()); tabs.addTab("Create order",orderPanel()); tabs.addTab("Order history",historyPanel());
        add(tabs,BorderLayout.CENTER); status.setBorder(BorderFactory.createEmptyBorder(5,10,8,10)); add(status,BorderLayout.SOUTH);
        reloadMenu(); reloadHistory();
    }
    private JPanel menuPanel(){
        JPanel p=new JPanel(new BorderLayout(8,8)); p.add(new JScrollPane(menuTable),BorderLayout.CENTER);
        JPanel buttons=new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton add=new JButton("Add food"), update=new JButton("Update price"), remove=new JButton("Remove food");
        add.addActionListener(e->addFood()); update.addActionListener(e->updatePrice()); remove.addActionListener(e->removeFood());
        buttons.add(add); buttons.add(update); buttons.add(remove); p.add(buttons,BorderLayout.SOUTH); return p;
    }
    private JPanel orderPanel(){
        JPanel p=new JPanel(new BorderLayout(8,8)); JPanel fields=new JPanel(new GridLayout(2,2,8,6));
        fields.add(new JLabel("Customer name")); fields.add(customer); fields.add(new JLabel("Contact")); fields.add(contact);
        p.add(fields,BorderLayout.NORTH); p.add(new JScrollPane(orderMenuTable),BorderLayout.CENTER);
        JPanel bottom=new JPanel(new FlowLayout(FlowLayout.LEFT)); bottom.add(new JLabel("Quantity")); bottom.add(quantity);
        JButton add=new JButton("Add selected to order"), finish=new JButton("Complete order & save bill"), clear=new JButton("Clear order");
        add.addActionListener(e->addToCart()); finish.addActionListener(e->finishOrder());
        clear.addActionListener(e->{cart.clear();status.setText("Order cleared.");});
        bottom.add(add); bottom.add(finish); bottom.add(clear); p.add(bottom,BorderLayout.SOUTH); return p;
    }
    private JPanel historyPanel(){
        JPanel p=new JPanel(new BorderLayout()); JButton refresh=new JButton("Refresh order history");
        refresh.addActionListener(e->reloadHistory()); p.add(new JScrollPane(historyTable),BorderLayout.CENTER);
        p.add(refresh,BorderLayout.SOUTH); return p;
    }
    private void reloadMenu(){
        try{
            menuModel.setRowCount(0); orderMenuModel.setRowCount(0);
            for(RestaurantDatabase.MenuRow row:RestaurantDatabase.loadMenu()){
                Object[] values={row.id,row.name,row.category,row.price.toPlainString()};
                menuModel.addRow(values); orderMenuModel.addRow(values.clone());
            }
            status.setText("Menu loaded from MySQL. Items: "+menuModel.getRowCount());
        }catch(SQLException ex){showError("Could not load menu. Run database/restaurant_management.sql and check database settings.\n"+ex.getMessage());}
    }
    private void addFood(){
        JTextField name=new JTextField(), price=new JTextField();
        JComboBox<String> category=new JComboBox<>(new String[]{"Vegetarian","Non-vegetarian","Beverage","Dessert"});
        JPanel form=new JPanel(new GridLayout(0,1,4,4)); form.add(new JLabel("Food name")); form.add(name);
        form.add(new JLabel("Category")); form.add(category); form.add(new JLabel("Price (₹)")); form.add(price);
        if(JOptionPane.showConfirmDialog(this,form,"Add food item",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;
        try{
            if(name.getText().trim().isEmpty())throw new IllegalArgumentException("Food name is required.");
            int id=RestaurantDatabase.addMenuItem(name.getText(),category.getSelectedItem().toString(),positiveMoney(price.getText()));
            reloadMenu(); status.setText("Food item saved with database ID "+id);
        }catch(Exception ex){showError(ex.getMessage());}
    }
    private void updatePrice(){
        int row=menuTable.getSelectedRow(); if(row<0){showError("Select a food item first.");return;}
        int id=((Number)menuModel.getValueAt(row,0)).intValue();
        String value=JOptionPane.showInputDialog(this,"New price (₹)",menuModel.getValueAt(row,3)); if(value==null)return;
        try{RestaurantDatabase.updateMenuPrice(id,positiveMoney(value));reloadMenu();}catch(Exception ex){showError(ex.getMessage());}
    }
    private void removeFood(){
        int row=menuTable.getSelectedRow();if(row<0){showError("Select a food item first.");return;}
        int id=((Number)menuModel.getValueAt(row,0)).intValue();
        if(JOptionPane.showConfirmDialog(this,"Remove this food item from the active menu?","Confirm",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
        try{RestaurantDatabase.removeMenuItem(id);reloadMenu();}catch(Exception ex){showError(ex.getMessage());}
    }
    private void addToCart(){
        int row=orderMenuTable.getSelectedRow();if(row<0){showError("Select a menu item first.");return;}
        try{
            int qty=Integer.parseInt(quantity.getText().trim());if(qty<1||qty>999)throw new IllegalArgumentException("Quantity must be from 1 to 999.");
            int id=((Number)orderMenuModel.getValueAt(row,0)).intValue();
            String name=orderMenuModel.getValueAt(row,1).toString(), category=orderMenuModel.getValueAt(row,2).toString();
            BigDecimal price=new BigDecimal(orderMenuModel.getValueAt(row,3).toString());
            cart.add(new CartLine(id,name,category,price,qty));status.setText("Added "+qty+" x "+name+". Cart lines: "+cart.size());
        }catch(NumberFormatException ex){showError("Enter a valid whole-number quantity.");}catch(RuntimeException ex){showError(ex.getMessage());}
    }
    private void finishOrder(){
        if(cart.isEmpty()){showError("Add at least one menu item.");return;}
        if(customer.getText().trim().isEmpty()){showError("Enter the customer name.");return;}
        BigDecimal subtotal=BigDecimal.ZERO; List<RestaurantDatabase.LineToSave> lines=new ArrayList<>();
        StringBuilder receipt=new StringBuilder("CAMPUS RESTAURANT\nCUSTOMER BILL\n--------------------------------\n");
        for(CartLine line:cart){
            BigDecimal lineTotal=line.price.multiply(BigDecimal.valueOf(line.quantity)).setScale(2,RoundingMode.HALF_UP);
            subtotal=subtotal.add(lineTotal);lines.add(new RestaurantDatabase.LineToSave(line.id,line.name,line.category,line.price,line.quantity,lineTotal));
            receipt.append(line.name).append(" x ").append(line.quantity).append(" = ₹").append(lineTotal).append('\n');
        }
        BigDecimal discount=subtotal.compareTo(new BigDecimal("500.00"))>=0?subtotal.multiply(new BigDecimal("0.10")).setScale(2,RoundingMode.HALF_UP):new BigDecimal("0.00");
        BigDecimal tax=subtotal.subtract(discount).multiply(new BigDecimal("0.05")).setScale(2,RoundingMode.HALF_UP);
        BigDecimal total=subtotal.subtract(discount).add(tax).setScale(2,RoundingMode.HALF_UP);
        Object choice=JOptionPane.showInputDialog(this,"Total: ₹"+total+"\nChoose simulated payment method:","Payment",JOptionPane.QUESTION_MESSAGE,null,new String[]{"Cash","UPI","Card"},"Cash");
        if(choice==null)return;
        try{
            long orderId=RestaurantDatabase.saveCompletedOrder(customer.getText(),contact.getText(),subtotal,discount,tax,total,choice.toString(),lines);
            receipt.append("--------------------------------\nSubtotal: ₹").append(subtotal).append("\nDiscount: ₹").append(discount)
                .append("\nTax (5% after discount): ₹").append(tax).append("\nTOTAL: ₹").append(total).append("\nPayment: ").append(choice)
                .append("\nOrder ID: ").append(orderId).append("\nPayment is simulated; no money was transferred.");
            JTextArea area=new JTextArea(receipt.toString(),18,42);area.setEditable(false);
            JOptionPane.showMessageDialog(this,new JScrollPane(area),"Saved bill",JOptionPane.INFORMATION_MESSAGE);
            cart.clear();customer.setText("");contact.setText("N/A");reloadHistory();status.setText("Order #"+orderId+" and its lines saved to MySQL.");
        }catch(SQLException ex){showError("Order was not saved. Database transaction rolled back.\n"+ex.getMessage());}
    }
    private void reloadHistory(){
        historyModel.setRowCount(0);
        try(Connection c=RestaurantDatabase.connect();Statement s=c.createStatement();
            ResultSet rs=s.executeQuery("SELECT order_id,order_time,customer_name,customer_contact,subtotal,discount,tax,total,payment_method FROM orders ORDER BY order_time DESC")){
            while(rs.next())historyModel.addRow(new Object[]{rs.getLong("order_id"),rs.getTimestamp("order_time"),rs.getString("customer_name"),rs.getString("customer_contact"),rs.getBigDecimal("subtotal"),rs.getBigDecimal("discount"),rs.getBigDecimal("tax"),rs.getBigDecimal("total"),rs.getString("payment_method")});
        }catch(SQLException ex){status.setText("Order history unavailable: "+ex.getMessage());}
    }
    private BigDecimal positiveMoney(String text){
        BigDecimal value=new BigDecimal(text.trim()).setScale(2,RoundingMode.HALF_UP);
        if(value.signum()<=0)throw new IllegalArgumentException("Price must be greater than zero.");return value;
    }
    private void showError(String message){JOptionPane.showMessageDialog(this,message,"Please check",JOptionPane.WARNING_MESSAGE);}
    private static final class CartLine{
        final int id;final String name,category;final BigDecimal price;final int quantity;
        CartLine(int id,String name,String category,BigDecimal price,int quantity){this.id=id;this.name=name;this.category=category;this.price=price;this.quantity=quantity;}
    }
    public static void main(String[] args){
        SwingUtilities.invokeLater(()->{try{UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());}catch(Exception ignored){}new RestaurantManagementGUI().setVisible(true);});
    }
}
