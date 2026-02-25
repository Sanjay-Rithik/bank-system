#include <iostream>
#include <map>
#include <vector>
#include <string>

using namespace std;

class Account {
private:
    int accountNumber;
    string name;
    double balance;
    vector<string> transactionHistory;   // Transaction history

public:
    Account() {}  // Default constructor for map

    Account(int accNo, string accName, double initialBalance) {
        accountNumber = accNo;
        name = accName;
        balance = initialBalance;
        transactionHistory.push_back("Account created with balance Rs. " + to_string(initialBalance));
    }

    int getAccountNumber() const {
        return accountNumber;
    }

    void deposit(double amount) {
        if (amount > 0) {
            balance += amount;
            transactionHistory.push_back("Deposited Rs. " + to_string(amount));
            cout << "Amount deposited successfully.\n";
        } else {
            cout << "Invalid amount.\n";
        }
    }

    void withdraw(double amount) {
        if (amount > 0 && amount <= balance) {
            balance -= amount;
            transactionHistory.push_back("Withdrawn Rs. " + to_string(amount));
            cout << "Amount withdrawn successfully.\n";
        } else {
            cout << "Invalid or insufficient amount.\n";
        }
    }

    void display() const {
        cout << "Account Number: " << accountNumber << "\n";
        cout << "Name: " << name << "\n";
        cout << "Balance: Rs. " << balance << "\n";
    }

    void showTransactionHistory() const {
        cout << "\n--- Transaction History ---\n";
        for (const auto &t : transactionHistory) {
            cout << t << endl;
        }
    }
};

// Using map instead of vector
map<int, Account> accounts;

// Faster search using map
Account* findAccount(int accNo) {
    auto it = accounts.find(accNo);
    if (it != accounts.end()) {
        return &(it->second);
    }
    return nullptr;
}

void createAccount() {
    int accNo;
    string name;
    double initialBalance;

    cout << "Enter Account Number: ";
    cin >> accNo;

    if (findAccount(accNo)) {
        cout << "Account already exists!\n";
        return;
    }

    cout << "Enter Name: ";
    cin.ignore();
    getline(cin, name);

    cout << "Enter Initial Balance: ";
    cin >> initialBalance;

    accounts[accNo] = Account(accNo, name, initialBalance);
    cout << "Account created successfully.\n";
}

void depositMoney() {
    int accNo;
    double amount;

    cout << "Enter Account Number: ";
    cin >> accNo;

    Account* acc = findAccount(accNo);
    if (acc) {
        cout << "Enter amount to deposit: ";
        cin >> amount;
        acc->deposit(amount);
    } else {
        cout << "Account not found.\n";
    }
}

void withdrawMoney() {
    int accNo;
    double amount;

    cout << "Enter Account Number: ";
    cin >> accNo;

    Account* acc = findAccount(accNo);
    if (acc) {
        cout << "Enter amount to withdraw: ";
        cin >> amount;
        acc->withdraw(amount);
    } else {
        cout << "Account not found.\n";
    }
}

void displayAccount() {
    int accNo;

    cout << "Enter Account Number: ";
    cin >> accNo;

    Account* acc = findAccount(accNo);
    if (acc) {
        acc->display();
    } else {
        cout << "Account not found.\n";
    }
}

// New Search Function
void searchAccount() {
    int accNo;

    cout << "Enter Account Number to search: ";
    cin >> accNo;

    Account* acc = findAccount(accNo);
    if (acc) {
        cout << "Account found!\n";
        acc->display();
    } else {
        cout << "Account not found.\n";
    }
}

// Show Transaction History
void showHistory() {
    int accNo;

    cout << "Enter Account Number: ";
    cin >> accNo;

    Account* acc = findAccount(accNo);
    if (acc) {
        acc->showTransactionHistory();
    } else {
        cout << "Account not found.\n";
    }
}

// Delete using map erase
void deleteAccount() {
    int accNo;

    cout << "Enter Account Number to close: ";
    cin >> accNo;

    if (accounts.erase(accNo)) {
        cout << "Account closed successfully.\n";
    } else {
        cout << "Account not found.\n";
    }
}

int main() {
    int choice;

    do {
        cout << "\n====== Bank Management System ======\n";
        cout << "1. Create Account\n";
        cout << "2. Deposit Money\n";
        cout << "3. Withdraw Money\n";
        cout << "4. Display Account\n";
        cout << "5. Search Account\n";
        cout << "6. Transaction History\n";
        cout << "7. Close Account\n";
        cout << "8. Exit\n";
        cout << "Enter your choice: ";
        cin >> choice;

        switch(choice) {
            case 1: createAccount(); break;
            case 2: depositMoney(); break;
            case 3: withdrawMoney(); break;
            case 4: displayAccount(); break;
            case 5: searchAccount(); break;
            case 6: showHistory(); break;
            case 7: deleteAccount(); break;
            case 8: cout << "Thank you for using the system!\n"; break;
            default: cout << "Invalid choice. Try again.\n";
        }

    } while(choice != 8);

    return 0;
}
