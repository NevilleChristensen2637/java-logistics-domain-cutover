package com.example.logistics;

public final class ShipmentWorkflowTest {
    public static void main(String[] args) {
        ShipmentWorkflow workflow = new ShipmentWorkflow();
        check("hold-and-review", workflow.nextAction(true, true, true));
        check("close-shipment", workflow.nextAction(true, true, false));
        check("request-proof-of-delivery", workflow.nextAction(true, false, false));
    }
    private static void check(String expected, String actual) {
        if (!expected.equals(actual)) throw new AssertionError(expected + " != " + actual);
    }
}
