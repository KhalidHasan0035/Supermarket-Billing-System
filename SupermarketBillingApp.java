import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;

// ---------- User Classes ----------
class User implements Serializable {
    private String username;
    private String password;
    private String role;
    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    
    public boolean authenticate(String password) {
        return this.password.equals(password);
    }
}

// ---------- Entity Classes ----------
class Customer implements Serializable {
    private int id;
    private String name;
    private String phone;
    private List<Bill> purchaseHistory;
    public Customer(int id, String name, String phone) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.purchaseHistory = new ArrayList<>();
    }
    public int getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public List<Bill> getPurchaseHistory() { return purchaseHistory; }
    public void addPurchase(Bill bill) { purchaseHistory.add(bill); }
    public void setPurchaseHistory(List<Bill> history) { this.purchaseHistory = history; }
    
    // Added missing methods
    public void setName(String name) { this.name = name; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String toString() {
        return name + " (" + phone + ")";
    }
}

class Product implements Serializable {
    private int id;
    private String name;
    private double price;
    private int stock;
    private String unitType; // "kg" or "pcs"
    public Product(int id, String name, double price, int stock, String unitType) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.unitType = unitType;
    }
    public int getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getStock() { return stock; }
    public String getUnitType() { return unitType; }
    public void setStock(int stock) { this.stock = stock; }
    public void setPrice(double price) { this.price = price; }
    public void setName(String name) { this.name = name; }
    public void reduceStock(double qty) { 
        if ("kg".equals(unitType)) {
            this.stock = (int) (this.stock * 1000 - qty * 1000) / 1000;
        } else {
            this.stock -= (int) qty;
        }
    }
    public boolean isLowStock() { return stock <= 5; }
    public String toString() {
        return name + " (Tk " + price + " per " + unitType + ")";
    }
}

class Bill implements Serializable {
    private int billNo;
    private Customer customer;
    private List<String> items;
    private double total;
    private Date date;
    private String cashier;
    private double amountPaid;
    private double change;
    public Bill(int billNo, Customer customer, List<String> items, double total, Date date, String cashier, double amountPaid, double change) {
        this.billNo = billNo;
        this.customer = customer;
        this.items = items;
        this.total = total;
        this.date = date;
        this.cashier = cashier;
        this.amountPaid = amountPaid;
        this.change = change;
    }
    public int getBillNo() { return billNo; }
    public Customer getCustomer() { return customer; }
    public double getTotal() { return total; }
    public Date getDate() { return date; }
    public String getCashier() { return cashier; }
    public List<String> getItems() { return items; }
    public double getAmountPaid() { return amountPaid; }
    public double getChange() { return change; }
    public String toPrintableString() {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("          AMADER SUPERMARKET           \n");
        sb.append("       123 Main Road, THAKURGAON         \n");
        sb.append("          Phone: 01518987563             \n");
        sb.append("========================================\n");
        sb.append("Bill No: ").append(billNo).append("\n");
        sb.append("Date: ").append(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(date)).append("\n");
        sb.append("Customer: ").append(customer.getName()).append("\n");
        sb.append("Phone: ").append(customer.getPhone()).append("\n");
        sb.append("Cashier: ").append(cashier).append("\n");
        sb.append("----------------------------------------\n");
        sb.append("Item           Qty   Price    Amount\n");
        sb.append("----------------------------------------\n");
        
        for (String item : items) {
            sb.append(item).append("\n");
        }
        
        sb.append("----------------------------------------\n");
        sb.append("Total: Tk ").append(String.format("%.2f", total)).append("\n");
        sb.append("Amount Paid: Tk ").append(String.format("%.2f", amountPaid)).append("\n");
        sb.append("Change: Tk ").append(String.format("%.2f", change)).append("\n");
        sb.append("========================================\n");
        sb.append("      Thank you for your purchase!      \n");
        sb.append("        Please visit again soon!        \n");
        sb.append("========================================\n");
        return sb.toString();
    }
}

// ---------- File Handler ----------
class FileHandler {
    public static void saveData(Object obj, String filename) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static Object loadData(String filename) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            return ois.readObject();
        } catch (Exception e) {
            return null;
        }
    }
}

// ---------- Main GUI ----------
public class SupermarketBillingApp extends JFrame {
    private List<User> users = new ArrayList<>();
    private List<Customer> customers = new ArrayList<>();
    private List<Product> products = new ArrayList<>();
    private List<Bill> bills = new ArrayList<>();
    private JTextField phoneField;
    private JTextField nameField;
    private JTextField productSearchField;
    private JList<Product> productList;
    private DefaultListModel<Product> productListModel;
    private JTextField qtyField;
    private JTable billTable;
    private DefaultTableModel billModel;
    private JLabel totalLabel;
    private int customerId = 1, productId = 1, billId = 1;
    private String currentUser;
    private String currentUserRole;
    
    public SupermarketBillingApp() {
        setTitle("Amader Supermarket - Billing System");
        setSize(900, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initializeDefaultUsers();
        showLoginDialog();
    }
    
    private void initializeDefaultUsers() {
        users.add(new User("admin", "admin123", "admin"));
        users.add(new User("cashier", "cashier123", "cashier"));
        FileHandler.saveData(users, "users.ser");
    }
    
    private void showLoginDialog() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        
        // Add Enter key support for login button
        JButton loginBtn = new JButton("Login");
        getRootPane().setDefaultButton(loginBtn);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "Login", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            Object loadedUsers = FileHandler.loadData("users.ser");
            if (loadedUsers != null) {
                users = (List<User>) loadedUsers;
            }
            boolean authenticated = false;
            for (User user : users) {
                if (user.getUsername().equals(username) && user.authenticate(password)) {
                    authenticated = true;
                    currentUser = username;
                    currentUserRole = user.getRole();
                    JOptionPane.showMessageDialog(this, "Login successful! Welcome " + username);
                    initializeMainUI(user.getRole());
                    setVisible(true);
                    break;
                }
            }
            if (!authenticated) {
                JOptionPane.showMessageDialog(this, "Invalid username or password!");
                showLoginDialog();
            }
        } else {
            System.exit(0);
        }
    }
    
    private void initializeMainUI(String role) {
        getContentPane().removeAll();
        
        JTabbedPane tabs = new JTabbedPane();
        
        if ("admin".equals(role)) {
            tabs.add("Dashboard", createDashboardPanel());
            tabs.add("Customers", createCustomerPanel());
            tabs.add("Products", createProductPanel());
            tabs.add("Billing", createBillingPanel());
        } else {
            tabs.add("Billing", createBillingPanel());
            tabs.add("Products", createProductViewPanel());
        }
        add(tabs);
        loadAll();
        revalidate();
        repaint();
    }
    
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Header with refresh button
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel headerLabel = new JLabel("DASHBOARD OVERVIEW", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refreshDashboard(panel));
        
        headerPanel.add(headerLabel, BorderLayout.CENTER);
        headerPanel.add(refreshBtn, BorderLayout.EAST);
        panel.add(headerPanel, BorderLayout.NORTH);
        
        // Create the dashboard content
        refreshDashboard(panel);
        
        return panel;
    }
    
    private void refreshDashboard(JPanel panel) {
        // Remove existing components except header
        Component[] components = panel.getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                JPanel compPanel = (JPanel) comp;
                if (compPanel.getComponentCount() > 0 && compPanel.getComponent(0) instanceof JLabel) {
                    // This is the header panel, keep it
                    continue;
                }
            }
            panel.remove(comp);
        }
        
        // Reload data
        loadAll();
        
        // Auto-delete old bills (older than 30 days)
        autoDeleteOldBills();
        
        // Stats panel
        JPanel statsPanel = new JPanel(new GridLayout(2, 3, 10, 10));
        
        // Calculate statistics
        int totalProducts = products.size();
        int lowStockProducts = 0;
        int outOfStockProducts = 0;
        double totalRevenue = getTodayRevenue();
        int totalBills = getTodayBillsCount();
        int totalCustomers = customers.size();
        
        // Calculate today's sales by product type
        double todayKgSales = getTodaySalesByUnit("kg");
        double todayPcsSales = getTodaySalesByUnit("pcs");
        
        List<Product> lowStockItems = new ArrayList<>();
        List<Product> outOfStockItems = new ArrayList<>();
        
        for (Product p : products) {
            if (p.getStock() == 0) {
                outOfStockProducts++;
                outOfStockItems.add(p);
            } else if (p.isLowStock()) {
                lowStockProducts++;
                lowStockItems.add(p);
            }
        }
        
        // Create stat cards with clickable functionality
        JButton totalProductsBtn = createClickableStatCard("Total Products", String.valueOf(totalProducts));
        JButton lowStockBtn = createClickableStatCard("Low Stock", String.valueOf(lowStockProducts));
        JButton outOfStockBtn = createClickableStatCard("Out of Stock", String.valueOf(outOfStockProducts));
        JButton revenueBtn = createStatCard("Today's Revenue", "Tk " + String.format("%.2f", totalRevenue));
        JButton totalBillsBtn = createStatCard("Today's Bills", String.valueOf(totalBills));
        JButton salesBtn = createStatCard("Today's Sales", String.format("Kg: %.2f, Pcs: %.2f", todayKgSales, todayPcsSales));
        
        // Add click listeners
        totalProductsBtn.addActionListener(e -> showProductsDialog("All Products", products));
        lowStockBtn.addActionListener(e -> showProductsDialog("Low Stock Products", lowStockItems));
        outOfStockBtn.addActionListener(e -> showProductsDialog("Out of Stock Products", outOfStockItems));
        
        statsPanel.add(totalProductsBtn);
        statsPanel.add(lowStockBtn);
        statsPanel.add(outOfStockBtn);
        statsPanel.add(revenueBtn);
        statsPanel.add(totalBillsBtn);
        statsPanel.add(salesBtn);
        
        panel.add(statsPanel, BorderLayout.CENTER);
        
        // Recent activity (today's bills only)
        JPanel recentPanel = new JPanel(new BorderLayout());
        recentPanel.setBorder(BorderFactory.createTitledBorder("Today's Bills"));
        
        String[] columns = {"Bill No", "Customer", "Amount", "Time", "Cashier"};
        DefaultTableModel recentModel = new DefaultTableModel(columns, 0);
        JTable recentTable = new JTable(recentModel);
        
        // Show today's bills
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String today = sdf.format(new Date());
        
        for (Bill bill : bills) {
            String billDate = sdf.format(bill.getDate());
            if (billDate.equals(today)) {
                recentModel.addRow(new Object[]{
                    bill.getBillNo(),
                    bill.getCustomer().getName(),
                    "Tk " + String.format("%.2f", bill.getTotal()),
                    new SimpleDateFormat("HH:mm:ss").format(bill.getDate()),
                    bill.getCashier()
                });
            }
        }
        
        recentPanel.add(new JScrollPane(recentTable), BorderLayout.CENTER);
        panel.add(recentPanel, BorderLayout.SOUTH);
        
        panel.revalidate();
        panel.repaint();
    }
    
    private double getTodayRevenue() {
        double revenue = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String today = sdf.format(new Date());
        
        for (Bill bill : bills) {
            String billDate = sdf.format(bill.getDate());
            if (billDate.equals(today)) {
                revenue += bill.getTotal();
            }
        }
        return revenue;
    }
    
    private int getTodayBillsCount() {
        int count = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String today = sdf.format(new Date());
        
        for (Bill bill : bills) {
            String billDate = sdf.format(bill.getDate());
            if (billDate.equals(today)) {
                count++;
            }
        }
        return count;
    }
    
    private double getTodaySalesByUnit(String unitType) {
        double totalSales = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String today = sdf.format(new Date());
        
        for (Bill bill : bills) {
            String billDate = sdf.format(bill.getDate());
            if (billDate.equals(today)) {
                for (String item : bill.getItems()) {
                    if (item.contains(unitType)) {
                        // Extract quantity from item string
                        String[] parts = item.split("\\s+");
                        if (parts.length >= 2) {
                            try {
                                double qty = Double.parseDouble(parts[1]);
                                totalSales += qty;
                            } catch (NumberFormatException e) {
                                // Ignore if parsing fails
                            }
                        }
                    }
                }
            }
        }
        return totalSales;
    }
    
    private void autoDeleteOldBills() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -30); // 30 days ago
        Date thirtyDaysAgo = cal.getTime();
        
        List<Bill> billsToKeep = new ArrayList<>();
        for (Bill bill : bills) {
            if (bill.getDate().after(thirtyDaysAgo)) {
                billsToKeep.add(bill);
            }
        }
        
        if (billsToKeep.size() < bills.size()) {
            bills = billsToKeep;
            saveAll();
        }
    }
    
    private void showProductsDialog(String title, List<Product> productsToShow) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new BorderLayout());
        
        String[] columns = {"ID", "Name", "Price", "Stock", "Unit", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);
        
        for (Product p : productsToShow) {
            String status;
            if (p.getStock() == 0) {
                status = "OUT OF STOCK";
            } else if (p.isLowStock()) {
                status = "LOW STOCK (" + p.getStock() + " left)";
            } else {
                status = "In Stock (" + p.getStock() + ")";
            }
            
            model.addRow(new Object[]{
                p.getId(), p.getName(), "Tk " + p.getPrice(), p.getStock(), p.getUnitType(), status
            });
        }
        
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        
        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dialog.dispose());
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private JButton createClickableStatCard(String title, String value) {
        JButton card = new JButton();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLUE, 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 16));
        valueLabel.setForeground(Color.BLUE);
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }
    
    private JButton createStatCard(String title, String value) {
        JButton card = new JButton();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY, 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        card.setEnabled(false); // Make non-clickable
        
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 16));
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }
    
    private JPanel createCustomerPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Header with refresh button
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel headerLabel = new JLabel("CUSTOMER MANAGEMENT", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refreshCustomerPanel(panel));
        
        headerPanel.add(headerLabel, BorderLayout.CENTER);
        headerPanel.add(refreshBtn, BorderLayout.EAST);
        panel.add(headerPanel, BorderLayout.NORTH);
        
        // Create customer panel content
        refreshCustomerPanel(panel);
        
        return panel;
    }
    
    private void refreshCustomerPanel(JPanel panel) {
        // Remove existing components except header
        Component[] components = panel.getComponents();
        for (int i = 1; i < components.length; i++) {
            panel.remove(components[i]);
        }
        
        // Reload data
        loadAll();
        
        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField searchField = new JTextField(15);
        JButton searchBtn = new JButton("Search by Phone");
        JButton viewHistoryBtn = new JButton("View Purchase History");
        JButton clearBtn = new JButton("Clear Search");
        JButton deleteOldBtn = new JButton("Delete Old Bills");
        
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        searchPanel.add(viewHistoryBtn);
        searchPanel.add(clearBtn);
        searchPanel.add(deleteOldBtn);
        
        // Add customer form
        JPanel formPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        JTextField nameField = new JTextField();
        JTextField phoneField = new JTextField();
        JButton addBtn = new JButton("Add New Customer");
        
        formPanel.add(new JLabel("Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Phone:"));
        formPanel.add(phoneField);
        
        // Add keyboard navigation to form fields
        setupKeyboardNavigation(nameField, phoneField, addBtn);
        setupKeyboardNavigation(phoneField, null, addBtn); // Changed to trigger addBtn on Enter
        
        JPanel formContainer = new JPanel(new BorderLayout());
        formContainer.add(formPanel, BorderLayout.CENTER);
        formContainer.add(addBtn, BorderLayout.SOUTH);
        
        // Customers table
        String[] columns = {"ID", "Name", "Phone", "Total Purchases", "Last Purchase"};
        DefaultTableModel customerModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable customerTable = new JTable(customerModel);
        
        // Populate table
        refreshCustomerTable(customerModel);
        
        // Delete button
        JButton deleteBtn = new JButton("Delete Selected Customer");
        deleteBtn.addActionListener(e -> deleteCustomer(customerTable, customerModel));
        
        // Edit button
        JButton editBtn = new JButton("Edit Selected Customer");
        editBtn.addActionListener(e -> editCustomer(customerTable, customerModel));
        
        // Event handlers
        searchBtn.addActionListener(e -> {
            String searchText = searchField.getText().trim();
            if (searchText.isEmpty()) {
                refreshCustomerTable(customerModel);
                return;
            }
            
            customerModel.setRowCount(0);
            for (Customer c : customers) {
                if (c.getPhone().contains(searchText) || c.getName().toLowerCase().contains(searchText.toLowerCase())) {
                    String lastPurchase = "Never";
                    if (!c.getPurchaseHistory().isEmpty()) {
                        Bill lastBill = c.getPurchaseHistory().get(c.getPurchaseHistory().size() - 1);
                        lastPurchase = new SimpleDateFormat("dd/MM/yyyy").format(lastBill.getDate());
                    }
                    customerModel.addRow(new Object[]{
                        c.getId(), c.getName(), c.getPhone(), 
                        c.getPurchaseHistory().size(), lastPurchase
                    });
                }
            }
        });
        
        clearBtn.addActionListener(e -> {
            searchField.setText("");
            refreshCustomerTable(customerModel);
        });
        
        deleteOldBtn.addActionListener(e -> {
            if (authenticateAdmin()) {
                autoDeleteOldBills();
                refreshCustomerTable(customerModel);
                JOptionPane.showMessageDialog(this, "Old bills deleted successfully!");
            }
        });
        
        addBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            if (name.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter both name and phone!");
                return;
            }
            
            // Check if phone already exists
            for (Customer c : customers) {
                if (c.getPhone().equals(phone)) {
                    JOptionPane.showMessageDialog(this, "Customer with this phone already exists!");
                    return;
                }
            }
            
            Customer newCustomer = new Customer(customerId++, name, phone);
            customers.add(newCustomer);
            saveAll();
            refreshCustomerTable(customerModel);
            nameField.setText("");
            phoneField.setText("");
            JOptionPane.showMessageDialog(this, "Customer added successfully!");
        });
        
        viewHistoryBtn.addActionListener(e -> {
            int selectedRow = customerTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a customer first!");
            } else {
                int customerId = (Integer) customerModel.getValueAt(selectedRow, 0);
                Customer selectedCustomer = null;
                for (Customer c : customers) {
                    if (c.getId() == customerId) {
                        selectedCustomer = c;
                        break;
                    }
                }
                
                if (selectedCustomer != null) {
                    showPurchaseHistory(selectedCustomer);
                }
            }
        });
        
        // Layout
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(searchPanel, BorderLayout.NORTH);
        northPanel.add(formContainer, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);
        
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(new JScrollPane(customerTable), BorderLayout.CENTER);
        southPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        panel.add(northPanel, BorderLayout.CENTER);
        panel.add(southPanel, BorderLayout.SOUTH);
        
        panel.revalidate();
        panel.repaint();
    }
    
    private void editCustomer(JTable customerTable, DefaultTableModel customerModel) {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a customer to edit!");
            return;
        }
        
        int customerId = (Integer) customerModel.getValueAt(selectedRow, 0);
        Customer selectedCustomer = null;
        for (Customer c : customers) {
            if (c.getId() == customerId) {
                selectedCustomer = c;
                break;
            }
        }
        
        if (selectedCustomer != null) {
            JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
            JTextField nameField = new JTextField(selectedCustomer.getName());
            JTextField phoneField = new JTextField(selectedCustomer.getPhone());
            
            panel.add(new JLabel("Name:"));
            panel.add(nameField);
            panel.add(new JLabel("Phone:"));
            panel.add(phoneField);
            
            int result = JOptionPane.showConfirmDialog(this, panel, "Edit Customer", 
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            
            if (result == JOptionPane.OK_OPTION) {
                String newName = nameField.getText().trim();
                String newPhone = phoneField.getText().trim();
                
                if (newName.isEmpty() || newPhone.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please enter both name and phone!");
                    return;
                }
                
                // Check if phone already exists (excluding current customer)
                for (Customer c : customers) {
                    if (c.getId() != customerId && c.getPhone().equals(newPhone)) {
                        JOptionPane.showMessageDialog(this, "Another customer with this phone already exists!");
                        return;
                    }
                }
                
                selectedCustomer.setName(newName);
                selectedCustomer.setPhone(newPhone);
                saveAll();
                refreshCustomerTable(customerModel);
                JOptionPane.showMessageDialog(this, "Customer updated successfully!");
            }
        }
    }
    
    private void deleteCustomer(JTable customerTable, DefaultTableModel customerModel) {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a customer to delete!");
            return;
        }
        
        // Admin authentication
        if (!authenticateAdmin()) {
            return;
        }
        
        int customerId = (Integer) customerModel.getValueAt(selectedRow, 0);
        String customerName = (String) customerModel.getValueAt(selectedRow, 1);
        
        int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to delete customer: " + customerName + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            // Remove customer
            for (int i = 0; i < customers.size(); i++) {
                if (customers.get(i).getId() == customerId) {
                    customers.remove(i);
                    break;
                }
            }
            
            saveAll();
            refreshCustomerTable(customerModel);
            JOptionPane.showMessageDialog(this, "Customer deleted successfully!");
        }
    }
    
    private boolean authenticateAdmin() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        panel.add(new JLabel("Admin Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Admin Password:"));
        panel.add(passwordField);
        int result = JOptionPane.showConfirmDialog(this, panel, "Admin Authentication", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            
            for (User user : users) {
                if (user.getUsername().equals(username) && user.authenticate(password) && "admin".equals(user.getRole())) {
                    return true;
                }
            }
            
            JOptionPane.showMessageDialog(this, "Invalid admin credentials!");
        }
        return false;
    }
    
    private void setupKeyboardNavigation(JTextField currentField, JComponent nextField, JButton button) {
        currentField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (nextField != null) {
                        nextField.requestFocus();
                        if (nextField instanceof JTextField) {
                            ((JTextField) nextField).selectAll();
                        }
                    } else if (button != null) {
                        button.doClick();
                    }
                    e.consume();
                }
            }
        });
    }
    
    private void refreshCustomerTable(DefaultTableModel model) {
        model.setRowCount(0);
        for (Customer c : customers) {
            String lastPurchase = "Never";
            if (!c.getPurchaseHistory().isEmpty()) {
                Bill lastBill = c.getPurchaseHistory().get(c.getPurchaseHistory().size() - 1);
                lastPurchase = new SimpleDateFormat("dd/MM/yyyy").format(lastBill.getDate());
            }
            model.addRow(new Object[]{
                c.getId(), c.getName(), c.getPhone(), 
                c.getPurchaseHistory().size(), lastPurchase
            });
        }
    }
    
    private void showPurchaseHistory(Customer customer) {
        JDialog historyDialog = new JDialog(this, "Purchase History - " + customer.getName(), true);
        historyDialog.setSize(600, 400);
        historyDialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new BorderLayout());
        
        String[] columns = {"Bill No", "Date", "Time", "Amount", "Items"};
        DefaultTableModel historyModel = new DefaultTableModel(columns, 0);
        JTable historyTable = new JTable(historyModel);
        
        for (Bill bill : customer.getPurchaseHistory()) {
            historyModel.addRow(new Object[]{
                bill.getBillNo(),
                new SimpleDateFormat("dd/MM/yyyy").format(bill.getDate()),
                new SimpleDateFormat("HH:mm:ss").format(bill.getDate()),
                "Tk " + String.format("%.2f", bill.getTotal()),
                bill.getItems().size() + " items"
            });
        }
        
        JButton viewDetailsBtn = new JButton("View Bill Details");
        JButton deleteBtn = new JButton("Delete Bill");
        
        viewDetailsBtn.addActionListener(e -> {
            int selectedRow = historyTable.getSelectedRow();
            if (selectedRow >= 0) {
                int billNo = (Integer) historyModel.getValueAt(selectedRow, 0);
                for (Bill bill : customer.getPurchaseHistory()) {
                    if (bill.getBillNo() == billNo) {
                        JTextArea billArea = new JTextArea(bill.toPrintableString());
                        billArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
                        billArea.setEditable(false);
                        JOptionPane.showMessageDialog(historyDialog, new JScrollPane(billArea), 
                                "Bill Details - #" + billNo, JOptionPane.INFORMATION_MESSAGE);
                        break;
                    }
                }
            }
        });
        
        deleteBtn.addActionListener(e -> {
            int selectedRow = historyTable.getSelectedRow();
            if (selectedRow >= 0) {
                if (authenticateAdmin()) {
                    int billNo = (Integer) historyModel.getValueAt(selectedRow, 0);
                    
                    int confirm = JOptionPane.showConfirmDialog(historyDialog, 
                            "Are you sure you want to delete Bill #" + billNo + "?",
                            "Confirm Delete", JOptionPane.YES_NO_OPTION);
                    
                    if (confirm == JOptionPane.YES_OPTION) {
                        // Remove from customer history
                        List<Bill> newHistory = new ArrayList<>();
                        for (Bill bill : customer.getPurchaseHistory()) {
                            if (bill.getBillNo() != billNo) {
                                newHistory.add(bill);
                            }
                        }
                        customer.setPurchaseHistory(newHistory);
                        
                        // Remove from main bills list
                        for (int i = 0; i < bills.size(); i++) {
                            if (bills.get(i).getBillNo() == billNo) {
                                bills.remove(i);
                                break;
                            }
                        }
                        
                        saveAll();
                        historyModel.removeRow(selectedRow);
                        JOptionPane.showMessageDialog(historyDialog, "Bill deleted successfully!");
                    }
                }
            }
        });
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(viewDetailsBtn);
        buttonPanel.add(deleteBtn);
        
        panel.add(new JScrollPane(historyTable), BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        historyDialog.add(panel);
        historyDialog.setVisible(true);
    }
    
    private JPanel createProductPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Header with refresh button
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel headerLabel = new JLabel("PRODUCT MANAGEMENT", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refreshProductPanel(panel));
        
        headerPanel.add(headerLabel, BorderLayout.CENTER);
        headerPanel.add(refreshBtn, BorderLayout.EAST);
        panel.add(headerPanel, BorderLayout.NORTH);
        
        // Create product panel content
        refreshProductPanel(panel);
        
        return panel;
    }
    
    private void refreshProductPanel(JPanel panel) {
        // Remove existing components except header
        Component[] components = panel.getComponents();
        for (int i = 1; i < components.length; i++) {
            panel.remove(components[i]);
        }
        
        // Reload data
        loadAll();
        
        // Add product form
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        JTextField nameField = new JTextField();
        JTextField priceField = new JTextField();
        JTextField stockField = new JTextField();
        JComboBox<String> unitCombo = new JComboBox<>(new String[]{"kg", "pcs"});
        JButton addBtn = new JButton("Add New Product");
        
        formPanel.add(new JLabel("Product Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Price:"));
        formPanel.add(priceField);
        formPanel.add(new JLabel("Initial Stock:"));
        formPanel.add(stockField);
        formPanel.add(new JLabel("Unit Type:"));
        formPanel.add(unitCombo);
        
        // Add keyboard navigation to form fields
        setupKeyboardNavigation(nameField, priceField, null);
        setupKeyboardNavigation(priceField, stockField, null);
        setupKeyboardNavigation(stockField, null, addBtn); // Changed to trigger addBtn on Enter
        
        JPanel formContainer = new JPanel(new BorderLayout());
        formContainer.add(formPanel, BorderLayout.CENTER);
        formContainer.add(addBtn, BorderLayout.SOUTH);
        
        // Products table
        String[] columns = {"ID", "Name", "Price", "Stock", "Unit", "Status"};
        DefaultTableModel productModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable productTable = new JTable(productModel);
        
        // Populate table
        refreshProductTable(productModel);
        
        // Delete button
        JButton deleteBtn = new JButton("Delete Selected Product");
        deleteBtn.addActionListener(e -> deleteProduct(productTable, productModel));
        
        // Edit button
        JButton editBtn = new JButton("Edit Selected Product");
        editBtn.addActionListener(e -> editProduct(productTable, productModel));
        
        // Restock button
        JButton restockBtn = new JButton("Restock Selected Product");
        restockBtn.addActionListener(e -> restockProduct(productTable, productModel));
        
        addBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String priceStr = priceField.getText().trim();
            String stockStr = stockField.getText().trim();
            String unitType = (String) unitCombo.getSelectedItem();
            
            if (name.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields!");
                return;
            }
            
            try {
                double price = Double.parseDouble(priceStr);
                int stock = Integer.parseInt(stockStr);
                
                Product newProduct = new Product(productId++, name, price, stock, unitType);
                products.add(newProduct);
                saveAll();
                refreshProductTable(productModel);
                
                nameField.setText("");
                priceField.setText("");
                stockField.setText("");
                JOptionPane.showMessageDialog(this, "Product added successfully!");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid numbers for price and stock!");
            }
        });
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(editBtn);
        buttonPanel.add(restockBtn);
        buttonPanel.add(deleteBtn);
        
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(formContainer, BorderLayout.NORTH);
        centerPanel.add(new JScrollPane(productTable), BorderLayout.CENTER);
        centerPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        panel.add(centerPanel, BorderLayout.CENTER);
        
        panel.revalidate();
        panel.repaint();
    }
    
    private void editProduct(JTable productTable, DefaultTableModel productModel) {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to edit!");
            return;
        }
        
        int productId = (Integer) productModel.getValueAt(selectedRow, 0);
        Product selectedProduct = null;
        for (Product p : products) {
            if (p.getId() == productId) {
                selectedProduct = p;
                break;
            }
        }
        
        if (selectedProduct != null) {
            JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
            JTextField nameField = new JTextField(selectedProduct.getName());
            JTextField priceField = new JTextField(String.valueOf(selectedProduct.getPrice()));
            JTextField stockField = new JTextField(String.valueOf(selectedProduct.getStock()));
            JComboBox<String> unitCombo = new JComboBox<>(new String[]{"kg", "pcs"});
            unitCombo.setSelectedItem(selectedProduct.getUnitType());
            
            panel.add(new JLabel("Product Name:"));
            panel.add(nameField);
            panel.add(new JLabel("Price:"));
            panel.add(priceField);
            panel.add(new JLabel("Stock:"));
            panel.add(stockField);
            panel.add(new JLabel("Unit Type:"));
            panel.add(unitCombo);
            
            int result = JOptionPane.showConfirmDialog(this, panel, "Edit Product", 
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            
            if (result == JOptionPane.OK_OPTION) {
                try {
                    String newName = nameField.getText().trim();
                    double newPrice = Double.parseDouble(priceField.getText().trim());
                    int newStock = Integer.parseInt(stockField.getText().trim());
                    String newUnitType = (String) unitCombo.getSelectedItem();
                    
                    if (newName.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Product name cannot be empty!");
                        return;
                    }
                    
                    selectedProduct.setName(newName);
                    selectedProduct.setPrice(newPrice);
                    selectedProduct.setStock(newStock);
                    // Note: Unit type change might require more complex handling
                    
                    saveAll();
                    refreshProductTable(productModel);
                    JOptionPane.showMessageDialog(this, "Product updated successfully!");
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Please enter valid numbers for price and stock!");
                }
            }
        }
    }
    
    private void restockProduct(JTable productTable, DefaultTableModel productModel) {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to restock!");
            return;
        }
        
        int productId = (Integer) productModel.getValueAt(selectedRow, 0);
        Product selectedProduct = null;
        for (Product p : products) {
            if (p.getId() == productId) {
                selectedProduct = p;
                break;
            }
        }
        
        if (selectedProduct != null) {
            String restockStr = JOptionPane.showInputDialog(this, 
                    "Enter quantity to add to " + selectedProduct.getName() + ":",
                    "0");
            
            if (restockStr != null && !restockStr.trim().isEmpty()) {
                try {
                    int restockQty = Integer.parseInt(restockStr.trim());
                    if (restockQty <= 0) {
                        JOptionPane.showMessageDialog(this, "Please enter a positive quantity!");
                        return;
                    }
                    
                    selectedProduct.setStock(selectedProduct.getStock() + restockQty);
                    saveAll();
                    refreshProductTable(productModel);
                    JOptionPane.showMessageDialog(this, "Product restocked successfully!");
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Please enter a valid number!");
                }
            }
        }
    }
    
    private void deleteProduct(JTable productTable, DefaultTableModel productModel) {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to delete!");
            return;
        }
        
        // Admin authentication
        if (!authenticateAdmin()) {
            return;
        }
        
        int productId = (Integer) productModel.getValueAt(selectedRow, 0);
        String productName = (String) productModel.getValueAt(selectedRow, 1);
        
        int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to delete product: " + productName + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            // Remove product
            for (int i = 0; i < products.size(); i++) {
                if (products.get(i).getId() == productId) {
                    products.remove(i);
                    break;
                }
            }
            
            saveAll();
            refreshProductTable(productModel);
            JOptionPane.showMessageDialog(this, "Product deleted successfully!");
        }
    }
    
    private void refreshProductTable(DefaultTableModel model) {
        model.setRowCount(0);
        for (Product p : products) {
            String status;
            if (p.getStock() == 0) {
                status = "OUT OF STOCK";
            } else if (p.isLowStock()) {
                status = "LOW STOCK (" + p.getStock() + " left)";
            } else {
                status = "In Stock (" + p.getStock() + ")";
            }
            
            model.addRow(new Object[]{
                p.getId(), p.getName(), "Tk " + p.getPrice(), p.getStock(), p.getUnitType(), status
            });
        }
    }
    
    private JPanel createProductViewPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Header with refresh button
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel headerLabel = new JLabel("PRODUCT VIEW", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refreshProductViewPanel(panel));
        
        headerPanel.add(headerLabel, BorderLayout.CENTER);
        headerPanel.add(refreshBtn, BorderLayout.EAST);
        panel.add(headerPanel, BorderLayout.NORTH);
        
        // Create product view content
        refreshProductViewPanel(panel);
        
        return panel;
    }
    
    private void refreshProductViewPanel(JPanel panel) {
        // Remove existing components except header
        Component[] components = panel.getComponents();
        for (int i = 1; i < components.length; i++) {
            panel.remove(components[i]);
        }
        
        // Reload data
        loadAll();
        
        // Products table
        String[] columns = {"ID", "Name", "Price", "Stock", "Unit", "Status"};
        DefaultTableModel productModel = new DefaultTableModel(columns, 0);
        JTable productTable = new JTable(productModel);
        
        refreshProductTable(productModel);
        
        panel.add(new JScrollPane(productTable), BorderLayout.CENTER);
        
        panel.revalidate();
        panel.repaint();
    }
    
    private JPanel createBillingPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Customer section
        JPanel customerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        phoneField = new JTextField(15);
        nameField = new JTextField(15);
        nameField.setEditable(false);
        JButton findCustomerBtn = new JButton("Find/Add Customer");
        
        gbc.gridx = 0; gbc.gridy = 0;
        customerPanel.add(new JLabel("Phone Number:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 0;
        customerPanel.add(phoneField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        customerPanel.add(new JLabel("Customer Name:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 1;
        customerPanel.add(nameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        customerPanel.add(findCustomerBtn, gbc);
        
        // Product section
        JPanel productPanel = new JPanel(new BorderLayout(5, 5));
        productSearchField = new JTextField();
        productListModel = new DefaultListModel<>();
        productList = new JList<>(productListModel);
        productList.setVisibleRowCount(6);
        
        productSearchField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { filterProducts(); }
            public void removeUpdate(DocumentEvent e) { filterProducts(); }
            public void insertUpdate(DocumentEvent e) { filterProducts(); }
            
            private void filterProducts() {
                String searchText = productSearchField.getText().toLowerCase();
                productListModel.clear();
                for (Product p : products) {
                    if (p.getName().toLowerCase().contains(searchText) || 
                        String.valueOf(p.getId()).contains(searchText)) {
                        productListModel.addElement(p);
                    }
                }
            }
        });
        
        JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
        searchPanel.add(new JLabel("Search Products:"), BorderLayout.NORTH);
        searchPanel.add(productSearchField, BorderLayout.CENTER);
        productPanel.add(searchPanel, BorderLayout.NORTH);
        productPanel.add(new JScrollPane(productList), BorderLayout.CENTER);
        
        // Quantity section
        JPanel quantityPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        qtyField = new JTextField(5);
        JButton addBtn = new JButton("Add to Bill");
        quantityPanel.add(new JLabel("Quantity:"));
        quantityPanel.add(qtyField);
        quantityPanel.add(addBtn);
        
        // Bill table
        billModel = new DefaultTableModel(new String[]{"Product", "Qty", "Unit", "Price", "Subtotal"}, 0);
        billTable = new JTable(billModel);
        totalLabel = new JLabel("Total: Tk 0.00", SwingConstants.RIGHT);
        totalLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JButton genBtn = new JButton("Generate Bill");
        
        // Layout
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.add(customerPanel, BorderLayout.NORTH);
        topPanel.add(productPanel, BorderLayout.CENTER);
        topPanel.add(quantityPanel, BorderLayout.SOUTH);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(totalLabel, BorderLayout.CENTER);
        bottomPanel.add(genBtn, BorderLayout.EAST);
        
        // Event handlers
        findCustomerBtn.addActionListener(e -> findOrAddCustomer());
        addBtn.addActionListener(e -> addToBill());
        genBtn.addActionListener(e -> generateBill());
        
        // Add keyboard navigation
        setupKeyboardNavigation(phoneField, null, findCustomerBtn); // Changed to trigger findCustomerBtn on Enter
        setupKeyboardNavigation(productSearchField, qtyField, null);
        setupKeyboardNavigation(qtyField, null, addBtn); // Changed to trigger addBtn on Enter
        
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(billTable), BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        // Load products for billing
        refreshProductList();
        
        return panel;
    }
    
    private void refreshProductList() {
        productListModel.clear();
        for (Product p : products) {
            productListModel.addElement(p);
        }
    }
    
    private void findOrAddCustomer() {
        String phone = phoneField.getText().trim();
        if (phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a phone number!");
            return;
        }
        
        for (Customer c : customers) {
            if (c.getPhone().equals(phone)) {
                nameField.setText(c.getName());
                JOptionPane.showMessageDialog(this, "Customer found: " + c.getName());
                return;
            }
        }
        
        String name = JOptionPane.showInputDialog(this, "Customer not found. Enter name for new customer:");
        if (name != null && !name.trim().isEmpty()) {
            Customer newCustomer = new Customer(customerId++, name.trim(), phone);
            customers.add(newCustomer);
            saveAll();
            nameField.setText(name.trim());
            JOptionPane.showMessageDialog(this, "New customer created!");
        }
    }
    
    private void addToBill() {
        if (phoneField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please find or add a customer first!");
            return;
        }
        
        Product p = productList.getSelectedValue();
        if (p == null) {
            JOptionPane.showMessageDialog(this, "Please select a product!");
            return;
        }
        try {
            double qty = Double.parseDouble(qtyField.getText().trim());
            if (qty <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be greater than 0!");
                return;
            }
            
            // For pieces, quantity must be whole number
            if ("pcs".equals(p.getUnitType()) && qty != (int) qty) {
                JOptionPane.showMessageDialog(this, "For pieces, quantity must be a whole number!");
                return;
            }
            
            // Check stock availability
            double availableStock = p.getStock();
            if (qty > availableStock) {
                JOptionPane.showMessageDialog(this, "Not enough stock! Available: " + availableStock + " " + p.getUnitType());
                return;
            }
            
            double subtotal = qty * p.getPrice();
            billModel.addRow(new Object[]{
                p.getName(), 
                qty,
                p.getUnitType(),
                String.format("Tk %.2f", p.getPrice()), 
                String.format("Tk %.2f", subtotal)
            });
            
            updateTotal();
            qtyField.setText("");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid quantity!");
        }
    }
    
    private void updateTotal() {
        double total = 0;
        for (int i = 0; i < billModel.getRowCount(); i++) {
            String subtotalStr = (String) billModel.getValueAt(i, 4);
            total += Double.parseDouble(subtotalStr.replace("Tk ", "").trim());
        }
        totalLabel.setText("Total: Tk " + String.format("%.2f", total));
    }
    
    private void generateBill() {
        if (phoneField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No customer selected!");
            return;
        }
        
        if (billModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No items in the bill!");
            return;
        }
        
        // Find customer
        Customer customer = null;
        for (Customer c : customers) {
            if (c.getPhone().equals(phoneField.getText().trim())) {
                customer = c;
                break;
            }
        }
        
        if (customer == null) {
            JOptionPane.showMessageDialog(this, "Customer not found!");
            return;
        }
        
        // Ask for amount paid
        String amountPaidStr = JOptionPane.showInputDialog(this, "Enter amount paid by customer:");
        if (amountPaidStr == null || amountPaidStr.trim().isEmpty()) {
            return;
        }
        
        try {
            double amountPaid = Double.parseDouble(amountPaidStr);
            double total = 0;
            List<String> items = new ArrayList<>();
            
            for (int i = 0; i < billModel.getRowCount(); i++) {
                String productName = (String) billModel.getValueAt(i, 0);
                double qty = (Double) billModel.getValueAt(i, 1);
                String unit = (String) billModel.getValueAt(i, 2);
                String priceStr = (String) billModel.getValueAt(i, 3);
                String subtotalStr = (String) billModel.getValueAt(i, 4);
                
                // Update product stock
                for (Product p : products) {
                    if (p.getName().equals(productName)) {
                        p.reduceStock(qty);
                        break;
                    }
                }
                
                items.add(String.format("%-15s %5.3f %3s   %7s   %8s", 
                    productName.length() > 15 ? productName.substring(0, 12) + "..." : productName,
                    qty, unit, priceStr, subtotalStr));
                total += Double.parseDouble(subtotalStr.replace("Tk ", "").trim());
            }
            
            if (amountPaid < total) {
                JOptionPane.showMessageDialog(this, "Amount paid is less than total amount!");
                return;
            }
            
            double change = amountPaid - total;
            
            Bill bill = new Bill(billId++, customer, items, total, new Date(), currentUser, amountPaid, change);
            bills.add(bill);
            customer.addPurchase(bill);
            saveAll();
            
            // Show bill
            JTextArea billArea = new JTextArea(bill.toPrintableString());
            billArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            billArea.setEditable(false);
            JOptionPane.showMessageDialog(this, new JScrollPane(billArea), "Bill Generated", JOptionPane.INFORMATION_MESSAGE);
            
            // Reset form
            billModel.setRowCount(0);
            totalLabel.setText("Total: Tk 0.00");
            phoneField.setText("");
            nameField.setText("");
            productSearchField.setText("");
            productListModel.clear();
            refreshProductList();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid amount!");
        }
    }
    
    private void saveAll() {
        FileHandler.saveData(customers, "customers.ser");
        FileHandler.saveData(products, "products.ser");
        FileHandler.saveData(bills, "bills.ser");
        FileHandler.saveData(users, "users.ser");
    }
    
    private void loadAll() {
        Object c = FileHandler.loadData("customers.ser");
        Object p = FileHandler.loadData("products.ser");
        Object b = FileHandler.loadData("bills.ser");
        Object u = FileHandler.loadData("users.ser");
        if (c != null) customers = (List<Customer>) c;
        if (p != null) products = (List<Product>) p;
        if (b != null) bills = (List<Bill>) b;
        if (u != null) users = (List<User>) u;
        // Update IDs
        for (Customer customer : customers) {
            if (customer.getId() >= customerId) customerId = customer.getId() + 1;
        }
        for (Product product : products) {
            if (product.getId() >= productId) productId = product.getId() + 1;
        }
        for (Bill bill : bills) {
            if (bill.getBillNo() >= billId) billId = bill.getBillNo() + 1;
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new SupermarketBillingApp();
        });
    }
}