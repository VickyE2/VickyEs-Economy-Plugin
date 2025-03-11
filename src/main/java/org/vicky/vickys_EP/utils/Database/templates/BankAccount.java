package org.vicky.vickys_EP.utils.Database.templates;

import jakarta.persistence.*;
import org.vicky.utilities.DatabaseTemplate;

import java.util.UUID;

@Entity
@Table(name = "Bank_Accounts")
public class BankAccount implements DatabaseTemplate {

    @Id
    @Column(name = "bank_id", unique = true, nullable = false)
    private UUID bankId;

    @Column(name = "bank_name", unique = true, nullable = false)
    private String bankName;

    @Column(name = "balance", nullable = false)
    private double balance;

    public BankAccount() {}

    public BankAccount(UUID bankId, String bankName, double balance) {
        this.bankId = bankId;
        this.bankName = bankName;
        this.balance = balance;
    }

    public UUID getBankId() {
        return bankId;
    }

    public void setBankId(UUID bankId) {
        this.bankId = bankId;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}

