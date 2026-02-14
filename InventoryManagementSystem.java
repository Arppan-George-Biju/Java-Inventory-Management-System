import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.sql.*;

public class InventoryManagementSystem extends JFrame {
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField itemNameField, categoryField, quantityField, priceField;
    private JLabel statsLabel;
    private Connection conn;
    
    private static final String DB_URL = "jdbc:mysql://localhost:3306/";
    private static final String DB_NAME = "inventory_db";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "stoper@@4748";

    public InventoryManagementSystem() {
        setTitle("Inventory Management System");
        setSize(1200, 550);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        initDB();
        initUI();
        loadItems();
        setVisible(true);
    }

    private void initUI() {
        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top stats
        JPanel top = new JPanel();
        top.setBackground(new Color(255, 87, 34));
        statsLabel = new JLabel("Total Items: 0");
        statsLabel.setFont(new Font("Arial", Font.BOLD, 18));
        statsLabel.setForeground(Color.WHITE);
        top.add(statsLabel);

        // Input panel
        JPanel input = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        input.setBackground(Color.WHITE);
        
        JLabel itemNameLabel = new JLabel("Item Name:");
        itemNameLabel.setFont(new Font("Arial", Font.BOLD, 12));
        input.add(itemNameLabel);
        itemNameField = new JTextField(15);
        itemNameField.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        input.add(itemNameField);
        
        JLabel categoryLabel = new JLabel("Category:");
        categoryLabel.setFont(new Font("Arial", Font.BOLD, 12));
        input.add(categoryLabel);
        categoryField = new JTextField(12);
        categoryField.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        input.add(categoryField);
        
        JLabel quantityLabel = new JLabel("Quantity:");
        quantityLabel.setFont(new Font("Arial", Font.BOLD, 12));
        input.add(quantityLabel);
        quantityField = new JTextField(8);
        quantityField.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        input.add(quantityField);
        
        JLabel priceLabel = new JLabel("Price:");
        priceLabel.setFont(new Font("Arial", Font.BOLD, 12));
        input.add(priceLabel);
        priceField = new JTextField(8);
        priceField.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        input.add(priceField);
        input.add(createButton("Add Item", new Color(76, 175, 80), e -> addItem()));

        // Table
        String[] cols = {"ID", "Item Name", "Category", "Quantity", "Price", "Added Date"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(25);

        // Buttons
        JPanel bottom = new JPanel();
        bottom.setBackground(Color.WHITE);
        bottom.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JButton deleteBtn = createButton("Delete Selected", new Color(244, 67, 54), e -> deleteItem());
        deleteBtn.setPreferredSize(new Dimension(140, 35));
        bottom.add(deleteBtn);

        main.add(top, BorderLayout.NORTH);
        main.add(input, BorderLayout.CENTER);
        main.add(new JScrollPane(table), BorderLayout.SOUTH);
        add(main, BorderLayout.NORTH);
        add(bottom, BorderLayout.SOUTH);
    }

    private JButton createButton(String text, Color color, java.awt.event.ActionListener action) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.addActionListener(action);
        return btn;
    }

    private void initDB() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            conn.createStatement().execute("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
            conn = DriverManager.getConnection(DB_URL + DB_NAME, DB_USER, DB_PASS);
            
            // Drop existing table and recreate with new schema
            Statement stmt = conn.createStatement();
            stmt.execute("DROP TABLE IF EXISTS items");
            stmt.execute(
                "CREATE TABLE items (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "item_name VARCHAR(200)," +
                "category VARCHAR(100)," +
                "quantity INT," +
                "price DECIMAL(10,2)," +
                "date VARCHAR(50))");
            stmt.close();
            System.out.println("Database ready!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "DB Error: " + e.getMessage());
        }
    }

    private void loadItems() {
        try {
            tableModel.setRowCount(0);
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM items");
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("id"), rs.getString("item_name"), 
                    rs.getString("category"), rs.getInt("quantity"),
                    rs.getDouble("price"), rs.getString("date")
                });
            }
            updateStats();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Load Error: " + e.getMessage());
        }
    }

    private void addItem() {
        String itemName = itemNameField.getText().trim();
        String category = categoryField.getText().trim();
        String quantityStr = quantityField.getText().trim();
        String priceStr = priceField.getText().trim();
        
        if (itemName.isEmpty() || category.isEmpty() || quantityStr.isEmpty() || priceStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Fill all fields!");
            return;
        }
        try {
            int quantity = Integer.parseInt(quantityStr);
            double price = Double.parseDouble(priceStr);
            String date = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO items (item_name, category, quantity, price, date) VALUES (?, ?, ?, ?, ?)");
            ps.setString(1, itemName);
            ps.setString(2, category);
            ps.setInt(3, quantity);
            ps.setDouble(4, price);
            ps.setString(5, date);
            ps.executeUpdate();
            itemNameField.setText("");
            categoryField.setText("");
            quantityField.setText("");
            priceField.setText("");
            loadItems();
            JOptionPane.showMessageDialog(this, "Item added successfully!");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Quantity and Price must be valid numbers!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void deleteItem() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select an item to delete!");
            return;
        }
        try {
            int id = (int) table.getValueAt(row, 0);
            PreparedStatement ps = conn.prepareStatement("DELETE FROM items WHERE id=?");
            ps.setInt(1, id);
            ps.executeUpdate();
            loadItems();
            JOptionPane.showMessageDialog(this, "Item deleted successfully!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void updateStats() {
        try {
            ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) as total FROM items");
            if (rs.next()) {
                statsLabel.setText("Total Items: " + rs.getInt("total"));
            }
        } catch (Exception e) {}
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new InventoryManagementSystem());
    }
}
