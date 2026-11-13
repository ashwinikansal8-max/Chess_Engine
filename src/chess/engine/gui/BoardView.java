package chess.engine.gui;

import chess.engine.board.Board;
import chess.engine.board.BoardUtils;
import chess.engine.board.Move;
import chess.engine.pieces.Piece;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.IntConsumer;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

final class BoardView extends GridPane {

    private static final Map<Piece.PieceType, String> PIECE_PATHS = loadPiecePaths();
    private final List<StackPane> cells = new ArrayList<>(BoardUtils.NUM_TILES);
    private final IntConsumer squareHandler;

    BoardView(final IntConsumer squareHandler) {
        this.squareHandler = squareHandler;
        getStyleClass().add("chess-board");
        setHgap(0);
        setVgap(0);
        for (int i = 0; i < BoardUtils.NUM_TILES_PER_ROW; i++) {
            final ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(12.5);
            getColumnConstraints().add(column);
            final RowConstraints row = new RowConstraints();
            row.setPercentHeight(12.5);
            getRowConstraints().add(row);
        }
        for (int coordinate = 0; coordinate < BoardUtils.NUM_TILES; coordinate++) {
            final int square = coordinate;
            final StackPane cell = new StackPane();
            cell.getStyleClass().add("board-square");
            cell.setMinSize(0, 0);
            cell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            cell.setOnMouseClicked(event -> this.squareHandler.accept(square));
            this.cells.add(cell);
            add(cell, BoardUtils.getColumn(coordinate), BoardUtils.getRow(coordinate));
        }
    }

    void render(final Board board, final Integer selectedSquare, final Set<Integer> legalDestinations,
                final Move lastMove) {
        final int lastFrom = lastMove == null ? -1 : lastMove.getCurrentCoordinate();
        final int lastTo = lastMove == null ? -1 : lastMove.getDestinationCoordinate();
        final int checkedKing = board.getCurrentPlayer().isInCheck()
                ? board.getCurrentPlayer().getPlayerKing().getPiecePosition() : -1;

        for (int coordinate = 0; coordinate < BoardUtils.NUM_TILES; coordinate++) {
            final StackPane cell = this.cells.get(coordinate);
            cell.getChildren().clear();
            cell.setStyle(squareStyle(coordinate, selectedSquare, lastFrom, lastTo, checkedKing));
            final Piece piece = board.getTile(coordinate).getPiece();
            if (piece != null) {
                final SVGPath icon = new SVGPath();
                icon.setContent(PIECE_PATHS.get(piece.getPieceType()));
                icon.setFill(piece.getPieceAlliance().isWhite() ? Color.web("#f5f0e6") : Color.web("#263238"));
                icon.setStroke(piece.getPieceAlliance().isWhite() ? Color.web("#4b514a") : Color.web("#101719"));
                icon.setStrokeWidth(1.8);
                icon.scaleXProperty().bind(cell.widthProperty().multiply(0.0075));
                icon.scaleYProperty().bind(cell.heightProperty().multiply(0.0075));
                cell.getChildren().add(icon);
            }
            if (legalDestinations.contains(coordinate)) {
                final Circle marker = new Circle();
                marker.radiusProperty().bind(cell.widthProperty().multiply(piece == null ? 0.1 : 0.34));
                marker.setFill(Color.color(0.1, 0.12, 0.1, piece == null ? 0.32 : 0));
                if (piece != null) {
                    marker.setStroke(Color.color(0.1, 0.12, 0.1, 0.58));
                    marker.setStrokeWidth(4);
                }
                cell.getChildren().add(marker);
            }
            addCoordinateLabels(cell, coordinate);
        }
    }

    private static String squareStyle(final int coordinate, final Integer selectedSquare,
                                      final int lastFrom, final int lastTo, final int checkedKing) {
        final boolean light = (BoardUtils.getRow(coordinate) + BoardUtils.getColumn(coordinate)) % 2 == 0;
        String color = light ? "#eeeed2" : "#769656";
        if (coordinate == lastFrom || coordinate == lastTo) {
            color = light ? "#f5f682" : "#baca44";
        }
        if (coordinate == checkedKing) {
            color = "#ef7772";
        }
        final String border = selectedSquare != null && coordinate == selectedSquare
                ? "-fx-border-color: #f0ad4e; -fx-border-width: 4;"
                : "";
        return "-fx-background-color: " + color + "; " + border;
    }

    private static void addCoordinateLabels(final StackPane cell, final int coordinate) {
        if (BoardUtils.getColumn(coordinate) == 0) {
            final Text rank = new Text(Integer.toString(8 - BoardUtils.getRow(coordinate)));
            rank.getStyleClass().add("coordinate-label");
            StackPane.setAlignment(rank, Pos.TOP_LEFT);
            cell.getChildren().add(rank);
        }
        if (BoardUtils.getRow(coordinate) == 7) {
            final Text file = new Text(Character.toString((char) ('a' + BoardUtils.getColumn(coordinate))));
            file.getStyleClass().add("coordinate-label");
            StackPane.setAlignment(file, Pos.BOTTOM_RIGHT);
            cell.getChildren().add(file);
        }
    }

    private static Map<Piece.PieceType, String> loadPiecePaths() {
        try (InputStream input = BoardView.class.getResourceAsStream("/chess/engine/gui/pieces.svg")) {
            if (input == null) {
                throw new IllegalStateException("Bundled chess piece vectors are missing");
            }
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            final NodeList paths = factory.newDocumentBuilder().parse(input).getElementsByTagName("path");
            final Map<Piece.PieceType, String> result = new EnumMap<>(Piece.PieceType.class);
            for (int i = 0; i < paths.getLength(); i++) {
                final Element path = (Element) paths.item(i);
                result.put(Piece.PieceType.valueOf(path.getAttribute("id")), path.getAttribute("d"));
            }
            for (final Piece.PieceType type : Piece.PieceType.values()) {
                if (!result.containsKey(type)) {
                    throw new IllegalStateException("Missing bundled vector for " + type);
                }
            }
            return Map.copyOf(result);
        } catch (final Exception exception) {
            throw new IllegalStateException("Unable to load bundled chess piece vectors", exception);
        }
    }
}
