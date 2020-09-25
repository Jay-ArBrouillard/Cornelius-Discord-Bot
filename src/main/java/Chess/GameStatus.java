package chess;

public enum GameStatus {
    INACTIVE,
    START_UP,
    PROCESS_MOVE,
    BLACK_WIN,
    WHITE_WIN,
    BLACK_FORFEIT,
    WHITE_FORFEIT,
    WHITE_IN_CHECK,
    BLACK_IN_CHECK,
    STALEMATE,
    RESIGNATION
}