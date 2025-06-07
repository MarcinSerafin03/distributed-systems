package org.keeper;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import javax.swing.*;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ZKWatcherApp implements Watcher {
    private ZooKeeper zk;
    private final String znode = "/a";
    private Process guiProcess;
    private final CountDownLatch connectedSignal = new CountDownLatch(1);
    private String guiCommand = "C:\\Users\\marci\\AppData\\Local\\Microsoft\\WindowsApps\\mspaint.exe";;

    public ZKWatcherApp(String hosts) throws IOException, InterruptedException, KeeperException {
        this.zk = new ZooKeeper(hosts, 3000, this);

        connectedSignal.await();

        System.out.println("Połączono z ZooKeeper. Rozpoczynam obserwację znode /a");
        watchZnode();
    }

    private void watchZnode() throws KeeperException, InterruptedException {
        try {
            Stat stat = zk.exists(znode, this);
            if (stat != null) {
                System.out.println("Znode /a już istnieje - uruchamiam aplikację GUI");
                startGUI();
                watchChildren();
            } else {
                System.out.println("Znode /a nie istnieje - czekam na utworzenie");
            }
        } catch (KeeperException.NoNodeException e) {
            System.out.println("Znode /a nie istnieje - ustawiam watch na utworzenie");
        }
    }

    private void startGUI() {
        if (guiProcess == null || !guiProcess.isAlive()) {
            try {
                System.out.println("Uruchamiam zewnętrzną aplikację GUI: " + guiCommand);
                guiProcess = Runtime.getRuntime().exec(guiCommand);


                Thread.sleep(500);
                if (!guiProcess.isAlive()) {
                    System.err.println("Proces GUI nie uruchomił się poprawnie");
                    guiProcess = null;
                } else {
                    System.out.println("Aplikacja GUI uruchomiona pomyślnie (PID: " + guiProcess.pid() + ")");
                }
            } catch (IOException | InterruptedException e) {
                System.err.println("Błąd podczas uruchamiania aplikacji GUI: " + e.getMessage());
                guiProcess = null;
            }
        } else {
            System.out.println("Aplikacja GUI już działa (PID: " + guiProcess.pid() + ")");
        }
    }

    private void stopGUI() {
        if (guiProcess != null && guiProcess.isAlive()) {
            System.out.println("Zatrzymuję zewnętrzną aplikację GUI (PID: " + guiProcess.pid() + ")");

            try {
                String cmd = "taskkill /PID " + guiProcess.pid() + " /T /F";
                Process killer = Runtime.getRuntime().exec(cmd);
                killer.waitFor();

                if (guiProcess.isAlive()) {
                    System.err.println("UWAGA: Nie udało się zamknąć procesu GUI!");
                } else {
                    System.out.println("Aplikacja GUI została zamknięta");
                }

            } catch (IOException | InterruptedException e) {
                System.err.println("Błąd podczas zamykania GUI przez taskkill: " + e.getMessage());
                Thread.currentThread().interrupt();
            }

            guiProcess = null;
        } else {
            System.out.println("Aplikacja GUI nie jest uruchomiona");
        }
    }


    private void watchChildren() throws KeeperException, InterruptedException {
        try {
            List<String> children = zk.getChildren(znode, this);  // Ustawiamy watch z this, nie nowym listenerem
            System.out.println("Ustawiono watch na potomków znode /a (aktualna liczba: " + children.size() + ")");
        } catch (KeeperException.NoNodeException e) {
            System.out.println("Znode /a został usunięty podczas ustawiania watch na potomków");
        }
    }


    private void printTree(String path, int indent) throws KeeperException, InterruptedException {
        try {
            List<String> children = zk.getChildren(path, false);
            for (String child : children) {
                String childPath = path.equals("/") ? "/" + child : path + "/" + child;
                System.out.println("  ".repeat(indent) + "├── " + child);
                printTree(childPath, indent + 1);
            }
        } catch (KeeperException.NoNodeException e) {
            System.out.println("  ".repeat(indent) + "└── [węzeł został usunięty]");
        }
    }

    public void displayTree() throws KeeperException, InterruptedException {
        System.out.println("\n=== Struktura drzewa znode '/a' ===");
        try {
            Stat stat = zk.exists(znode, false);
            if (stat != null) {
                System.out.println("/a");
                printTree(znode, 1);
            } else {
                System.out.println("Znode /a nie istnieje");
            }
        } catch (KeeperException.NoNodeException e) {
            System.out.println("Znode /a nie istnieje");
        }
        System.out.println("=================================\n");
    }

    @Override
    public void process(WatchedEvent event) {
        System.out.println("Otrzymano zdarzenie: " + event.getType() + " dla ścieżki: " + event.getPath());

        try {
            switch (event.getState()) {
                case SyncConnected -> {
                    System.out.println("Połączono z ZooKeeper");
                    connectedSignal.countDown();
                }
                case Disconnected -> {
                    System.out.println("Rozłączono z ZooKeeper");
                }
                case Expired -> {
                    System.out.println("Sesja ZooKeeper wygasła");
                    System.exit(1);
                }
            }

            switch (event.getType()) {
                case NodeCreated -> {
                    if (event.getPath().equals(znode)) {
                        System.out.println("Znode /a został utworzony - uruchamiam GUI");
                        startGUI();
                        watchChildren();
                    }
                }
                case NodeDeleted -> {
                    if (event.getPath().equals(znode)) {
                        System.out.println("Znode /a został usunięty - zamykam GUI");
                        stopGUI();
                        System.out.println("Ustawiam ponownie watch na znode /a");
                        zk.exists(znode, this);
                    }
                }
                case NodeChildrenChanged -> {
                    if (event.getPath().equals(znode)) {
                        List<String> updatedChildren = zk.getChildren(znode, this); // Odśwież i ustaw watch ponownie
                        SwingUtilities.invokeLater(() -> {
                            String message = "Aktualna liczba potomków znode /a: " + updatedChildren.size();
                            System.out.println(message);
                            JOptionPane.showMessageDialog(null, message, "ZooKeeper Watcher", JOptionPane.INFORMATION_MESSAGE);
                        });
                    }
                }
                case NodeDataChanged -> {
                    System.out.println("Zmieniono dane w znode: " + event.getPath());
                }
            }
        } catch (Exception e) {
            System.err.println("Błąd podczas przetwarzania zdarzenia: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void close() throws InterruptedException {
        System.out.println("Zamykam aplikację...");
        stopGUI();
        if (zk != null) {
            zk.close();
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Użycie: java ZKWatcherApp <zookeeper-hosts>");
            System.out.println("Przykład: java ZKWatcherApp localhost:2181");
            return;
        }

        ZKWatcherApp app = new ZKWatcherApp(args[0]);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                app.close();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }));

        System.out.println("\n=== ZooKeeper Watcher Application ===");
        System.out.println("Aplikacja monitoruje znode /a");
        System.out.println("Dostępne komendy:");
        System.out.println("  'show' - wyświetl strukturę drzewa /a");
        System.out.println("  'status' - sprawdź status aplikacji GUI");
        System.out.println("  'exit' - zakończ aplikację");
        System.out.println("=====================================\n");

        java.util.Scanner scanner = new java.util.Scanner(System.in);
        while (true) {
            System.out.print("Wpisz komendę: ");
            String input = scanner.nextLine().trim();

            if (input.equals("exit")) {
                break;
            } else if (input.equals("show")) {
                app.displayTree();
            } else if (input.equals("status")) {
                if (app.guiProcess != null && app.guiProcess.isAlive()) {
                    System.out.println("Aplikacja GUI działa (PID: " + app.guiProcess.pid() + ")");
                } else {
                    System.out.println("Aplikacja GUI nie działa");
                }
            } else if (!input.isEmpty()) {
                System.out.println("Nieznana komenda: " + input);
            }
        }

        app.close();
        System.out.println("Aplikacja zakończona");
    }
}
