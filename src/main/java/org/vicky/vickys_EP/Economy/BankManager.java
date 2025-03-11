package org.vicky.vickys_EP.Economy;
import org.vicky.vickys_EP.utils.Database.daos.BankAccountDAO;
import org.vicky.vickys_EP.utils.Database.templates.BankAccount;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class BankManager {

    // Singleton instance
    private static BankManager instance;

    // Executor for handling timed transactions
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    // Store scheduled transactions per bank name
    private final ConcurrentHashMap<String, ScheduledFuture<?>> scheduledTransactions = new ConcurrentHashMap<>();

    // Private constructor to enforce singleton
    private BankManager() {
        // Initialization logic if needed
    }

    public static synchronized BankManager getInstance() {
        if (instance == null) {
            instance = new BankManager();
        }
        return instance;
    }

    /**
     * Create a new bank account.
     *
     * @param bankName the unique bank name
     * @return true if the bank was created, false if an account with that name already exists.
     */
    public boolean createBank(String bankName) {
        BankAccount existing = new BankAccountDAO().findByName(bankName);
        if (existing != null) {
            return false;
        }
        BankAccount account = new BankAccount(UUID.randomUUID(), bankName, 0.0);
        return new BankAccountDAO().saveOrUpdate(account);
    }

    /**
     * Get the balance of the bank account.
     *
     * @param bankName the unique bank name
     * @return the current balance, or 0.0 if the account does not exist.
     */
    public double getBankBalance(String bankName) {
        BankAccount account = new BankAccountDAO().findByName(bankName);
        return (account != null) ? account.getBalance() : 0.0;
    }

    /**
     * Deposit an amount into the bank account.
     *
     * @param bankName the unique bank name
     * @param amount   the amount to deposit
     * @return true if the deposit was successful; false otherwise.
     */
    public boolean bankDeposit(String bankName, double amount) {
        BankAccount account = new BankAccountDAO().findByName(bankName);
        if (account == null) {
            return false;
        }
        account.setBalance(account.getBalance() + amount);
        return new BankAccountDAO().saveOrUpdate(account);
    }

    /**
     * Withdraw an amount from the bank account.
     *
     * @param bankName the unique bank name
     * @param amount   the amount to withdraw
     * @return true if the withdrawal was successful (i.e., sufficient funds), false otherwise.
     */
    public boolean bankWithdraw(String bankName, double amount) {
        BankAccount account = new BankAccountDAO().findByName(bankName);
        if (account == null || account.getBalance() < amount) {
            return false;
        }
        account.setBalance(account.getBalance() - amount);
        return new BankAccountDAO().saveOrUpdate(account);
    }

    /**
     * Schedule a delayed bank transaction (deposit, withdrawal, etc.).
     * The transaction can be cancelled before execution.
     *
     * @param bankName    the unique bank name
     * @param transaction a Runnable encapsulating the transaction logic
     * @param delay       delay before execution
     * @param unit        time unit for the delay
     */
    public void scheduleBankTransaction(String bankName, Runnable transaction, long delay, TimeUnit unit) {
        ScheduledFuture<?> future = scheduler.schedule(transaction, delay, unit);
        scheduledTransactions.put(bankName, future);
    }

    /**
     * Cancel a scheduled bank transaction.
     *
     * @param bankName the unique bank name
     * @return true if the transaction was cancelled; false otherwise.
     */
    public boolean cancelScheduledBankTransaction(String bankName) {
        ScheduledFuture<?> future = scheduledTransactions.get(bankName);
        if (future != null && !future.isDone()) {
            boolean cancelled = future.cancel(false);
            if (cancelled) {
                scheduledTransactions.remove(bankName);
            }
            return cancelled;
        }
        return false;
    }

    /**
     * Shutdown the scheduler when the server stops.
     */
    public void shutdown() {
        scheduler.shutdown();
    }
}
