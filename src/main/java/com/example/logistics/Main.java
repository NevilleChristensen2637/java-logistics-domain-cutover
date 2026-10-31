package com.example.logistics;

public final class Main {
    public static void main(String[] args) throws Exception {
        String domain = required("SENDING_DOMAIN");
        String zoneId = required("DNS_ZONE_ID");
        String key = required("INFRAI_API_KEY");
        String result = new DomainOnboardingService(new InfraiClient(key)).configure(domain, zoneId);
        System.out.println("Sending domain checked: " + result);
        System.out.println("Shipment decision: " + new ShipmentWorkflow().nextAction(true, true, false));
        System.out.println("Cutover: pause incumbent, verify domain, send a canary, then enable production traffic.");
        System.out.println("Rollback: restore incumbent routing and keep the domain records for audit review.");
    }
    private static String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " is required");
        return value;
    }
}
