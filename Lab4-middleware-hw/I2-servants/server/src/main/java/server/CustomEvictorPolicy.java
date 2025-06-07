//package server;
//
//import com.zeroc.Ice.*;
//import utils.LoggingUtil;
//
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.*;
//
//public class CustomEvictorPolicy implements ServantLocator {
//    private final ObjectAdapter adapter;
//    private final int maxSize;
//    private final String category;
//    private final Map<String, Object> servants = new HashMap<>();
//    private final LinkedList<String> lruList = new LinkedList<>();
//    private final String stateDir = "servant_states";
//
//    public CustomEvictorPolicy(ObjectAdapter adapter, int maxSize, String category) {
//        this.adapter = adapter;
//        this.maxSize = maxSize;
//        this.category = category;
//
//        // Utworzenie katalogu na stany serwantów
//        try {
//            Files.createDirectories(Paths.get(stateDir));
//        } catch (IOException e) {
//            LoggingUtil.log("CustomEvictor", "Błąd podczas tworzenia katalogu stanów: " + e.getMessage());
//        }
//
//        LoggingUtil.log("CustomEvictor", "Zainicjalizowano ewiktor LRU (max: " + maxSize + " serwantów)");
//    }
//
//    @Override
//    public Object locate(Current current, LocalObjectHolder cookie) throws UserException {
//        String id = current.id.name;
//        LoggingUtil.log("CustomEvictor", "Żądanie locatora dla obiektu: " + id);
//
//        synchronized (servants) {
//            // Sprawdzenie czy serwant jest już w pamięci
//            Object servant = servants.get(id);
//
//            if (servant != null) {
//                // Aktualizacja LRU
//                lruList.remove(id);
//                lruList.addFirst(id);
//                LoggingUtil.log("CustomEvictor", "Znaleziono serwant w pamięci: " + id);
//                return servant;
//            }
//
//            // Sprawdzenie czy serwant był zapisany na dysku
//            Path statePath = Paths.get(stateDir, id + ".state");
//            if (Files.exists(statePath)) {
//                try {
//                    servant = DedicatedServant.loadState(statePath.toString());
//                    LoggingUtil.log("CustomEvictor", "Wczytano stan serwanta z dysku: " + id);
//                } catch (Exception e) {
//                    LoggingUtil.log("CustomEvictor", "Błąd podczas wczytywania stanu: " + e.getMessage());
//                    servant = new DedicatedServant(id);
//                }
//            } else {
//                // Tworzenie nowego serwanta
//                servant = new DedicatedServant(id);
//                LoggingUtil.log("CustomEvictor", "Utworzono nowy serwant: " + id);
//            }
//
//            // Ewikacja jeśli przekroczono limit
//            if (servants.size() >= maxSize) {
//                evictLRUServant();
//            }
//
//            // Dodanie serwanta do mapy i LRU listy
//            servants.put(id, servant);
//            lruList.addFirst(id);
//
//            return servant;
//        }
//    }
//
//    @Override
//    public void finished(Current current, Object servant, Object cookie) throws UserException {
//        // Nic nie robimy po zakończeniu obsługi żądania
//    }
//
//    @Override
//    public void deactivate(String category) {
//        synchronized (servants) {
//            // Zapisanie stanu wszystkich serwantów przed deaktywacją
//            for (Map.Entry<String, Object> entry : servants.entrySet()) {
//                saveServantState(entry.getKey(), entry.getValue());
//            }
//
//            servants.clear();
//            lruList.clear();
//            LoggingUtil.log("CustomEvictor", "Deaktywacja wszystkich serwantów");
//        }
//    }
//
//    private void evictLRUServant() {
//        if (lruList.isEmpty()) {
//            return;
//        }
//
//        String lruId = lruList.removeLast();
//        Object servant = servants.remove(lruId);
//
//        if (servant != null) {
//            saveServantState(lruId, servant);
//            LoggingUtil.log("CustomEvictor", "Ewikacja najdawniej używanego serwanta: " + lruId);
//        }
//    }
//
//    private void saveServantState(String id, Object servant) {
//        if (servant instanceof DedicatedServant) {
//            try {
//                Path statePath = Paths.get(stateDir, id + ".state");
//                ((DedicatedServant) servant).saveState(statePath.toString());
//            } catch (IOException e) {
//                LoggingUtil.log("CustomEvictor",
//                        "Błąd podczas zapisywania stanu serwanta " + id + ": " + e.getMessage());
//            }
//        }
//    }
//}