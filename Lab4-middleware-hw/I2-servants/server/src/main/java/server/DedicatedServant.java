package server;

import ServiceDefinition.Service;
import com.zeroc.Ice.Current;
import utils.LoggingUtil;

import java.io.*;

public class DedicatedServant implements Service {
    private final String id;
    private int invocationCount;
    private long creationTime;

    public DedicatedServant(String id) {
        this.id = id;
        this.invocationCount = 0;
        this.creationTime = System.currentTimeMillis();
        LoggingUtil.log("DedicatedServant", "Utworzono dedykowanego serwanta dla ID: " + id);
    }

    @Override
    public String performOperation(String input, Current current) {
        invocationCount++;
        LoggingUtil.log("DedicatedServant[" + id + "]",
                "Wywołanie #" + invocationCount + ", operacja z parametrem: " + input);
        return "Dedykowany serwant " + id + " przetworzył: " + input;
    }

    @Override
    public int getInvocationCount(Current current) {
        LoggingUtil.log("DedicatedServant[" + id + "]",
                "Pobrano liczbę wywołań: " + invocationCount);
        return invocationCount;
    }

    // Metody do serializacji i deserializacji stanu (dla ewiktora)
    public void saveState(String filePath) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(new ServantState(id, invocationCount, creationTime));
            LoggingUtil.log("DedicatedServant[" + id + "]", "Stan zapisany do pliku: " + filePath);
        }
    }

    public static DedicatedServant loadState(String filePath) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            ServantState state = (ServantState) ois.readObject();
            DedicatedServant servant = new DedicatedServant(state.getId());
            servant.invocationCount = state.getInvocationCount();
            servant.creationTime = state.getCreationTime();
            LoggingUtil.log("DedicatedServant", "Stan wczytany z pliku: " + filePath);
            return servant;
        }
    }

    // Klasa wewnętrzna do przechowywania stanu
    private static class ServantState implements Serializable {
        private final String id;
        private final int invocationCount;
        private final long creationTime;

        public ServantState(String id, int invocationCount, long creationTime) {
            this.id = id;
            this.invocationCount = invocationCount;
            this.creationTime = creationTime;
        }

        public String getId() {
            return id;
        }

        public int getInvocationCount() {
            return invocationCount;
        }

        public long getCreationTime() {
            return creationTime;
        }
    }
}