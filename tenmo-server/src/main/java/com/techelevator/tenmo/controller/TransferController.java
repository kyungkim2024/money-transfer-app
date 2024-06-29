package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.TransferDao;
import com.techelevator.tenmo.exception.DaoException;
import com.techelevator.tenmo.model.Transfer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/user")
@PreAuthorize("isAuthenticated()")
public class TransferController {

    private final TransferDao transferDao;

    public TransferController(TransferDao transferDao) {
        this.transferDao = transferDao;
    }

    // Get the entire transfer history
    @RequestMapping(path = "/all/transfer", method = RequestMethod.GET)
    @PreAuthorize("hasRole('USER')")
    public List<Transfer> getAllTransferList() {
        return transferDao.getAllTransfers();
    }

    // Get the transfer history of the current user
    @RequestMapping(path = "/{id}/transfer", method = RequestMethod.GET)
    @PreAuthorize("hasRole('USER')")
    public List<Transfer> getTransferHistory(@PathVariable int id) {
        return transferDao.getTransferHistoryByUserId(id);
    }

    // Get the pending transfer history of the current user
    @RequestMapping(path = "/{id}/transfer/pending", method = RequestMethod.GET)
    @PreAuthorize("hasRole('USER')")
    public List<Transfer> getPendingRequests(@PathVariable int id) {
        return transferDao.getTransferHistoryInPendingByUserId(id);
    }

    // Get the transfer with specific transfer id
    @RequestMapping(path = "/{id}/transfer/{transferId}", method = RequestMethod.GET)
    @PreAuthorize("hasRole('USER')")
    public Transfer getTransfer(@PathVariable int id, @PathVariable int transferId) {
        return transferDao.getTransferByTransferId(id, transferId);
    }

    // Update the transfer with specific transfer id and update Pending to Approved/Rejected
    @RequestMapping(path = "/{id}/transfer/{transferId}", method = RequestMethod.PUT)
    @PreAuthorize("hasRole('USER')")
    public Transfer updateTransferStatus(@PathVariable int id, @PathVariable int transferId, @RequestBody Transfer transfer) {
        transfer.setTransferId(transferId);
        try {
            return transferDao.updateTransfer(id, transferId, transfer);
        } catch(DaoException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Transfer not found");
        }
    }

    // Post new transfer for Send transfer
    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(path = "/{id}/transfer", method = RequestMethod.POST)
    @PreAuthorize("hasRole('USER')")
    public Transfer postNewTransfer(@PathVariable int id, @RequestBody  Transfer transfer) {
        return transferDao.createTransfer(id, transfer);
    }

    //TODO
    @PostMapping("/user/{id}/transfer/sending}")
    public ResponseEntity<Void> sendBucks(@RequestBody Transfer transfer) {
        //transferDao.sendBucks(transfer);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    //TODO
    @PostMapping("/user/{id}/transfer/requesting")
    public ResponseEntity<Void> requestBucks(@RequestBody Transfer transfer) {
        //transferDao.requestBucks(transfer);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}