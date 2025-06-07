package server;

import ServiceDefinition.Service;
import com.zeroc.Ice.Current;
import utils.LoggingUtil;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class SharedServant implements Service {
    private final ConcurrentHashMap<String, AtomicInteger> invocationCounts;

    public SharedServant() {
        this.invocationCounts = new ConcurrentHashMap<>();
        LoggingUtil.log("SharedServant", "Utworzono współdzielony serwant");
    }

    @Override
    public String performOperation(String input, Current current) {
        String objectId = current.id.name;
        AtomicInteger counter = invocationCounts.computeIfAbsent(objectId,
                k -> new AtomicInteger(0));
        int count = counter.incrementAndGet();

        LoggingUtil.log("SharedServant",
                "Obsługa obiektu: " + objectId + ", wywołanie #" + count + ", parametr: " + input);

        return "Współdzielony serwant obsłużył obiekt " + objectId + " z parametrem: " + input;
    }

    @Override
    public int getInvocationCount(Current current) {
        String objectId = current.id.name;
        AtomicInteger counter = invocationCounts.getOrDefault(objectId, new AtomicInteger(0));
        LoggingUtil.log("SharedServant",
                "Pobrano liczbę wywołań dla obiektu " + objectId + ": " + counter.get());
        return counter.get();
    }
}