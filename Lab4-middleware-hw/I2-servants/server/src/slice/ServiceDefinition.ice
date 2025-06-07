module ServiceDefinition {
    interface Service {
        string performOperation(string input);
        int getInvocationCount();
    };
};