module chess.engine {
    requires javafx.controls;
    requires java.xml;

    exports chess.engine;
    exports chess.engine.ai;
    exports chess.engine.board;
    exports chess.engine.game;
    exports chess.engine.gui;
    exports chess.engine.pieces;
    exports chess.engine.player;
}
