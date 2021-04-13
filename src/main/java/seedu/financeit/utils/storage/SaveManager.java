package seedu.financeit.utils.storage;

import seedu.financeit.Financeit;
import seedu.financeit.common.CommandPacket;
import seedu.financeit.datatrackers.entrytracker.EntryTracker;
import seedu.financeit.datatrackers.manualtracker.ManualTracker;
import seedu.financeit.datatrackers.recurringtracker.RecurringTracker;
import seedu.financeit.parser.InputParser;
import seedu.financeit.ui.TablePrinter;
import seedu.financeit.ui.UiManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SaveManager {
    public static final String fullPath = "./data/backup/names.txt";
    public static final String dirPath = "./data/backup";
    private static String prompt = "";
    private static boolean menu = true;

    public static void main() {
        while (true) {
            if (menu == true) {
                helpMenu();
            }
            InputParser parser = InputParser.getInstance();
            CommandPacket packet = parser.parseInput(UiManager.handleInput());
            menu = true;
            switch (packet.getCommandString()) {
            case "list":
                listSaves(packet);
                break;
            case "add":
                if (checkValidity(packet) == true) {
                    addSave(packet);
                }
                break;
            case "load":
                if (checkValidity(packet) == true) {
                    loadSave(packet);
                }
                break;
            case "reset":
                resetSave();
                break;
            case "delete":
                if (checkValidity(packet) == true) {
                    deleteSave(packet);
                }
                break;
            case "help":
                menu = true;
                break;
            case "exit":
                return;
            default:
                prompt = "Invalid Command";
            }
        }
    }

    /**
     * This method checks if the entered command is valid and adds the
     * appropriate prompt to the status bar.
     * @param packet packet containing parsed command from user
     * @return If true, run the command, if false, report status to the user and don't run the command.
     */
    private static boolean checkValidity(CommandPacket packet) {
        if (packet.getParam("/name") == null) {
            prompt = "Invalid Command";
            return false;
        }

        if (packet.getParam("/name").trim().equals("")) {
            prompt = "Name cannot be empty!";
            return false;
        }

        Pattern p = Pattern.compile("[^A-Za-z0-9]");
        Matcher m = p.matcher(packet.getParam("/name"));
        if (m.find()) {
            prompt = "No spaces or special characters allowed! Alphanumeric only!";
            return false;
        }

        return true;
    }

    private static void helpMenu() {
        UiManager.refreshPage();
        status();
        TablePrinter.setTitle("Welcome to Save Manager");
        TablePrinter.addRow("No.; Commands                                           ; Syntax                    ");
        TablePrinter.addRow("[1]; List local saves; list");
        TablePrinter.addRow("[2]; Add/Overwrite save; add /name");
        TablePrinter.addRow("[3]; Load save; load /name");
        TablePrinter.addRow("[4]; Delete save; delete /name");
        TablePrinter.addRow("[5]; Reset program; reset");
        TablePrinter.addRow("[6]; Quit to main; exit");
        TablePrinter.printList();
    }

    private static void status() {
        System.out.println("Status: " + prompt);
        prompt = "";
    }

    private static void helpMsg() {
        System.out.println("Enter <help> for a list of commands");
    }

    private static void resetSave() {
        GoalTrackerSaver.clear();
        RecurringTrackerSaver.clear();
        ManualTrackerSaver.clear();
        prompt = "Program has been reset";
    }

    public static void resetAllLists() {
        System.out.println(ManualTracker.getLedgerList());
        ManualTracker.getLedgerList().removeAllItems();
        EntryTracker.getEntryList().removeAllItems();
        RecurringTracker.getEntries().removeAllItems();
    }

    /**
     * This method prints a list of backup saves using the names provided
     * in text file names.txt
     * @param packet packet containing parsed command from user
     */
    private static void listSaves(CommandPacket packet) {
        try {
            menu = false;
            UiManager.refreshPage();
            helpMsg();
            TablePrinter.setTitle("List Saves");
            TablePrinter.addRow("No.; Names                                           ");
            SaveHandler.buildFile(dirPath, fullPath);
            File file = new File(fullPath);
            Scanner scanner = new Scanner(file);
            for (int i = 1; scanner.hasNext(); ) {
                String name = scanner.nextLine();
                if (!name.equals("")) {
                    TablePrinter.addRow("[" + i + "]; " + name);
                    i++;
                }
            }
            TablePrinter.printList();
        } catch (Exception e) {
            menu = true;
            prompt = e.toString();
        }
    }

    /**
     * This method creates a backup save in data/backup directory. This save consists of
     * 3 text files ending with _gt, _mt, _at and an entry in the names.txt file. If entered
     * name already exist, then the function overwrites the existing save, otherwise, new
     * save will be created.
     * @param packet packet containing parsed command from user
     */
    public static void addSave(CommandPacket packet) {
        try {
            String name = packet.getParam("/name");
            String path = dirPath + "/" + name;
            GoalTrackerSaver.getInstance().save(dirPath, path + "_gt.txt");
            ManualTrackerSaver.getInstance().save(dirPath, path + "_mt.txt");
            RecurringTrackerSaver.getInstance().save(dirPath, path + "_at.txt");
            SaveHandler.buildFile(dirPath, fullPath);
            File file = new File(fullPath);
            Scanner scanner = new Scanner(file);
            while (scanner.hasNext()) {
                String saveString = scanner.nextLine();
                if (saveString.equals(name)) {
                    prompt = name + " has been overwritten!";
                    return;
                }
            }
            FileWriter fileWriter = new FileWriter(fullPath, true);
            fileWriter.write(name + "\n");
            fileWriter.close();
            prompt = name + " has been added!";

        } catch (Exception e) {
            prompt = e.toString();
        }
    }

    /**
     * This method searches for a matching name entry in names.txt. Once found, it will
     * use this name to access contents of 3 text files containing saved data for Goal
     * Tracker, Manual Tracker and Auto Tracker respectively. The 3 text files in ./data/backup
     * directory is subsequently copied to the main save found in ./data directory where it
     * is then loaded using the load method.
     * @param packet packet containing parsed command from user
     */
    public static void loadSave(CommandPacket packet) {
        try {
            String name = packet.getParam("/name");
            String path = dirPath + "/" + name;
            String desAuto = RecurringTrackerSaver.getInstance().fullPath;
            String desGoal = GoalTrackerSaver.getInstance().fullPath;
            String desManual = ManualTrackerSaver.getInstance().fullPath;
            RecurringTrackerSaver.getInstance().buildFile();
            GoalTrackerSaver.getInstance().buildFile();
            ManualTrackerSaver.getInstance().buildFile();
            File file = new File(fullPath);
            Scanner scanner = new Scanner(file);
            while (scanner.hasNext()) {
                String saveString = scanner.nextLine();
                if (saveString.equals(name)) {
                    FileInputStream inGt = new FileInputStream(path + "_gt.txt");
                    FileOutputStream outGt = new FileOutputStream(desGoal);
                    FileChannel sourceChannel = inGt.getChannel();
                    FileChannel destChannel = outGt.getChannel();
                    destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());

                    FileInputStream inMt = new FileInputStream(path + "_mt.txt");
                    FileOutputStream outMt = new FileOutputStream(desManual);
                    sourceChannel = inMt.getChannel();
                    destChannel = outMt.getChannel();
                    destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());

                    FileInputStream inAt = new FileInputStream(path + "_at.txt");
                    FileOutputStream outAt = new FileOutputStream(desAuto);
                    sourceChannel = inAt.getChannel();
                    destChannel = outAt.getChannel();
                    destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());

                    inGt.close();
                    inMt.close();
                    inAt.close();
                    outMt.close();
                    outGt.close();
                    outAt.close();
                    clear();
                    Financeit.load();
                    prompt = name + " has been loaded!";
                    return;
                }
            }
            prompt = name + " cannot be found!";
        } catch (Exception e) {
            prompt = e.toString();
        }
    }

    public static void clear() {
        GoalTrackerSaver.clear();
        RecurringTrackerSaver.clear();
        ManualTrackerSaver.clear();
    }

    /**
     * This method deletes the given entry in names.txt as well as the save files
     * associated with that name.
     * @param packet packet containing parsed command from user
     */
    public static void deleteSave(CommandPacket packet) {
        try {
            String name = packet.getParam("/name");
            StringBuilder nameList = new StringBuilder();
            File file = new File(fullPath);
            Scanner scanner = new Scanner(file);
            prompt = name + " is not found!";
            while (scanner.hasNext()) {
                String saveString = scanner.nextLine();
                if (!saveString.equals(name)) {
                    nameList.append(saveString + "\n");
                } else {
                    String path = dirPath + "/" + name;
                    Files.deleteIfExists(Paths.get(path + "_gt.txt"));
                    Files.deleteIfExists(Paths.get(path + "_mt.txt"));
                    Files.deleteIfExists(Paths.get(path + "_at.txt"));
                    prompt = name + " has been removed!";
                }
            }
            FileWriter fileWriter = new FileWriter(fullPath, false);
            fileWriter.write(nameList.toString());
            fileWriter.close();

        } catch (Exception e) {
            prompt = e.toString();
        }
    }
}
