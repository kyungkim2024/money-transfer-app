package com.techelevator.tenmo;

import com.techelevator.tenmo.model.AuthenticatedUser;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.UserCredentials;
import com.techelevator.tenmo.services.AccountService;
import com.techelevator.tenmo.services.AuthenticationService;
import com.techelevator.tenmo.services.ConsoleService;
import com.techelevator.tenmo.services.TransferService;

import java.math.BigDecimal;

public class App {

    private static final String API_BASE_URL = "http://localhost:8080/";

    private final ConsoleService consoleService = new ConsoleService();
    private final AuthenticationService authenticationService = new AuthenticationService(API_BASE_URL);
    private final AccountService accountService = new AccountService(API_BASE_URL);
    private final TransferService transferService = new TransferService(API_BASE_URL);

    private AuthenticatedUser currentUser;

    public static void main(String[] args) {
        App app = new App();
        app.run();
    }

    private void run() {
        consoleService.printGreeting();
        loginMenu();
        if (currentUser != null) {
            mainMenu();
        }
    }

    private void loginMenu() {
        int menuSelection = -1;
        while (menuSelection != 0 && currentUser == null) {
            consoleService.printLoginMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                handleRegister();
            } else if (menuSelection == 2) {
                handleLogin();
            } else if (menuSelection != 0) {
                System.out.println("Invalid Selection");
                consoleService.pause();
            }
        }
    }

    private void handleRegister() {
        System.out.println("Please register a new user account");
        UserCredentials credentials = consoleService.promptForCredentials();
        if (authenticationService.register(credentials)) {
            System.out.println("Registration successful. You can now login.");
        } else {
            consoleService.printErrorMessage();
        }
    }

    private void handleLogin() {
        UserCredentials credentials = consoleService.promptForCredentials();
        currentUser = authenticationService.login(credentials);
        if (currentUser == null) {
            consoleService.printErrorMessage();
        }
    }

    private void mainMenu() {
        int menuSelection = -1;
        while (menuSelection != 0) {
            consoleService.printMainMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                viewCurrentBalance();
//          2. View transfer short version transfer history
//          Then, choose transfer ID to view the transfer details
            } else if (menuSelection == 2) {
                viewTransferHistory();
            } else if (menuSelection == 3) {

                approvalMenu();
            } else if (menuSelection == 4) {
                // Need to get list of accounts to send TE Bucks
                sendBucks();  // Here we can change balances for both accounts
                // In sendBucks(), I implemented POST the 'Send transfer' into transfer DB (transfer_status_id = 2, 'Approved') because it's sending
            } else if (menuSelection == 5) {
                // Need to get list of accounts to request TE Bucks
                requestBucks();
                // In requestBucks(), I implemented POST the 'Send transfer' into transfer DB (transfer_status_id = 1, 'Pending') because it's request
            } else if (menuSelection == 0) {
                continue;
            } else {
                System.out.println("Invalid Selection");
            }
            consoleService.pause();
        }
    }

    private void approvalMenu() {
        int menuSelection = -1;
        while (menuSelection != 0) {
            viewPendingRequests();
            consoleService.printApprovalRejection();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                handleApprovalRequest();
                //sendBucks();
            } else if (menuSelection == 2) {
                handleRejectRequest();
            } else if (menuSelection == 0) {
                continue;
            } else {
                System.out.println("Invalid Selection");
            }
            consoleService.pause();
        }
    }

	private void viewCurrentBalance() {
        System.out.println(accountService.getBalance(currentUser));
	}

	private void viewTransferHistory() {
        transferService.getTransferHistory(currentUser);
	}

	private void viewPendingRequests() { transferService.getPendingRequests(currentUser); }

    private void handleApprovalRequest() {
        // Below will change the transfer from pending to approved, and return true.
        // If true, then will execute sendbucks method to change balances for both accounts
        // TODO : need to implement method to change balances for both accounts
        // Need to get the amount to be transferred and then
        // For requester, requester's current balance + amount to be transferred
        // For sender, sender's current balance - amount to be transferred
        int recipeientTransferId = consoleService.promptForInt("Please input transfer ID: ");
        // For approval :
        // currentUser = money receiver,
        if (transferService.approveRequest(recipeientTransferId, currentUser)) {
            //
            Transfer transfer = transferService.getTransferByTransferId(currentUser, recipeientTransferId);
            // Update balances for both current user(current balance + amount) and the recipient(current balance - amount)

        }
    }

    private void handleRejectRequest() { transferService.rejectRequest(currentUser); }

    // Request : currentUser = money receiver, accountFromId = money sender
    private void requestBucks() {
        int accountFromId = consoleService.promptForInt("Please choose recipient's account ID you are requesting money from: ");
        BigDecimal amount = consoleService.promptForBigDecimal("Please input amount in two decimal: ");
        // POST into Transfer table, need to get account object by user ID
        // transfer_status_id & transfer_type_id = 1 for 'Pending' and 'Request'
        transferService.postTransfer(1, accountFromId, amount, currentUser, accountService.getAccountByUserId(currentUser));
    }

    //TODO
	private void sendBucks() {
        int accountToId = consoleService.promptForInt("Please choose recipient's account ID: ");
        BigDecimal amount = consoleService.promptForBigDecimal("Please input amount in two decimal: ");
        // POST into Transfer table
        // transfer_status_id & transfer_type_id = 2 for 'Approved' and 'Send'
        transferService.postTransfer(2, accountToId, amount, currentUser, accountService.getAccountByUserId(currentUser));
        // Change balances for currentUser and receiver in account DB

	}

}
