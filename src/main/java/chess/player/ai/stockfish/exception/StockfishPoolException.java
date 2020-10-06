package chess.player.ai.stockfish.exception;

public class StockfishPoolException extends IllegalStateException {
    public StockfishPoolException() {
        super();
    }

    public StockfishPoolException(String message) {
        super(message);
    }

    public StockfishPoolException(String message, Throwable cause) {
        super(message, cause);
    }

    public StockfishPoolException(Throwable cause) {
        super(cause);
    }
}
