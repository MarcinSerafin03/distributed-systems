package server;

import com.zeroc.Ice.*;
import com.zeroc.Ice.Object;
import utils.LoggingUtil;

import java.util.HashMap;
import java.util.Map;

public class ServantFactory implements ServantLocator {
    private final String category;
    private final long timeout;
    private final Map<String, ServantInfo> servants;

    public ServantFactory(String category, long timeout) {
        this.category = category;
        this.timeout = timeout;
        this.servants = new HashMap<>();
        LoggingUtil.log("ServantFactory", "Utworzono fabrykę serwantów dla kategorii: " + category);
    }

    @Override
    public Object locate(Current current, LocalObjectHolder cookie) throws UserException {
        String id = current.id.name;
        LoggingUtil.log("ServantFactory", "Żądanie locatora dla obiektu: " + id);

        synchronized (servants) {
            ServantInfo info = servants.get(id);

            if (info != null) {
                // Serwant już istnieje
                info.lastAccess = System.currentTimeMillis();
                LoggingUtil.log("ServantFactory", "Znaleziono istniejący serwant dla: " + id);
                cookie.value = info;
                return info.servant;
            }

            // Tworzenie nowego serwanta
            Object servant = createServant(id);
            if (servant != null) {
                info = new ServantInfo(servant, System.currentTimeMillis());
                servants.put(id, info);
                cookie.value = info;
                LoggingUtil.log("ServantFactory", "Utworzono nowy serwant dla: " + id);

                // Uruchomienie wątku ewiktera (jeśli timeout > 0)
                if (timeout > 0) {
                    startEvictorThread();
                }

                return servant;
            }
        }

        throw new ObjectNotExistException();
    }

    @Override
    public void finished(Current current, Object servant, Object cookie) throws UserException {
        // Nic nie robimy po zakończeniu obsługi żądania
    }

    @Override
    public LocateResult locate(Current current) throws UserException {
        return null;
    }

    @Override
    public void finished(Current current, Object object, java.lang.Object o) throws UserException {

    }

    @Override
    public void deactivate(String category) {
        synchronized (servants) {
            LoggingUtil.log("ServantFactory", "Deaktywacja serwantów dla kategorii: " + category);
            servants.clear();
        }
    }

    private Object createServant(String id) {
        try {
            return new DedicatedServant(id);
        } catch (Exception e) {
            LoggingUtil.log("ServantFactory", "Błąd podczas tworzenia serwanta: " + e.getMessage());
            return null;
        }
    }

    private void startEvictorThread() {
        Thread evictorThread = new Thread(() -> {
            LoggingUtil.log("ServantFactory", "Uruchomiono wątek ewiktera");

            while (true) {
                try {
                    Thread.sleep(timeout / 2);
                    evictExpiredServants();
                } catch (InterruptedException e) {
                    break;
                }
            }
        });

        evictorThread.setDaemon(true);
        evictorThread.start();
    }

    private void evictExpiredServants() {
        long now = System.currentTimeMillis();

        synchronized (servants) {
            servants.entrySet().removeIf(entry -> {
                if (now - entry.getValue().lastAccess > timeout) {
                    LoggingUtil.log("ServantFactory", "Ewikacja serwanta: " + entry.getKey());
                    return true;
                }
                return false;
            });
        }
    }

    static class ServantInfo {
        Object servant;
        long lastAccess;

        ServantInfo(Object servant, long lastAccess) {
            this.servant = servant;
            this.lastAccess = lastAccess;
        }
    }
}