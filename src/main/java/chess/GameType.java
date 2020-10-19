package chess;

public enum GameType {
    PVP {
        @Override
        public boolean isPlayerVsComputer() {
            return false;
        }

        @Override
        public boolean isPlayerVsPlayer() {
            return true;
        }

        @Override
        public boolean isComputerVsComputer() {
            return false;
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

        @Override
        public boolean isComputerVsComputer() {
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

        @Override
        public boolean isComputerVsComputer() {
            return false;
        }
    },
    CVC {
        @Override
        public boolean isPlayerVsComputer() {
            return false;
        }

        @Override
        public boolean isPlayerVsPlayer() {
            return false;
        }

        @Override
        public boolean isComputerVsComputer() {
            return true;
        }
    };

    public abstract boolean isPlayerVsComputer();
    public abstract boolean isPlayerVsPlayer();
    public abstract boolean isComputerVsComputer();
}
