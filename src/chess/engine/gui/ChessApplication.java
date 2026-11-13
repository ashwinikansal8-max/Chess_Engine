package chess.engine.gui;

import chess.engine.Alliance;
import chess.engine.ai.MiniMax;
import chess.engine.board.Board;
import chess.engine.board.Move;
import chess.engine.game.GameSession;
import chess.engine.player.MoveStatus;
import chess.engine.player.Player;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ChessApplication extends Application {

    private final MiniMax engine = new MiniMax(3);
    private GameSession game;
    private BoardView boardView;
    private Label statusLabel;
    private Label turnLabel;
    private Label humanLabel;
    private Label computerLabel;
    private ListView<String> historyView;
    private Integer selectedSquare;
    private Move lastMove;
    private Map<Integer, Move> selectedMoves = Map.of();
    private boolean engineThinking;
    private boolean gameOverDialogShown;
    private long gameGeneration;
    private Task<Move> activeTask;

    @Override
    public void start(final Stage stage) {
        this.game = new GameSession();
        this.boardView = new BoardView(this::handleSquareClick);
        final BorderPane root = new BorderPane();
        root.getStyleClass().add("app-root");
        root.setTop(createHeader());

        this.historyView = new ListView<>();
        this.historyView.getStyleClass().add("move-history");
        final VBox sidePanel = createSidePanel();
        final StackPane boardArea = new StackPane(this.boardView);
        boardArea.getStyleClass().add("board-area");
        this.boardView.prefWidthProperty().bind(javafx.beans.binding.Bindings.min(
                boardArea.widthProperty(), boardArea.heightProperty()));
        this.boardView.prefHeightProperty().bind(this.boardView.prefWidthProperty());

        final HBox content = new HBox(28, boardArea, sidePanel);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(22, 30, 30, 30));
        HBox.setHgrow(boardArea, Priority.ALWAYS);
        root.setCenter(content);

        final Scene scene = new Scene(root, 1160, 790);
        scene.getStylesheets().add(ChessApplication.class.getResource("/chess/engine/gui/app.css").toExternalForm());
        stage.setTitle("Chess Engine");
        stage.setMinWidth(820);
        stage.setMinHeight(620);
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> cancelEngineTask());
        stage.show();
        refreshView();
    }

    private HBox createHeader() {
        final Label title = new Label("CHESS");
        title.getStyleClass().add("app-title");
        final Label subtitle = new Label("A thoughtful game, one move at a time");
        subtitle.getStyleClass().add("app-subtitle");
        final VBox heading = new VBox(4, title, subtitle);
        this.statusLabel = new Label();
        this.statusLabel.getStyleClass().add("status-pill");
        this.turnLabel = new Label();
        this.turnLabel.getStyleClass().add("turn-label");
        final VBox state = new VBox(7, this.statusLabel, this.turnLabel);
        state.setAlignment(Pos.CENTER_RIGHT);
        final HBox header = new HBox(heading, state);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(20);
        header.setPadding(new Insets(22, 30, 18, 30));
        HBox.setHgrow(heading, Priority.ALWAYS);
        return header;
    }

    private VBox createSidePanel() {
        final Label playersTitle = new Label("PLAYERS");
        playersTitle.getStyleClass().add("section-title");
        this.humanLabel = new Label("Your move");
        this.humanLabel.getStyleClass().add("player-card");
        this.computerLabel = new Label("Chess Engine");
        this.computerLabel.getStyleClass().add("player-card");

        final Label movesTitle = new Label("MOVE HISTORY");
        movesTitle.getStyleClass().add("section-title");
        final Button newGame = new Button("New game");
        newGame.getStyleClass().add("new-game-button");
        newGame.setMaxWidth(Double.MAX_VALUE);
        newGame.setOnAction(event -> startNewGame());

        final VBox panel = new VBox(14, playersTitle, this.humanLabel, this.computerLabel,
                movesTitle, this.historyView, newGame);
        panel.getStyleClass().add("side-panel");
        panel.setPadding(new Insets(22));
        panel.setMinWidth(250);
        panel.setPrefWidth(290);
        panel.setMaxWidth(330);
        VBox.setVgrow(this.historyView, Priority.ALWAYS);
        return panel;
    }

    private void handleSquareClick(final int coordinate) {
        if (this.engineThinking || this.game.getBoard().getCurrentPlayer().getAlliance() != Alliance.WHITE
                || isGameOver()) {
            return;
        }

        final Move destinationMove = this.selectedMoves.get(coordinate);
        if (destinationMove != null) {
            if (this.game.play(destinationMove) == MoveStatus.DONE) {
                this.lastMove = destinationMove;
                clearSelection();
                refreshView();
                if (!isGameOver()) {
                    startEngineTurn();
                }
            }
            return;
        }

        final var tile = this.game.getBoard().getTile(coordinate);
        if (tile.isTileOccupied() && tile.getPiece().getPieceAlliance() == Alliance.WHITE) {
            selectPiece(coordinate);
        } else {
            clearSelection();
            refreshView();
        }
    }

    private void selectPiece(final int coordinate) {
        this.selectedSquare = coordinate;
        final Map<Integer, Move> destinations = new LinkedHashMap<>();
        final Player current = this.game.getBoard().getCurrentPlayer();
        for (final Move move : current.getLegalMoves()) {
            if (move.getCurrentCoordinate() != coordinate) {
                continue;
            }
            if (current.makeMove(move).getMoveStatus() == MoveStatus.DONE) {
                destinations.put(move.getDestinationCoordinate(), move);
            }
        }
        this.selectedMoves = Map.copyOf(destinations);
        refreshView();
    }

    private void clearSelection() {
        this.selectedSquare = null;
        this.selectedMoves = Map.of();
    }

    private void startEngineTurn() {
        final Board position = this.game.getBoard();
        final long generation = this.gameGeneration;
        this.engineThinking = true;
        refreshView();

        final Task<Move> task = new Task<>() {
            @Override
            protected Move call() {
                return engine.findBestMove(position, this::isCancelled);
            }
        };
        this.activeTask = task;
        task.setOnSucceeded(event -> {
            if (generation != this.gameGeneration || position != this.game.getBoard()) {
                return;
            }
            this.engineThinking = false;
            final Move move = task.getValue();
            if (move != null && this.game.play(move) == MoveStatus.DONE) {
                this.lastMove = move;
            }
            refreshView();
            showGameOverIfNeeded();
        });
        task.setOnFailed(event -> {
            if (generation != this.gameGeneration) {
                return;
            }
            this.engineThinking = false;
            refreshView();
            final Throwable error = task.getException();
            final Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Engine error");
            alert.setHeaderText("The computer could not complete its move.");
            alert.setContentText(error == null ? "Unknown engine error." : error.getMessage());
            alert.showAndWait();
        });
        task.setOnCancelled(event -> {
            if (generation == this.gameGeneration) {
                this.engineThinking = false;
                refreshView();
            }
        });
        final Thread thread = new Thread(task, "chess-engine-search");
        thread.setDaemon(true);
        thread.start();
    }

    private void startNewGame() {
        this.gameGeneration++;
        cancelEngineTask();
        this.game = new GameSession();
        this.lastMove = null;
        this.gameOverDialogShown = false;
        this.engineThinking = false;
        clearSelection();
        refreshView();
    }

    private void cancelEngineTask() {
        if (this.activeTask != null) {
            this.activeTask.cancel(true);
            this.activeTask = null;
        }
    }

    private boolean isGameOver() {
        final Player current = this.game.getBoard().getCurrentPlayer();
        return current.isInCheckMate() || current.isInStaleMate();
    }

    private void refreshView() {
        if (this.game == null) {
            return;
        }
        final Board board = this.game.getBoard();
        this.boardView.render(board, this.selectedSquare, this.selectedMoves.keySet(), this.lastMove);
        final Player current = board.getCurrentPlayer();
        if (current.isInCheckMate()) {
            this.statusLabel.setText("Checkmate");
            this.turnLabel.setText(current.getOpponent().getAlliance() + " wins");
        } else if (current.isInStaleMate()) {
            this.statusLabel.setText("Draw");
            this.turnLabel.setText("Stalemate");
        } else if (this.engineThinking) {
            this.statusLabel.setText("Computer is thinking");
            this.turnLabel.setText("Please wait…");
        } else if (current.isInCheck()) {
            this.statusLabel.setText("Check");
            this.turnLabel.setText(allianceName(current.getAlliance()) + " to move");
        } else {
            this.statusLabel.setText("In progress");
            this.turnLabel.setText(allianceName(current.getAlliance()) + " to move");
        }
        this.humanLabel.setText((current.getAlliance() == Alliance.WHITE && !this.engineThinking ? "●  " : "○  ") + "You · White");
        this.computerLabel.setText((current.getAlliance() == Alliance.BLACK || this.engineThinking ? "●  " : "○  ") + "Chess Engine · Black");
        updateMoveHistory();
        showGameOverIfNeeded();
    }

    private void updateMoveHistory() {
        final List<String> moves = this.game.getMoveHistory();
        final List<String> rows = new ArrayList<>();
        for (int index = 0; index < moves.size(); index += 2) {
            final String blackMove = index + 1 < moves.size() ? moves.get(index + 1) : "";
            rows.add((index / 2 + 1) + ".   " + moves.get(index) + "     " + blackMove);
        }
        this.historyView.getItems().setAll(rows);
        if (!rows.isEmpty()) {
            this.historyView.scrollTo(rows.size() - 1);
        }
    }

    private void showGameOverIfNeeded() {
        if (this.gameOverDialogShown || this.game == null) {
            return;
        }
        final Player current = this.game.getBoard().getCurrentPlayer();
        if (!current.isInCheckMate() && !current.isInStaleMate()) {
            return;
        }
        this.gameOverDialogShown = true;
        final Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game over");
        alert.setHeaderText(current.isInCheckMate()
                ? allianceName(current.getOpponent().getAlliance()) + " wins by checkmate"
                : "The game is a draw");
        alert.setContentText(current.isInCheckMate() ? "Start a new game whenever you're ready." : "Stalemate. Start a new game whenever you're ready.");
        alert.show();
    }

    private static String allianceName(final Alliance alliance) {
        return alliance == Alliance.WHITE ? "White" : "Black";
    }

    @Override
    public void stop() {
        cancelEngineTask();
    }

    public static void main(final String[] args) {
        launch(args);
    }
}
