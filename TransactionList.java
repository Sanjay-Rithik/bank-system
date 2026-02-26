package bank;

import java.util.ArrayList;
import java.util.List;

/*
 * DSA: Singly Linked List
 *
 * How it works:
 *   head → tx5 → tx4 → tx3 → tx2 → tx1 → null
 *
 * New transactions are added at the FRONT (head) so the
 * most recent transaction always comes first — O(1) insert.
 *
 * To read all transactions we just follow .next pointers
 * from head until we hit null.
 */
public class TransactionList {

    private Transaction head; // points to the most recent transaction
    private int size;

    public TransactionList() {
        head = null;
        size = 0;
    }

    // Add a new transaction at the front — O(1)
    public void add(Transaction t) {
        t.next = head; // new node points to old head
        head   = t;    // head now points to new node
        size++;
    }

    // Return all transactions as a plain list (for the API to send to frontend)
    public List<Transaction> getAll() {
        List<Transaction> result = new ArrayList<>();
        Transaction current = head;
        while (current != null) {
            result.add(current);
            current = current.next; // follow the pointer
        }
        return result;
    }

    public int size() { return size; }
}
