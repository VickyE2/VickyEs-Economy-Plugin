package org.vicky.vickys_EP.utils.Database.daos;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.vicky.utilities.DatabaseManager.HibernateUtil;
import org.vicky.vickys_EP.utils.Database.templates.BankAccount;

import java.util.UUID;

public class BankAccountDAO {

    private final SessionFactory sessionFactory;

    public BankAccountDAO() {
        this.sessionFactory = HibernateUtil.getSessionFactory();
    }

    /**
     * Find a BankAccount by its UUID.
     *
     * @param bankId the unique identifier of the bank account
     * @return the BankAccount if found, or null otherwise.
     */
    public BankAccount findById(UUID bankId) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(BankAccount.class, bankId);
        }
    }

    /**
     * Find a BankAccount by its bank name.
     *
     * @param bankName the unique bank name
     * @return the BankAccount if found, or null otherwise.
     */
    public BankAccount findByName(String bankName) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM BankAccount WHERE bankName = :bankName", BankAccount.class)
                    .setParameter("bankName", bankName)
                    .uniqueResult();
        }
    }

    /**
     * Save or update a BankAccount.
     *
     * @param account the BankAccount entity to be saved or updated
     * @return true if the operation was successful, false otherwise.
     */
    public boolean saveOrUpdate(BankAccount account) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.saveOrUpdate(account);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Delete a BankAccount.
     *
     * @param account the BankAccount entity to delete
     * @return true if the deletion was successful, false otherwise.
     */
    public boolean delete(BankAccount account) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.delete(account);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return false;
        }
    }
}