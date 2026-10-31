package com.example.logistics;

public final class ShipmentWorkflow {
    public String nextAction(boolean delivered, boolean proofUploaded, boolean exceptionOpen) {
        if (exceptionOpen) return "hold-and-review";
        if (delivered && proofUploaded) return "close-shipment";
        if (delivered) return "request-proof-of-delivery";
        return "continue-in-transit";
    }
}
