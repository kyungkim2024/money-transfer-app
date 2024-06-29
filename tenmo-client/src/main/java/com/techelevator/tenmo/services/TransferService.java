package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.AuthenticatedUser;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import com.techelevator.util.BasicLogger;
import org.springframework.http.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

public class TransferService {

    private static final String API_BASE_URL = "http://localhost:8080/";
    private final String baseUrl;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ConsoleService consoleService = new ConsoleService();
    private final AccountService accountService = new AccountService(API_BASE_URL);


    public TransferService(String url) {
        this.baseUrl = url;
    }

    public void getTransferHistory(AuthenticatedUser currentUser) {
        Transfer[] transferHistoryArray = null;
        try {
            ResponseEntity<Transfer[]> response = restTemplate.exchange(baseUrl + "user/{id}/transfer", HttpMethod.GET, makeAuthEntity(currentUser), Transfer[].class, currentUser.getUser().getId());
            transferHistoryArray = response.getBody();
            System.out.println("-------------------------------------");
            System.out.println("Transfer                             ");
            System.out.println("ID          From/To            Amount");
            System.out.println("-------------------------------------");
            for (Transfer transfer : transferHistoryArray) {
                // 1: Request
                if (transfer.getTransferTypeId() == 1) {
                    System.out.printf("%d      FROM: %s          $ %s\n", transfer.getTransferId(),
                            getUserNameByAccountId(currentUser, transfer.getAccountFrom()), transfer.getAmount());
                    // 2: Send
                } else if (transfer.getTransferTypeId() == 2) {
                    System.out.printf("%d      TO: %s            $ %s\n", transfer.getTransferId(),
                            getUserNameByAccountId(currentUser, transfer.getAccountTo()), transfer.getAmount());
                }
            }
            System.out.println("-------------------------------------");
            getTransferDetailByTransferId(currentUser);
        } catch (RestClientException e) {
            BasicLogger.log("Error retrieving transfer record: " + e.getMessage());
        } catch (NullPointerException e) {
            BasicLogger.log("No transfer record found!");
        }
    }

    private void getTransferDetailByTransferId(AuthenticatedUser currentUser) {
        while (true) {
            int transferId = consoleService.promptForInt("Please input transfer ID to view detail (0 to cancel): ");
            if (transferId == 0) {
                break;
            }
            Transfer transfer = getTransferByTransferId(currentUser, transferId);
            System.out.println("-------------------------------------");
            System.out.println("           Transfer Detail           ");
            System.out.println("-------------------------------------");
            System.out.printf("ID: %d\n" +
                            "FROM: %s\n" +
                            "TO: %s\n" +
                            "TYPE: %s\n" +
                            "STATUS: %s\n" +
                            "AMOUNT: %s\n", transfer.getTransferId(), getUserNameByAccountId(currentUser, transfer.getAccountFrom()),
                    getUserNameByAccountId(currentUser, transfer.getAccountTo()),
                    getTypeString(transfer), getStatusString(transfer), transfer.getAmount());
            System.out.println("-------------------------------------");
        }
    }

    public void getPendingRequests(AuthenticatedUser currentUser) {
        Transfer[] transferHistoryPendingArray = null;
        try {
            ResponseEntity<Transfer[]> response = restTemplate.exchange(baseUrl + "user/{id}/transfer/pending/", HttpMethod.GET,
                    makeAuthEntity(currentUser), Transfer[].class, currentUser.getUser().getId());
            transferHistoryPendingArray = response.getBody();
            System.out.println("-------------------------------------");
            System.out.println("Transfer                             ");
            System.out.println("ID             To              Amount");
            System.out.println("-------------------------------------");
            if (transferHistoryPendingArray.length != 0) {
                int currentUserAccountId = accountService.getAccountByUserId(currentUser).getAccount_id();
                for (Transfer transfer : transferHistoryPendingArray) {
                    if (transfer.getAccountFrom() == currentUserAccountId) {
                        System.out.printf("%d      TO: %s            $ %s\n", transfer.getTransferId(),
                                getUserNameByAccountId(currentUser, transfer.getAccountTo()), transfer.getAmount());
                    }
                }
                System.out.println("-------------------------------------");
            } else {
                System.out.println("There is no pending request0!");
                System.out.println("Please hit 0 to cancel");
            }
        } catch (RestClientException e) {
            System.out.println("Error retrieving pending transfer record: " + e.getMessage());
        } catch (NullPointerException e) {
            System.out.println("No transfer record found!");
        }
    }

    public Transfer getTransferByTransferId(AuthenticatedUser currentUser, int transferId) {
        Transfer transfer = null;
        try {
            ResponseEntity<Transfer> response = restTemplate.exchange(baseUrl + "user/{id}/transfer/{transferId}", HttpMethod.GET, makeAuthEntity(currentUser), Transfer.class, currentUser.getUser().getId(), transferId);
            transfer = response.getBody();
        } catch (RestClientException e) {
            BasicLogger.log("Error retrieving pending transfer record: " + e.getMessage());
        } catch (NullPointerException e) {
            BasicLogger.log("No transfer record found!");
        }
        return transfer;
    }

    public void postTransfer(int sendOrRequestId, int accountToOrFromId, BigDecimal amount, AuthenticatedUser currentUser, Account currentUserAccount) {
        HttpEntity<Transfer> entity = null;
        if (sendOrRequestId == 2) {
            Transfer sendTransfer = makeSendTransfer(accountToOrFromId, amount, currentUserAccount);
            entity = makeTransferEntity(sendTransfer, currentUser);
        } else if (sendOrRequestId == 1) {
            Transfer requestTransfer = makeRequestTransfer(accountToOrFromId, amount, currentUserAccount);
            entity = makeTransferEntity(requestTransfer, currentUser);
        }
        try {
            restTemplate.exchange(baseUrl + "user/{id}/transfer", HttpMethod.POST, entity, Transfer.class, currentUser.getUser().getId());
        } catch (RestClientResponseException e) {
            BasicLogger.log(e.getRawStatusCode() + " : " + e.getStatusText());
        } catch (ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
    }

    public boolean approveRequest(int transferId, AuthenticatedUser currentUser) {
        Transfer transfer = getTransferByTransferId(currentUser, transferId);
        transfer.setTransferStatusId(2);
        HttpEntity<Transfer> transferEntity = makeTransferEntity(transfer, currentUser);
        boolean success = false;
        try {
            restTemplate.exchange(baseUrl + "user/{id}/transfer/{transferId}", HttpMethod.PUT, transferEntity, Transfer.class, currentUser.getUser().getId(), transferId);
            success = true;
        } catch (RestClientResponseException e) {
            BasicLogger.log(e.getRawStatusCode() + " : " + e.getStatusText());
        } catch (ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return success;
    }

    public void rejectRequest(AuthenticatedUser currentUser) {
        int transferId = consoleService.promptForInt("Please select a transfer ID to reject: ");
        Transfer transfer = getTransferByTransferId(currentUser, transferId);
        // Set transfer_status_id = 3 for 'Reject'
        transfer.setTransferStatusId(3);
        HttpEntity<Transfer> entity = makeTransferEntity(transfer, currentUser);
        //boolean success = false;
        try {
            restTemplate.exchange(baseUrl + "user/{id}/transfer/{transferId}", HttpMethod.PUT, entity, Transfer.class, currentUser.getUser().getId(), transferId);
            //success = true;
        } catch (RestClientResponseException e) {
            BasicLogger.log(e.getRawStatusCode() + " : " + e.getStatusText());
        } catch (ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        //return success;
    }

    // Helper methods

    // For Sending TE Bucks : currentUser = money sender
    // accountToId : receiver's account ID
    public Transfer makeSendTransfer(int accountToId, BigDecimal amount, Account currentUserAccount) {
        Transfer newTransfer = new Transfer();
        newTransfer.setTransferTypeId(2);
        newTransfer.setTransferStatusId(2);
        int accountFrom = currentUserAccount.getAccount_id();
        newTransfer.setAccountFrom(accountFrom);
        int accountTo = accountToId;
        newTransfer.setAccountTo(accountTo);
        newTransfer.setAmount(amount);
        return newTransfer;
    }

    // For Requesting TE Bucks : currentUser = money receiver
    // accountFromId : user who got requested for money from the current User
    public Transfer makeRequestTransfer(int accountFromId, BigDecimal amount, Account currentUserAccount) {
        Transfer newTransfer = new Transfer();
        newTransfer.setTransferTypeId(1);
        newTransfer.setTransferStatusId(1);
        int accountFrom = accountFromId;
        newTransfer.setAccountFrom(accountFrom);
        int accountTo = currentUserAccount.getAccount_id();
        newTransfer.setAccountTo(accountTo);
        newTransfer.setAmount(amount);
        return newTransfer;
    }

    private String getTypeString(Transfer transfer) {
        String type = null;
        if (transfer.getTransferTypeId() == 1) {
            type = "Request";
        } else if (transfer.getTransferTypeId() == 2) {
            type = "Send";
        }
        return type;
    }

    private String getStatusString(Transfer transfer) {
        String status = null;
        if (transfer.getTransferStatusId() == 1) {
            status = "Pending";
        } else if (transfer.getTransferStatusId() == 2) {
            status = "Approved";
        } else if (transfer.getTransferStatusId() == 3) {
            status = "Rejected";
        }
        return status;
    }

    private String getUserNameByAccountId(AuthenticatedUser currentUser, int accountId) {
        String userName = null;
        int userId = 0;
        try {
            ResponseEntity<Account[]> responseAccount = restTemplate.exchange(baseUrl + "user/all/account", HttpMethod.GET, makeAuthEntity(currentUser), Account[].class);
            Account[] accounts = responseAccount.getBody();
            for (Account account : accounts) {
                if (account.getAccount_id() == accountId) {
                    userId = account.getUser_id();
                }
            }
            ResponseEntity<User> responseUser = restTemplate.exchange(baseUrl + "user/userId/{id}", HttpMethod.GET, makeAuthEntity(currentUser), User.class, userId);
            User user = responseUser.getBody();
            userName = user.getUsername();
        } catch (RestClientException e) {
            System.out.println("Error retrieving pending Account/User record: " + e.getMessage());
        } catch (NullPointerException e) {
            System.out.println("No transfer record found!");
        }
        return userName;
    }

    private HttpEntity<Transfer> makeTransferEntity(Transfer transfer, AuthenticatedUser currentUser) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(currentUser.getToken());
        return new HttpEntity<>(transfer, headers);
    }

    private HttpEntity<Void> makeAuthEntity(AuthenticatedUser currentUser) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(currentUser.getToken());
        return new HttpEntity<>(headers);
    }

}
