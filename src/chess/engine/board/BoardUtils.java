package chess.engine.board;

public final class BoardUtils {

    public static final int NUM_TILES = 64;
    public static final int NUM_TILES_PER_ROW = 8;

    private static final String[] ALGEBRAIC_NOTATION = initializeAlgebraicNotation();

    private BoardUtils() {
        throw new RuntimeException("You cannot instantiate me!");
    }

    public static boolean isValidTileCoordinate(final int coordinate) {
        return coordinate >= 0 && coordinate < NUM_TILES;
    }

    public static int getRow(final int coordinate) {
        return coordinate / NUM_TILES_PER_ROW;
    }

    public static int getColumn(final int coordinate) {
        return coordinate % NUM_TILES_PER_ROW;
    }

    public static String getPositionAtCoordinate(final int coordinate) {
        return ALGEBRAIC_NOTATION[coordinate];
    }

    private static String[] initializeAlgebraicNotation() {
        final String[] notation = new String[NUM_TILES];
        for (int i = 0; i < NUM_TILES; i++) {
            notation[i] = "" + (char) ('a' + getColumn(i)) + (8 - getRow(i));
        }
        return notation;
    }
}
