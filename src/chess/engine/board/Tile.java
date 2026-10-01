package chess.engine.board;

import chess.engine.pieces.Piece;

import java.util.HashMap;
import java.util.Map;


public abstract class Tile {

        protected final int tileCoordinate;

        private static Map<Integer,EmptyTile> EMPTY_TILES = createdAllPossibilityEmptyTiles();

        private static Map<Integer,EmptyTile> createdAllPossibilityEmptyTiles() {
            final Map<Integer,EmptyTile> emptyTiles = new HashMap<Integer, EmptyTile>();

            for(int i=0;i<64;i++)
                emptyTiles.put(i,new EmptyTile(i));

            return Map.copyOf(emptyTiles);
        }

        public static Tile createTile(final int tileCoordinate, final Piece piece) {
             return piece!=null? new OccupiedTile(tileCoordinate,piece): EMPTY_TILES.get(tileCoordinate);
        }

        private Tile(int tileCoordinate){
            this.tileCoordinate = tileCoordinate;
        }


        public abstract boolean  isTileOccupied();

        public abstract Piece getPiece();

        public static final class EmptyTile extends Tile{

            private EmptyTile(final int Coordinate){
                super(Coordinate);
            }

            @Override
            public boolean  isTileOccupied() {
                return false;
            }

            @Override
            public  Piece getPiece()
                {
                return null;
                }
        }

        public static final class OccupiedTile extends Tile{

            private final Piece pieceOnTile;
            private OccupiedTile(int Coordinate, Piece pieceOnTile){
                super(Coordinate);
                this.pieceOnTile = pieceOnTile;
            }

            @Override
            public boolean  isTileOccupied() {
                return true;
            }

            @Override
            public  Piece getPiece()
                {
                    return this.pieceOnTile;
                }


        }

    }
