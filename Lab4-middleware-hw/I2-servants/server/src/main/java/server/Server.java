package server;

import com.zeroc.Ice.*;
import utils.LoggingUtil;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class Server {
    public static void main(String[] args) {
        try (Communicator communicator = Util.initialize(args)) {
            // Konfiguracja
            Properties properties = communicator.getProperties();
            int port = properties.getPropertyAsIntWithDefault("Server.Port", 10000);
            int evictionTimeout = properties.getPropertyAsIntWithDefault("Server.EvictionTimeout", 30);
            int maxServants = properties.getPropertyAsIntWithDefault("Server.MaxServants", 10);

            // Tworzenie adaptera
            ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints(
                    "ServiceAdapter", "default -p " + port);

            LoggingUtil.log("Server", "Inicjalizacja serwera na porcie " + port);

            // Ustawienie Active Servant Map dla dedykowanych serwantów
            ServantLocator dedicatedLocator = new ServantFactory(
                    "dedicated", TimeUnit.SECONDS.toMillis(evictionTimeout));
            adapter.addServantLocator(dedicatedLocator, "dedicated");

            // Tworzenie współdzielonego serwanta
            SharedServant sharedServant = new SharedServant();
            adapter.add(sharedServant, Util.stringToIdentity("shared/service"));

            LoggingUtil.log("Server", "Zarejestrowano współdzielony serwant");

            // Dodanie ewiktora (rozszerzenie)
            if (properties.getPropertyAsInt("Server.UseCustomEvictor") == 1) {
                LoggingUtil.log("Server", "Aktywowano specjalizowany ewiktor (LRU)");
                CustomEvictorPolicy evictor = new CustomEvictorPolicy(
                        adapter, maxServants, "dedicated");
                adapter.addServantLocator(evictor, "evictor");
            }

            adapter.activate();
            LoggingUtil.log("Server", "Adapter aktywowany, serwer gotowy");

            communicator.waitForShutdown();
        }
    }
}