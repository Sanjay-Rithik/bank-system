package bank;

// A single transaction — also acts as a LinkedList node
public class Transaction {

    String id;
    String type;      // DEPOSIT, WITHDRAW
    double amount;
    double balanceAfter;
    String note;

    // LinkedList pointer to the next (older) transaction
    Transaction next;

    public Transaction(String id, String type, double amount, double balanceAfter, String note) {
        this.id           = id;
        this.type         = type;
        this.amount       = amount;
        this.balanceAfter = balanceAfter;
        this.note         = note;
        this.next         = null;
    }
}
