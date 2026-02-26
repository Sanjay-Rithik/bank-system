package bank;

import java.util.ArrayList;

/*
 * DSA: Stack (Last In, First Out)
 *
 * How it works:
 *   Think of a stack of plates.
 *   You always add and remove from the TOP.
 *
 *   push(tx)  → put a plate on top
 *   pop()     → take the top plate off  (undo!)
 *   peek()    → look at the top plate without removing
 *
 * We use this to undo the last transaction.
 * Every time a deposit or withdrawal happens, we push it here.
 * When the user clicks Undo, we pop the top item and reverse it.
 */
public class UndoStack {

    private ArrayList<Transaction> stack = new ArrayList<>();

    // Push a transaction onto the top of the stack
    public void push(Transaction t) {
        stack.add(t);
    }

    // Pop the top transaction off the stack (returns null if empty)
    public Transaction pop() {
        if (stack.isEmpty()) return null;
        return stack.remove(stack.size() - 1); // remove from the end = top
    }

    // Look at the top without removing
    public Transaction peek() {
        if (stack.isEmpty()) return null;
        return stack.get(stack.size() - 1);
    }

    public boolean isEmpty() { return stack.isEmpty(); }
    public int size()        { return stack.size(); }
}
