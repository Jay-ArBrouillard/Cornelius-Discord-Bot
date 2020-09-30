package chess;

public enum GameMode {
    PVP {
        @Override
        public boolean isPlayerVsComputer() {
            return false;
        }

        @Override
        public boolean isPlayerVsPlayer() {
            return true;
        }
    },
    PVC {
        @Override
        public boolean isPlayerVsComputer() {
            return true;
        }

        @Override
        public boolean isPlayerVsPlayer() {
            return false;
        }
    },
    CVP {
        @Override
        public boolean isPlayerVsComputer() {
            return true;
        }

        @Override
        public boolean isPlayerVsPlayer() {
            return false;
        }
    };

    public abstract boolean isPlayerVsComputer();
    public abstract boolean isPlayerVsPlayer();
}
