package bank;

import java.util.ArrayList;
import java.util.List;

/*
 * DSA: Binary Search Tree (BST)
 *
 * How it works:
 *   Every Account node has a left and right child.
 *   Accounts with a SMALLER account number go LEFT.
 *   Accounts with a LARGER account number go RIGHT.
 *
 *           ACC1003
 *          /       \
 *       ACC1001   ACC1005
 *                 /
 *              ACC1004
 *
 * Searching: at each node, go left or right — O(log n) average.
 * In-order traversal (left → node → right) gives sorted order.
 */
public class AccountBST {

    private Account root;

    // Insert an account into the BST
    public void insert(Account acc) {
        root = insertRec(root, acc);
    }

    private Account insertRec(Account node, Account acc) {
        if (node == null) return acc; // empty spot found, place it here

        if (acc.accountNumber.compareTo(node.accountNumber) < 0) {
            node.left = insertRec(node.left, acc);   // go left
        } else if (acc.accountNumber.compareTo(node.accountNumber) > 0) {
            node.right = insertRec(node.right, acc); // go right
        }
        return node;
    }

    // Search for an account by account number — O(log n)
    public Account search(String accountNumber) {
        return searchRec(root, accountNumber);
    }

    private Account searchRec(Account node, String accountNumber) {
        if (node == null) return null; // not found

        int cmp = accountNumber.compareTo(node.accountNumber);
        if (cmp == 0) return node;        // found it!
        if (cmp < 0)  return searchRec(node.left,  accountNumber); // go left
        else          return searchRec(node.right, accountNumber); // go right
    }

    // In-order traversal — returns all accounts sorted by account number
    public List<Account> getAllSorted() {
        List<Account> result = new ArrayList<>();
        inOrder(root, result);
        return result;
    }

    private void inOrder(Account node, List<Account> result) {
        if (node == null) return;
        inOrder(node.left, result);  // visit left subtree first
        result.add(node);            // then this node
        inOrder(node.right, result); // then right subtree
    }

    // Search accounts whose name contains the given keyword
    public List<Account> searchByName(String keyword) {
        List<Account> all    = getAllSorted();
        List<Account> result = new ArrayList<>();
        for (Account a : all) {
            if (a.ownerName.toLowerCase().contains(keyword.toLowerCase())) {
                result.add(a);
            }
        }
        return result;
    }
}
