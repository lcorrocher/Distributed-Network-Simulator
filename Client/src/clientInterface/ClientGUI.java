package clientInterface;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import javafx.application.Platform;

import java.nio.file.WatchService;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;

import java.io.*;
import java.net.Socket;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * ClientGUI class to create a GUI for the client using JavaFX, which connects to the server via sockets on port localhost:8080.
 * The GUI also allows the user to select the source, destination, and message to send.
 * Buttons are created to send messages across the network, wipe the network, and view logs.
 */
public class ClientGUI extends Application {

    private ComboBox<String> srcBox;
    private ComboBox<String> dstBox;
    private ComboBox<String> msgBox;
    private Button viewLogsButton;
    private Button clearLogButton;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Client");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20, 20, 20, 20));
        grid.setVgap(10);
        grid.setHgap(10);

        ObservableList<String> letters = FXCollections.observableArrayList(
                IntStream.rangeClosed('a', 'p').mapToObj(i -> "" + (char) i).collect(Collectors.toList())
        );

        Label srcLabel = new Label("Source:");
        GridPane.setConstraints(srcLabel, 0, 0);
        srcBox = new ComboBox<>(letters);
        GridPane.setConstraints(srcBox, 1, 0);

        Label dstLabel = new Label("Destination:");
        GridPane.setConstraints(dstLabel, 0, 1);
        dstBox = new ComboBox<>(letters);
        GridPane.setConstraints(dstBox, 1, 1);

        Label msgLabel = new Label("Message:");
        GridPane.setConstraints(msgLabel, 0, 2);
        File folder = new File("server/io/messages");
        File[] listOfFiles = folder.listFiles();
        ObservableList<String> messages = FXCollections.observableArrayList();
        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                if (file.isFile()) {
                    messages.add(file.getName());
                }
            }
        }
        msgBox = new ComboBox<>(messages);
        GridPane.setConstraints(msgBox, 1, 2);

        Button startStopServerButton = new Button("Start Server");
        GridPane.setConstraints(startStopServerButton, 0, 4);
        startStopServerButton.setDisable(true);

        Button sendButton = new Button("Send message across network");
        GridPane.setConstraints(sendButton, 0, 3);
        sendButton.setOnAction(e -> handleNormalMessage());
        sendButton.setDisable(false);

        Button wipeButton = new Button("Wipe");
        GridPane.setConstraints(wipeButton, 1, 3);
        wipeButton.setDisable(true);
        wipeButton.setOnAction(e -> handleWipeCommand());

        Button terminateButton = new Button("Terminate Client");
        GridPane.setConstraints(terminateButton, 0, 5);
        terminateButton.setOnAction(e -> System.exit(0));

        viewLogsButton = new Button("View Logs");
        GridPane.setConstraints(viewLogsButton, 1, 4);
        viewLogsButton.setDisable(false);
        viewLogsButton.setOnAction(e -> viewLogs());

        clearLogButton = new Button("Clear Log");
        GridPane.setConstraints(clearLogButton, 1, 5);
        clearLogButton.setOnAction(e -> handleClearLog());

        grid.getChildren().addAll(srcLabel, srcBox, dstLabel, dstBox, msgLabel, msgBox, sendButton, wipeButton, terminateButton, viewLogsButton, clearLogButton, startStopServerButton);

        //TODO: create action which starts sever and enables send button. It had a port collision the last time#

        Scene scene = new Scene(grid, 400, 255);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void handleWipeCommand() {
        String hostName = "localhost";
        int portNumber = 8080;

        try {
            Socket socket = new Socket(hostName, portNumber);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            out.println("wipe");

            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleNormalMessage() {
        if (srcBox.getValue() == null || dstBox.getValue() == null || msgBox.getValue() == null) {
            new Alert(Alert.AlertType.ERROR, "Please ensure values for source, destination, and message before sending").showAndWait();
            return;
        }

        String hostName = "localhost";
        int portNumber = 8080;

        try {
            Socket socket = new Socket(hostName, portNumber);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            out.println(srcBox.getValue());
            out.println(dstBox.getValue());
            out.println(msgBox.getValue());

            out.close();
            socket.close();

            viewLogsButton.setDisable(false);
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Please ensure the server is running before sending a message").showAndWait();
        }
    }

    private void viewLogs() {
        Stage logStage = new Stage();
        logStage.setTitle("Application Logs");

        VBox vbox = new VBox();
        TextArea textArea = new TextArea();

        VBox.setVgrow(textArea, Priority.ALWAYS);

        // new thread that watches the log file for changes and updates the text area
        new Thread(() -> {
            try {
                WatchService watchService = FileSystems.getDefault().newWatchService();
                Path path = Paths.get("server/application_time_log.txt").getParent();
                path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

                while (true) {
                    WatchKey key = watchService.take();
                    for (WatchEvent<?> event : key.pollEvents()) {
                        if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                            Platform.runLater(() -> {
                                textArea.clear();
                                try (BufferedReader reader = new BufferedReader(new FileReader("server/application_time_log.txt"))) {
                                    String line;
                                    while ((line = reader.readLine()) != null) {
                                        textArea.appendText(line + "\n");
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            });
                        }
                    }
                    key.reset();
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        vbox.getChildren().add(textArea);
        Scene scene = new Scene(vbox, 600, 700);
        logStage.setScene(scene);
        logStage.show();
    }

    private void handleClearLog() {
        String hostName = "localhost";
        int portNumber = 8081;

        try {
            Socket socket = new Socket(hostName, portNumber);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            out.println("clearLog");

            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}