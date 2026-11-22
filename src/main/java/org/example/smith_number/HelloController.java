package org.example.smith_number;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class HelloController {
    @FXML private TextField upperLimitField;
    @FXML private TextField threadCountField;
    @FXML private Button startButton;
    @FXML private Button stopButton;
    @FXML private Label errorLabel;
    @FXML private Label statusLabel;
    @FXML private FlowPane workerPane;
    @FXML private TextArea resultsArea;
    @FXML private Label smithCountLabel;
    @FXML private Label nonSmithCountLabel;
    @FXML private Label totalCountLabel;

    private final List<SearchTask> workers = new ArrayList<>();
    private int finished;

    @FXML
    private void initialize() {
        upperLimitField.setText("10000");
        threadCountField.setText("4");
        statusLabel.setText("Ready.");
        clearResults();
        setRunning(false);
    }

    @FXML
    private void startSearch() {
        if (!workers.isEmpty()) {
            return;
        }
        int upperLimit = positiveInteger(upperLimitField.getText());
        int threadCount = positiveInteger(threadCountField.getText());
        if (upperLimit == 0) {
            showError("Upper limit must be a positive 32-bit integer.", upperLimitField);
            return;
        }
        if (threadCount == 0) {
            showError("Thread count must be a positive 32-bit integer.", threadCountField);
            return;
        }
        int maximumThreads = Math.min(upperLimit, SmithSearch.MAX_THREADS);
        if (threadCount > maximumThreads) {
            showError("Thread count must be between 1 and " + maximumThreads + ".", threadCountField);
            return;
        }

        errorLabel.setVisible(false);
        errorLabel.setText("");
        workerPane.getChildren().clear();
        clearResults();
        finished = 0;
        setRunning(true);
        for (SmithSearch.Range range : SmithSearch.ranges(upperLimit, threadCount)) {
            SearchTask task = new SearchTask(range);
            workers.add(task);
            workerPane.getChildren().add(workerCard(workers.size(), task));
            task.setOnSucceeded(event -> workerFinished(task, upperLimit));
            task.setOnFailed(event -> {
                if (workers.contains(task)) {
                    cancelWorkers();
                    clearResults();
                    statusLabel.setText("Search failed.");
                    setRunning(false);
                }
            });
        }
        updateRunningStatus();
        for (int i = 0; i < workers.size(); i++) {
            Thread thread = new Thread(workers.get(i), "Smith worker " + (i + 1));
            thread.setDaemon(true);
            thread.start();
        }
    }

    private static int positiveInteger(String text) {
        try {
            return Math.max(0, Integer.parseInt(text.trim()));
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    private void showError(String message, TextField field) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        field.requestFocus();
    }

    private VBox workerCard(int number, SearchTask task) {
        SmithSearch.Range range = task.range;
        Label heading = new Label("Thread " + number);
        ProgressIndicator progress = new ProgressIndicator(0);
        progress.setPrefSize(42, 42);
        progress.setMaxSize(42, 42);
        progress.progressProperty().bind(task.progressProperty());
        Label interval = new Label(range.first() + " to " + range.last());
        interval.setMaxWidth(Double.MAX_VALUE);
        Label count = new Label(range.size() + (range.size() == 1 ? " number" : " numbers"));
        Label state = new Label("Ready");
        task.stateProperty().addListener((observable, previous, current) -> state.setText(switch (current) {
            case READY, SCHEDULED -> "Ready";
            case RUNNING -> "Running";
            case SUCCEEDED -> "Done";
            case CANCELLED -> "Cancelled";
            case FAILED -> "Failed";
        }));

        VBox card = new VBox(6, heading, progress, interval, count, state);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(150);
        return card;
    }

    private void workerFinished(SearchTask task, int upperLimit) {
        if (!workers.contains(task)) {
            return;
        }
        finished++;
        if (finished < workers.size()) {
            updateRunningStatus();
            return;
        }

        int smithCount = 0;
        StringJoiner matches = new StringJoiner(", ");
        for (SearchTask worker : workers) {
            Result result = worker.getValue();
            smithCount += result.count();
            if (result.count() > 0) {
                matches.add(result.matches());
            }
        }
        resultsArea.setText(matches.toString());
        showCounts(smithCount, upperLimit);
        statusLabel.setText("Complete. Checked all " + upperLimit + " numbers from 1 to " + upperLimit
                + ". Finished threads: " + finished + " of " + workers.size() + ".");
        workers.clear();
        setRunning(false);
    }

    private void updateRunningStatus() {
        statusLabel.setText("Running. Finished threads: " + finished + " of " + workers.size() + ".");
    }

    @FXML
    private void stopSearch() {
        if (workers.isEmpty()) {
            return;
        }
        int threadCount = workers.size();
        cancelWorkers();
        clearResults();
        statusLabel.setText("Stopped. Finished threads: " + finished + " of " + threadCount + ".");
        setRunning(false);
    }

    void close() {
        cancelWorkers();
    }

    private void cancelWorkers() {
        for (SearchTask worker : workers) {
            worker.cancel(true);
        }
        workers.clear();
    }

    private void setRunning(boolean running) {
        upperLimitField.setDisable(running);
        threadCountField.setDisable(running);
        startButton.setDisable(running);
        stopButton.setDisable(!running);
    }

    private void clearResults() {
        resultsArea.clear();
        showCounts(0, 0);
    }

    private void showCounts(int smithCount, int total) {
        smithCountLabel.setText("Smith numbers: " + smithCount);
        nonSmithCountLabel.setText("Non-Smith numbers: " + (total - smithCount));
        totalCountLabel.setText("Total checked: " + total);
    }

    private record Result(String matches, int count) {}

    private static final class SearchTask extends Task<Result> {
        private final SmithSearch.Range range;

        SearchTask(SmithSearch.Range range) {
            this.range = range;
            updateProgress(0, range.size());
        }

        @Override
        protected Result call() {
            StringBuilder matches = new StringBuilder();
            int count = 0;
            int size = range.size();
            long lastUpdate = System.nanoTime();
            for (int offset = 0; offset < size; offset++) {
                if (isCancelled()) {
                    return null;
                }
                int number = range.first() + offset;
                if (SmithSearch.isSmith(number)) {
                    if (count > 0) {
                        matches.append(", ");
                    }
                    matches.append(number);
                    count++;
                }
                int checked = offset + 1;
                if (checked % 64 == 0) {
                    long now = System.nanoTime();
                    if (now - lastUpdate >= 50_000_000L) {
                        updateProgress(checked, size);
                        lastUpdate = now;
                    }
                }
            }
            updateProgress(size, size);
            return new Result(matches.toString(), count);
        }
    }
}
