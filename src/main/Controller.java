package main;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import org.apache.commons.lang.SystemUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    public Label bottomLabel;
    public Label expertsPanel;
    public Label indicatorsPanel;
    public Label scriptsPanel;
    public Label libraryPanel;
    public Label includePanel;
    public ListView<CheckBox> folderList = new ListView<>();
    public TextField folderEntryField;
    public GridPane grid;

    public String DUMMYREMOVEME;

    // Lists of the folders that we will copy to, along with corresponding checkboxes,
    private ArrayList<CheckBox> folderCheckBoxes;
    private ArrayList<File> terminalFolderList;
    // Filename to save our little bit of data
    private final String SAVE_FILENAME = "mqldistrib.save";

    /**
     * Method called when the stage is opened
     *
     * @param location
     * @param resources
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupDragAndDropListeners();
        getSavedPath();
        checkFolderLocationIsOk();
        setupFolderList();
    }


    /**
     * React to user entry in the folder location.
     * @param actionEvent
     */
    public void folderLocationEntryAction(ActionEvent actionEvent) {
        checkFolderLocationIsOk();
        setupFolderList();
    }

    /**
     * Checks to see whether we have a path saved already!
     */
    private void getSavedPath() {
        Path saveFilePath = Paths.get(SAVE_FILENAME);

        if (Files.exists(saveFilePath)) {
            try {
                // Get all lines in the file, even though we're just using the first.
                List<String> fileLinesList = Files.readAllLines(saveFilePath);
                folderEntryField.setText(fileLinesList.get(0));
            } catch (Exception e) {
                // If it's gone wrong, set the entry field to blank, and show an error
                folderEntryField.setText("");
                bottomLabel.setText("Error reading save file");
                return;
            }
        } else {
            // Create the save file if it doesn't already exist.
            try {
                Files.createFile(Paths.get(SAVE_FILENAME));
                bottomLabel.setText("Created new data save file.");
            } catch (IOException e) {
                e.printStackTrace();
                bottomLabel.setText("File write error.");
            }
        }
    }

    /**
     * Sets up the areas on the form to listen for a drag-and-drop event
     * Each panel needs a setDragOver and setOnDragDropped event.
     */
    private void setupDragAndDropListeners() {
        // What to do with the Experts Panel...

        expertsPanel.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        expertsPanel.setOnDragDropped(event -> {
            System.out.println("Drag and drop.");
            Dragboard db = event.getDragboard();
            List<File> droppedFiles = db.getFiles();
            for (File file : droppedFiles) {
                try {
                    doTransfer(file, "Experts");
                } catch (IOException e) {
                    e.printStackTrace();
                    bottomLabel.setText("Error copying file.");
                }
            }
            event.setDropCompleted(true);
            event.consume();
        });

        indicatorsPanel.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        indicatorsPanel.setOnDragDropped(event -> {
            System.out.println("Drag and drop.");
            Dragboard db = event.getDragboard();
            List<File> droppedFiles = db.getFiles();
            for (File file : droppedFiles) {
                try {
                    doTransfer(file, "Indicators");
                } catch (IOException e) {
                    e.printStackTrace();
                    bottomLabel.setText("Error copying file.");
                }
            }
            event.setDropCompleted(true);
            event.consume();
        });

        scriptsPanel.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        scriptsPanel.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            List<File> droppedFiles = db.getFiles();
            for (File file : droppedFiles) {
                try {
                    doTransfer(file, "Scripts");
                } catch (IOException e) {
                    e.printStackTrace();
                    bottomLabel.setText("Error copying file.");
                }
            }
            event.setDropCompleted(true);
            event.consume();
        });

        libraryPanel.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        libraryPanel.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            List<File> droppedFiles = db.getFiles();
            for (File file : droppedFiles) {
                try {
                    doTransfer(file, "Libraries");
                } catch (IOException e) {
                    e.printStackTrace();
                    bottomLabel.setText("Error copying file.");
                }
            }
            event.setDropCompleted(true);
            event.consume();
        });

        includePanel.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        includePanel.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            List<File> droppedFiles = db.getFiles();
            for (File file : droppedFiles) {
                try {
                    doTransfer(file, "Include");
                } catch (IOException e) {
                    e.printStackTrace();
                    bottomLabel.setText("Error copying file.");
                }
            }
            event.setDropCompleted(true);
            event.consume();
        });

    }

    /**
     * Method does heavy lifting of actually copying files.
     *
     * @param file
     * @param dropTargetName
     */
    private void doTransfer(File file, String dropTargetName) throws IOException {
        // First check we have specified something as a terminal folder
        Path sourceFilePath = file.toPath();
        if (terminalFolderList == null) {
            bottomLabel.setText("Check folder location!");
            return;
        }
        // Let's keep track of whether the copy operation has been successful
        boolean fileOperationsOK = true;
        // Cycle through our list of terminal folders
        for (int i = 0; i < terminalFolderList.size(); i++) {
            // Check to see whether the corresponding checkbox is selected first:
            if (!folderCheckBoxes.get(i).isSelected()) continue;
            // We need to handle the paths differently for Linux and Windows.:
            String destinationStr;
            bottomLabel.setText("Constructing path.");
            if (SystemUtils.IS_OS_WINDOWS) {
                // Back-slash for Win.
                destinationStr = terminalFolderList.get(i).toString() + "\\" + dropTargetName + "\\" + file.getName();
            } else {
                // Fwd slash for Lin.
                destinationStr = terminalFolderList.get(i).toString() + "/" + dropTargetName + "/" + file.getName();
            }
            bottomLabel.setText("Path: " + destinationStr);
            Path destPath = Paths.get(destinationStr);
            try {
                Files.copy(sourceFilePath, destPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                // The copy operation has failed.
                e.printStackTrace();
                bottomLabel.setText("Error copying file.");
                System.out.println("Error copying " + sourceFilePath.toString() + " to " + destPath.toString());
                fileOperationsOK = false;
            }
        }
        // If it's all worked OK, we can put a friendly message in our notification area.
        if (fileOperationsOK)
            bottomLabel.setText("File copy OK.");
        else
            bottomLabel.setText("File copy error.");
    }

    /**
     * Take a look at the entered folder, see if it makes sense!
     */
    private ArrayList<File> checkFolderLocationIsOk() {
        Path path;
        try {
            // See if the entered path is valid.
            path = Paths.get(folderEntryField.getText());
        } catch (Exception e) {
            // If not, show an error message.
            bottomLabel.setText("Enter valid folder location.");
            //terminalFolderList = new ArrayList<>();
            terminalFolderList = new ArrayList<>();
            return null;
        }
        if (Files.exists(path)) {
            // Check to see whether the folder really is a terminal folder.
            //System.out.println("Folder exists!");
            ArrayList<File> folderList = checkForMT4subfolders(path);
            if (folderList != null) {
                bottomLabel.setText("Folder location OK.");
                return folderList;
            } else {
                // If not, here's another error message.
                bottomLabel.setText("Check folder location.");
                terminalFolderList = new ArrayList<>();
                return null;
            }
        } else {
            //System.out.println("Folder does not exist!");
            bottomLabel.setText("Check folder location.");
            terminalFolderList = new ArrayList<>();
            return null;
        }
    }

    /**
     * Shows a list of checkboxes in the listview for user selection as copy targets
     */
    private void setupFolderList() {
        // First, clear the existing list
        folderList.getItems().clear();
        folderCheckBoxes = new ArrayList<>();
        // Cycle through each input folder, display and generate a checkbox.
        for (int i = 0; i < terminalFolderList.size(); i++) {
            // Use a helper method to get a readable display string
            String displayString = getListDisplayString(terminalFolderList.get(i));
            // Add a new checkbox to our list, and display in the List view
            CheckBox iCheckBox = new CheckBox(displayString);
            iCheckBox.setSelected(true);
            iCheckBox.setFont(Font.font(12));
            folderCheckBoxes.add(iCheckBox);
            folderList.getItems().add(iCheckBox);
        }
    }

    /**
     * Returns a readable version of the file path
     *
     * @param iFile Path to truncate to a more readable form
     */
    private String getListDisplayString(File iFile) {
        int strLength = iFile.getPath().length();
        // Trim the last five chars, ie \MQL4
        String returnStr = iFile.getPath().substring(0, strLength - 5);
        // Need to separate the last relevant folder. First, figure out which OS, are we using / or \ ?
        String folderSeparator;
        if (SystemUtils.IS_OS_WINDOWS) {
            folderSeparator = "\\\\";
        } else {
            folderSeparator = "/";
        }
        String[] indivFolders = returnStr.split(folderSeparator);
        returnStr = indivFolders[indivFolders.length - 1];
        return returnStr;
    }

    /**
     * Check to see whether there are valid MT4 folders in the one specfied by the user.
     * Returns a list of paths if so.
     */
    private ArrayList<File> checkForMT4subfolders(Path specifiedPath) {
        // We can return a list of paths to return that are valid destinations for our files
        ArrayList<File> folderList = new ArrayList<>();
        File[] list = new File(specifiedPath.toString()).listFiles();
        // Return a null list if there are no folders here.
        if (list == null) return null;
        // Go through each item in the specified folder
        // We start with the assumption that the folder is not the right one
        for (File iFile : list
                ) {
            if (iFile.isDirectory()) {
                String terminalPath = iFile.toString();
                File[] fileList2 = new File(terminalPath).listFiles();
                for (File jFile : fileList2
                        ) {
                    // See if this is an MQL4 folder...
                    if (jFile.toString().substring(jFile.toString().length() - 4, jFile.toString().length()).equals("MQL4")) {
                        folderList.add(jFile);
                    }
                }
            }
        }
        // If we have found some folders, we can return them. Otherwise null.
        if (folderList.size() > 0) {
            terminalFolderList = folderList;
            // As the folder is valid, we can save it to file.
            saveFolderLocation();
            return folderList;
        } else {
            terminalFolderList = null;
            return null;
        }
    }

    /**
     * Save the terminal folder path to a file for use next time the app is used.
     */
    private void saveFolderLocation() {
        try {
            FileWriter saveFileWriter = new FileWriter(SAVE_FILENAME);
            saveFileWriter.write(folderEntryField.getText());
            saveFileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
            bottomLabel.setText("Error writing to save file.");
        }
    }

}
