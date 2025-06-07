#!/usr/bin/env python3
import sys
import Ice
import random
import time
from datetime import datetime
import threading
import generated.ServiceDefinition_ice as ServiceDefinition

def log(source, message):
    timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S.%f")[:-3]
    print(f"[{timestamp}] [{source}] {message}")

def get_proxy(communicator, category, name, type_id):
    identity = Ice.Identity()
    identity.category = category
    identity.name = name
    
    proxy = communicator.stringToProxy(f"{identity.category}/{identity.name}:default -p 10000")
    
    if category == "dedicated" or category == "evictor":
        # Użycie checkedCast dla dedykowanych serwantów
        log("Client", f"Wykonuję checkedCast dla {category}/{name}")
        return ServiceDefinition.ServicePrx.checkedCast(proxy)
    else:
        # Użycie uncheckedCast dla współdzielonych serwantów
        log("Client", f"Wykonuję uncheckedCast dla {category}/{name}")
        return ServiceDefinition.ServicePrx.uncheckedCast(proxy)

def test_dedicated_servant(communicator, servant_id):
    log("Client", f"Testowanie dedykowanego serwanta: {servant_id}")
    
    # Pierwszy krok: uzyskanie proxy
    proxy = get_proxy(communicator, "dedicated", servant_id, "::ServiceDefinition::Service")
    
    if proxy:
        # Test wykonania operacji
        result = proxy.performOperation(f"Test dedykowanego serwanta {servant_id}")
        log("Client", f"Wynik operacji: {result}")
        
        # Sprawdzenie liczby wywołań
        count = proxy.getInvocationCount()
        log("Client", f"Liczba wywołań: {count}")
    else:
        log("Client", f"Nie udało się uzyskać proxy dla {servant_id}")

def test_shared_servant(communicator, service_name):
    log("Client", f"Testowanie współdzielonego serwanta dla usługi: {service_name}")
    
    # Uzyskanie proxy
    proxy = get_proxy(communicator, "shared", service_name, "::ServiceDefinition::Service")
    
    if proxy:
        # Test wykonania operacji
        result = proxy.performOperation(f"Test współdzielonego serwanta dla {service_name}")
        log("Client", f"Wynik operacji: {result}")
        
        # Sprawdzenie liczby wywołań
        count = proxy.getInvocationCount()
        log("Client", f"Liczba wywołań: {count}")
    else:
        log("Client", f"Nie udało się uzyskać proxy dla współdzielonego serwanta {service_name}")

def test_evictor(communicator, servant_ids, max_servants):
    log("Client", f"Testowanie ewiktora z {len(servant_ids)} serwantami (max: {max_servants})")
    
    # Testowanie ewikacji - aktywacja więcej serwantów niż maksymalna ilość
    for i, servant_id in enumerate(servant_ids):
        proxy = get_proxy(communicator, "evictor", servant_id, "::ServiceDefinition::Service")
        
        # Wykonanie operacji
        result = proxy.performOperation(f"Test ewiktora dla serwanta {servant_id}")
        log("Client", f"Serwant {i+1}/{len(servant_ids)}: {result}")
        
        # Pauza dla lepszej czytelności logów
        time.sleep(0.5)
    
    # Ponownie odwołanie do pierwszego serwanta (powinien być przywrócony z dysku)
    log("Client", f"Próba dostępu do pierwszego serwanta po ewikacji")
    proxy = get_proxy(communicator, "evictor", servant_ids[0], "::ServiceDefinition::Service")
    result = proxy.performOperation("Test przywrócenia serwanta")
    count = proxy.getInvocationCount()
    log("Client", f"Wynik: {result}, liczba wywołań: {count}")

def main():
    with Ice.initialize(sys.argv) as communicator:
        try:
            log("Client", "Inicjalizacja klienta")
            
            # Test dedykowanych serwantów
            for i in range(1, 4):
                test_dedicated_servant(communicator, f"service{i}")
                time.sleep(1)
            
            # Test współdzielonych serwantów
            for i in range(1, 4):
                test_shared_servant(communicator, f"service{i}")
                time.sleep(1)
            
            # Test ewiktora (rozszerzenie)
            log("Client", "Rozpoczynam test ewiktora...")
            servant_ids = [f"evictor_test_{i}" for i in range(1, 16)]  # Utworzenie 15 serwantów (max 10)
            test_evictor(communicator, servant_ids, 10)
            
            log("Client", "Testy zakończone")
            
        except Ice.Exception as e:
            log("Client", f"Błąd: {e}")
            return 1
    
    return 0

if __name__ == "__main__":
    sys.exit(main())