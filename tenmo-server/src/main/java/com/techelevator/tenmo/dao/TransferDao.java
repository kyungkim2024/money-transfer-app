package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transfer;

import java.util.List;

public interface TransferDao {

    List<Transfer> getAllTransfers();

    List<Transfer> getTransferHistoryByUserId(int userId);

    List<Transfer> getTransferHistoryInPendingByUserId(int userId);

    Transfer getTransferByTransferId(int userId, int transferId);

    Transfer updateTransfer(int userId, int transferId, Transfer transfer);

    Transfer getTransferByOnlyTransferId(int transferId);

    Transfer createTransfer(int userId, Transfer updatedTransfer);

    //TODO
    // Need Send TE bucks

    //TODO
    // Need Request TE bucks
}
