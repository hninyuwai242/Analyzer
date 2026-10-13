package server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public interface StockInterface extends Remote {

    boolean login(String username, String password) throws RemoteException;

    List<Map<String, Object>> getBranchStock(int branch_id) throws RemoteException;

    List<Map<String, Object>> getLowStockAlerts(int branch_id) throws RemoteException;

    List<Map<String, Object>> searchProducts(String keyword) throws RemoteException;

    boolean sellProduct(int branch_id,
                        int product_id,
                        int quantity,
                        String date) throws RemoteException;

    List<Map<String, Object>> getBranchSales(int branch_id) throws RemoteException;

    boolean recordSale(int branchId, int productId, int qty, double amount) throws RemoteException;
    
    List<Map<String, Object>> getTransactionHistory() throws RemoteException;

    boolean placeOrder(int branchId, int productId, int qty) throws RemoteException;

    List<Map<String, Object>> getMyOrders(int branch_id) throws RemoteException;

    List<Map<String, Object>> getAllProducts() throws RemoteException;
    
    int getProductQuantityInBranch(int bId, int pId) throws RemoteException;
    
    int getBranchId(String username) throws RemoteException;

    // --- Branch Name ယူရန် Method သစ် ---
    String getBranchName(int branchId) throws RemoteException;

    // Daily & Monthly Sales Report အတွက် ပြင်ဆင်ထားသော Method (selectedDate parameter ထည့်သွင်းထားသည်)
    List<Map<String, Object>> getSalesReport(int branchId, String reportType, String selectedDate) throws RemoteException;
}


