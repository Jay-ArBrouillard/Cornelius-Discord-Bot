package chess;

public enum GameMode {
    PVP {
        @Override
        public boolean isPlayerVsComputer() {
            return false;
        }
    },
    PVC {
        @Override
        public boolean isPlayerVsComputer() {
            return true;
        }
    },
    CVP {
        @Override
        public boolean isPlayerVsComputer() {
            return true;
        }
    };

    public abstract boolean isPlayerVsComputer();
}
