package utils;

public class LoadingBar {
    public static void main(String[] args) {
        int totalTasks = 100;
        for (int i = 0; i <= totalTasks; i++) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            updateLoadingBar(i, totalTasks);
        }
    }

    public static void printDynamicLoadingBar(int numPacketsRecv, int numPackets) throws InterruptedException {
        if (numPackets > 3) {
            updateLoadingBar(numPacketsRecv, numPackets);
        } else {
            for (int n = 0; n < 10; n++) {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                double numPacketsRecvDbl = (double) (n + 1) / 10;
                updateLoadingBar(numPacketsRecvDbl, numPackets);
            }
        }
    }

    public static void updateLoadingBar(double currentTask, int totalTasks) {
        int percentComplete = (int) (((double) currentTask / totalTasks) * 100);

        String colour;
        if (percentComplete < 26) {
            colour = Colour.RED;
        } else if (percentComplete < 51) {
            colour = Colour.ORANGE;
        } else if (percentComplete < 76) {
            colour = Colour.YELLOW_BOLD;
        } else if (percentComplete < 95) {
            colour = Colour.YELLOW;
        } else {
            colour = Colour.GREEN;
        }

        StringBuilder loadingBar = new StringBuilder("[");
        int completed = (int) (percentComplete / 2.0);
        for (int i = 0; i < completed; i++) {
            loadingBar.append("=");
        }
        for (int i = completed; i < 50; i++) {
            loadingBar.append(" ");
        }
        loadingBar.append("] " + percentComplete + "%." + Colour.RESET + " Packets received: " + (int) currentTask + "/" + totalTasks + " ");

        // Clear previous line and print updated loading bar
        System.out.print("\r");
        System.out.print(colour + loadingBar.toString());
        System.out.flush();

        if (currentTask == totalTasks) {
            System.out.println();
        }
    }
}
