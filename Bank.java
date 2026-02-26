package bank;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Bank.java — the core of the system.
 *
 * It uses three DSA structures:
 *   1. HashMap        — stores all accounts for fast O(1) lookup by account number
 *   2. AccountBST     — stores accounts in a BST for sorted listing and search
 *   3. TransactionList— a linked list per account storing transaction history
 *   4. UndoStack      — a stack that lets us undo the last transaction
 *
 * All business logic (create account, deposit, withdraw, transfer) lives here.
 */
public class Bank {

    // HashMap: accountNumber -> Account  (O(1) lookup)
    private HashMap<String, Account> accounts = new HashMap<>();

    // BST: for sorted listing and name search
    private AccountBST bst = new AccountBST();

    // Per-account transaction history (one LinkedList per account)
    private HashMap<String, TransactionList> history = new HashMap<>();

    // Undo stack (shared across all accounts)
    private UndoStack undoStack = new UndoStack();

    private int accountCounter = 1000;
    private int txCounter      = 1;

    // ── Seed some demo data on startup ───────────────────────────────────────
    public Bank() {
        createAccount("Alice Johnson",  "alice@email.com",  5000, "SAVINGS");
        createAccount("Bob Smith",      "bob@email.com",    8500, "CURRENT");
        createAccount("Carol Williams", "carol@email.com", 12000, "SAVINGS");
        createAccount("David Brown",    "david@email.com",  3200, "CURRENT");

        // Add a few transactions so history isn't empty
        deposit("ACC1001", 2000, "Initial top-up");
        withdraw("ACC1002", 500, "ATM withdrawal");
        deposit("ACC1003", 1500, "Salary credit");
    }

    // ── Create Account ───────────────────────────────────────────────────────
    public Account createAccount(String name, String email, double initialDeposit, String type) {
        String accNum = "ACC" + (++accountCounter);
        Account acc   = new Account(accNum, name, email, initialDeposit, type);

        accounts.put(accNum, acc);   // add to HashMap
        bst.insert(acc);             // add to BST
        history.put(accNum, new TransactionList()); // create empty linked list

        // Record the opening deposit as first transaction
        String txId = "TX" + (txCounter++);
        history.get(accNum).add(new Transaction(txId, "DEPOSIT", initialDeposit, initialDeposit, "Account opened"));

        return acc;
    }

    // ── Deposit ──────────────────────────────────────────────────────────────
    public Transaction deposit(String accNum, double amount, String note) {
        Account acc = getAccount(accNum);
        if (acc == null)    throw new RuntimeException("Account not found.");
        if (amount <= 0)    throw new RuntimeException("Amount must be positive.");

        acc.balance += amount;

        Transaction tx = new Transaction("TX" + (txCounter++), "DEPOSIT", amount, acc.balance, note);
        history.get(accNum).add(tx); // prepend to linked list
        undoStack.push(tx);          // push to undo stack
        return tx;
    }

    // ── Withdraw ─────────────────────────────────────────────────────────────
    public Transaction withdraw(String accNum, double amount, String note) {
        Account acc = getAccount(accNum);
        if (acc == null)             throw new RuntimeException("Account not found.");
        if (amount <= 0)             throw new RuntimeException("Amount must be positive.");
        if (acc.balance < amount)    throw new RuntimeException("Insufficient funds.");

        acc.balance -= amount;

        Transaction tx = new Transaction("TX" + (txCounter++), "WITHDRAW", amount, acc.balance, note);
        history.get(accNum).add(tx); // prepend to linked list
        undoStack.push(tx);          // push to undo stack
        return tx;
    }

    // ── Undo Last Transaction ────────────────────────────────────────────────
    public String undo() {
        Transaction tx = undoStack.pop(); // pop from stack
        if (tx == null) return "Nothing to undo.";

        // Find which account this transaction belongs to
        for (Account acc : accounts.values()) {
            List<Transaction> txList = history.get(acc.accountNumber).getAll();
            if (!txList.isEmpty() && txList.get(0).id.equals(tx.id)) {
                // Reverse the transaction
                if (tx.type.equals("DEPOSIT"))  acc.balance -= tx.amount;
                if (tx.type.equals("WITHDRAW")) acc.balance += tx.amount;
                return "Undid " + tx.type + " of " + tx.amount + " on " + acc.accountNumber;
            }
        }
        return "Could not find account for undo.";
    }

    // ── Getters ──────────────────────────────────────────────────────────────
    public Account getAccount(String accNum) {
        return accounts.get(accNum); // HashMap lookup — O(1)
    }

    public List<Account> getAllAccounts() {
        return bst.getAllSorted(); // BST in-order — sorted by account number
    }

    public List<Account> searchByName(String name) {
        return bst.searchByName(name);
    }

    public List<Transaction> getHistory(String accNum) {
        TransactionList list = history.get(accNum);
        if (list == null) return Collections.emptyList();
        return list.getAll(); // traverse linked list
    }

    public Map<String, Object> getStats() {
        List<Account> all = getAllAccounts();
        double total = all.stream().mapToDouble(a -> a.balance).sum();
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalAccounts", all.size());
        stats.put("totalBalance",  total);
        stats.put("undoStackSize", undoStack.size());
        return stats;
    }
}
