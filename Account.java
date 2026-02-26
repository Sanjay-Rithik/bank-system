package bank;

// Represents a single bank account
public class Account {

    String accountNumber;
    String ownerName;
    String email;
    double balance;
    String type; // SAVINGS or CURRENT

    // BST pointers — used by AccountBST
    Account left, right;

    public Account(String accountNumber, String ownerName, String email, double balance, String type) {
        this.accountNumber = accountNumber;
        this.ownerName     = ownerName;
        this.email         = email;
        this.balance       = balance;
        this.type          = type;
        this.left          = null;
        this.right         = null;
    }
}
