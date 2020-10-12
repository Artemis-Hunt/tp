package seedu.financeit.manualtracker;

import seedu.financeit.common.CommandPacket;
import seedu.financeit.common.Constants;
import seedu.financeit.common.exceptions.InsufficientParamsException;
import seedu.financeit.common.exceptions.ItemNotFoundException;
import seedu.financeit.manualtracker.subroutine.EntryTracker;
import seedu.financeit.parser.InputParser;
import seedu.financeit.ui.TablePrinter;
import seedu.financeit.ui.UiManager;
import seedu.financeit.utils.FiniteStateMachine;

/**
 * Class to handle routine for manual ledger management.
 */
public class ManualTracker {
    private static LedgerList ledgerList = new LedgerList();
    private static CommandPacket packet;

    public static void main() {
        boolean endTracker = false;
        FiniteStateMachine fsm = new FiniteStateMachine(FiniteStateMachine.State.MAIN_MENU);
        UiManager.printWithStatusIcon(Constants.PrintType.SYS_MSG, "Welcome to Manual Tracker!");
        while (!endTracker) {
            switch (fsm.getCurrState()) {
            case MAIN_MENU:
                fsm.setNextState(handleMainMenu());
                break;
            case CREATE_LEDGER:
                fsm.setNextState(handleCreateLedger());
                break;
            case OPEN_LEDGER:
                fsm.setNextState(handleOpenLedger());
                break;
            case DELETE_LEDGER:
                fsm.setNextState(handleDeleteLedger());
                break;
            case SHOW_LEDGER:
                fsm.setNextState(handleShowLedger());
                break;
            case INVALID_STATE:
                fsm.setNextState(handleInvalidState());
                break;
            case EXIT:
                UiManager.printWithStatusIcon(Constants.PrintType.SYS_MSG, "Exiting from ManualTracker");
                endTracker = true;
                break;
            case END_TRACKER:
                endTracker = true;
                break;
            default:
                break;
            }
            fsm.transitionState();
        }
    }


    public static LedgerList getLedgerList() {
        return ledgerList;
    }

    public static void setCommandPacket(CommandPacket p) {
        packet = p;
    }

    public static void createLedger() {
        handleCreateLedger();
    }

    private static FiniteStateMachine.State handleMainMenu() {
        UiManager.printSpace();
        UiManager.printWithStatusIcon(Constants.PrintType.DIRECTORY, "[ MAIN_MENU -> MANUAL_TRACKER_MENU ]");
        UiManager.printInputPromptMessage();

        String input = UiManager.handleInput();
        packet = new InputParser().parseInput(input);
        UiManager.refreshPage();
        switch (packet.getCommandString()) {
        case "ledger open":
            // Fall through
        case "open":
            return FiniteStateMachine.State.OPEN_LEDGER;
        case "ledger new":
            // Fall through
        case "new":
            return FiniteStateMachine.State.CREATE_LEDGER;
        case "ledger list":
            // Fall through
        case "list":
            return FiniteStateMachine.State.SHOW_LEDGER;
        case "ledger delete":
            // Fall through
        case "delete":
            return FiniteStateMachine.State.DELETE_LEDGER;
        case "exit":
            return FiniteStateMachine.State.EXIT;
        case "commands":
            printCommandList();
            return FiniteStateMachine.State.MAIN_MENU;
        default:
            UiManager.printWithStatusIcon(Constants.PrintType.ERROR_MESSAGE, "Command failed. Try again.");
            return FiniteStateMachine.State.MAIN_MENU;
        }
    }

    private static FiniteStateMachine.State handleCreateLedger() {
        FiniteStateMachine.State state = FiniteStateMachine.State.MAIN_MENU;
        Ledger ledger = new Ledger();
        ledger.setRequiredParams(
            "/date"
        );
        try {
            ledger.handlePacket(packet);
            ledgerList.addItem(ledger);
            UiManager.printWithStatusIcon(Constants.PrintType.SYS_MSG,
                String.format("%s created!", ledger.getName()));
        } catch (InsufficientParamsException exception) {
            UiManager.printWithStatusIcon(Constants.PrintType.ERROR_MESSAGE,
                    exception.getMessage());
        } finally {
            if (!ledger.getHasParsedAllRequiredParams()) {
                UiManager.printWithStatusIcon(Constants.PrintType.ERROR_MESSAGE,
                    "Input failed due to param error.");
            }
        }
        return state;
    }

    private static FiniteStateMachine.State handleDeleteLedger() {
        FiniteStateMachine.State state = FiniteStateMachine.State.MAIN_MENU;
        Ledger ledger;
        ledgerList.setRequiredParams(
            "/id or /date"
        );
        try {
            ledgerList.setCurrItemFromPacket(packet);
            ledger = (Ledger) ledgerList.getCurrItem();
            System.out.println(ledger.getName());
            ledgerList.removeItem(ledger);
            UiManager.printWithStatusIcon(Constants.PrintType.SYS_MSG,
                String.format("%s deleted!", ledger.getName()));
        } catch (InsufficientParamsException | ItemNotFoundException exception) {
            UiManager.printWithStatusIcon(Constants.PrintType.ERROR_MESSAGE,
                exception.getMessage());
        } finally {
            if (!ledgerList.getHasParsedAllRequiredParams()) {
                UiManager.printWithStatusIcon(Constants.PrintType.ERROR_MESSAGE,
                    "Input failed due to param error.");
            }
        }
        return state;
    }

    private static FiniteStateMachine.State handleShowLedger() {
        ledgerList.printList();
        return FiniteStateMachine.State.MAIN_MENU;
    }

    private static FiniteStateMachine.State handleInvalidState() {
        System.out.println("You dun goof bro uwu");
        return FiniteStateMachine.State.MAIN_MENU;
    }

    private static FiniteStateMachine.State handleOpenLedger() {
        FiniteStateMachine.State state = FiniteStateMachine.State.MAIN_MENU;
        Ledger ledger;
        ledgerList.setRequiredParams(
            "/id or /date"
        );
        try {
            ledgerList.setCurrItemFromPacket(packet);
            ledger = (Ledger) ledgerList.getCurrItem();
            EntryTracker.setCurrLedger(ledger);
            UiManager.printWithStatusIcon(Constants.PrintType.SYS_MSG,
                String.format("%s opened!", ledger.getName()));
        } catch (InsufficientParamsException exception) {
            UiManager.printWithStatusIcon(Constants.PrintType.ERROR_MESSAGE,
                exception.getMessage());
            return state;
        } catch (ItemNotFoundException exception) {
            UiManager.printWithStatusIcon(Constants.PrintType.ERROR_MESSAGE,
                exception.getMessage());
            UiManager.printWithStatusIcon(Constants.PrintType.SYS_MSG,
                "Generating new ledger...");
            handleCreateLedger();
            return handleOpenLedger();
        } finally {
            if (!ledgerList.getHasParsedAllRequiredParams()) {
                UiManager.printWithStatusIcon(Constants.PrintType.ERROR_MESSAGE,
                    "Input failed due to param error.");
            }
        }
        ledgerList.popCurrItem();
        return EntryTracker.main();
    }

    private static void printCommandList() {
        TablePrinter.setTitle("List of Commands");
        TablePrinter.addRow("No.;Command            ;Input Format                  ");
        TablePrinter.addRow("1.;Open ledger;ledger open /date <YYMMDD>");
        TablePrinter.addRow("2.;New ledger;ledger new /date <YYMMDD>");
        TablePrinter.addRow("3.;list ledgers;ledger list");
        TablePrinter.addRow("4.;delete ledgers;ledger delete /date <YYMMDD>");
        TablePrinter.addRow("5.;exit to main menu;exit");
        TablePrinter.printList();
    }
}