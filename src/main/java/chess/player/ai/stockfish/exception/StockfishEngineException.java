package chess.player.ai.stockfish.exception;

public class StockfishEngineException extends RuntimeException {
    public StockfishEngineException() {
        super();
    }

    public StockfishEngineException(String message) {
        super(message);
    }

    public StockfishEngineException(String message, Throwable cause) {
        super(message, cause);
    }

    public StockfishEngineException(Throwable cause) {
        super(cause);
    }
}
