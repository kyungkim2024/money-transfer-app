package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.exception.DaoException;
import com.techelevator.tenmo.model.Transfer;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class JdbcTransferDao implements TransferDao{

    private final JdbcTemplate jdbcTemplate;

    public JdbcTransferDao(JdbcTemplate jdbcTemplate, AccountDao accountDao) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Transfer> getAllTransfers() {
        List<Transfer> transferList = new ArrayList<>();
        String sql = "SELECT * FROM transfer;";
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql);
            while (results.next()) {
                transferList.add(mapRowToTransfer(results));
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
        return transferList;
    }

    @Override
    public List<Transfer> getTransferHistoryByUserId(int userId) {
        List<Transfer> transferList = new ArrayList<>();
        String sql = "SELECT t.transfer_id, t.transfer_type_id, t.transfer_status_id, t.account_from, t.account_to, t.amount FROM transfer t " +
                "JOIN account a ON a.account_id = t.account_from OR a.account_id = t.account_to " +
                "WHERE a.user_id = ?;";
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, userId);
            while (results.next()) {
                transferList.add(mapRowToTransfer(results));
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
        return transferList;
    }

    @Override
    public List<Transfer> getTransferHistoryInPendingByUserId(int userId) {
        List<Transfer> transferList = new ArrayList<>();
        String sql = "SELECT t.transfer_id, t.transfer_type_id, t.transfer_status_id, t.account_from, t.account_to, t.amount FROM transfer t " +
                "JOIN account a ON a.account_id = t.account_from OR a.account_id = t.account_to " +
                "WHERE a.user_id = ? AND t.transfer_status_id = 1;";
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, userId);
            while (results.next()) {
                transferList.add(mapRowToTransfer(results));
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
        return transferList;
    }

    @Override
    public Transfer getTransferByTransferId(int userId, int transferId) {
        Transfer transfer = null;
        String sql = "SELECT t.transfer_id, t.transfer_type_id, t.transfer_status_id, t.account_from, t.account_to, t.amount FROM transfer t " +
                "JOIN account a ON a.account_id = t.account_from OR a.account_id = t.account_to " +
                "WHERE a.user_id = ? AND t.transfer_id = ?;";
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, userId, transferId);
            if (results.next()) {
                transfer = mapRowToTransfer(results);
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
        return transfer;
    }

    @Override
    public Transfer updateTransfer(int userId, int transferId, Transfer transfer) {
        Transfer updatedTransfer = null;
        String sql = "UPDATE transfer SET transfer_status_id = ? WHERE transfer_id = ?;";
        try {
            int numberOfRows = jdbcTemplate.update(sql, transfer.getTransferStatusId(), transferId);
            if (numberOfRows == 0) {
                throw new DaoException("Zero rows affected, expected at least one");
            } else {
                updatedTransfer = getTransferByOnlyTransferId(transfer.getTransferId());
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation", e);
        }
        return updatedTransfer;
    }

    @Override
    public Transfer getTransferByOnlyTransferId(int transferId) {
        Transfer transfer = null;
        String sql = "SELECT transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount FROM transfer WHERE transfer_id = ?;";
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, transferId);
            if (results.next()) {
                transfer = mapRowToTransfer(results);
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
        return transfer;
    }

    @Override
    public Transfer createTransfer(int userId, Transfer updatedTransfer) {
        Transfer newTransfer = null;
        String sql = "INSERT INTO transfer (transfer_type_id, transfer_status_id, account_from, account_to, amount) VALUES (?, ?, ?, ?, ?) RETURNING transfer_id;";
        try {
            int newTransferId = jdbcTemplate.queryForObject(sql, int.class, updatedTransfer.getTransferTypeId(), updatedTransfer.getTransferStatusId(), updatedTransfer.getAccountFrom(), updatedTransfer.getAccountTo(), updatedTransfer.getAmount());
            newTransfer = getTransferByOnlyTransferId(newTransferId);
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation", e);
        }
        return newTransfer;
    }

    //TODO
    // Send TE bucks

    //TODO
    // Request TE bucks

    private Transfer mapRowToTransfer(SqlRowSet results) {
        Transfer transfer = new Transfer();
        transfer.setTransferId(results.getInt("transfer_id"));
        transfer.setTransferTypeId(results.getInt("transfer_type_id"));
        transfer.setTransferStatusId(results.getInt("transfer_status_id"));
        transfer.setAccountFrom(results.getInt("account_from"));
        transfer.setAccountTo(results.getInt("account_to"));
        transfer.setAmount(results.getBigDecimal("amount"));
        return transfer;
    }

}
