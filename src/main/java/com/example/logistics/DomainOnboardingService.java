package com.example.logistics;

public final class DomainOnboardingService {
    private final InfraiClient client;
    public DomainOnboardingService(InfraiClient client) { this.client = client; }

    public String configure(String domain, String zoneId) throws Exception {
        String records = "{\"zone_id\":\"" + zoneId + "\",\"record_type\":\"TXT\",\"name\":\"" + domain + "\",\"content\":\"v=spf1 include:send.example ~all\",\"ttl\":300}";
        client.request("PUT", "/v1/dns/record/upsert", records);
        String dmarc = "{\"zone_id\":\"" + zoneId + "\",\"record_type\":\"TXT\",\"name\":\"_dmarc." + domain + "\",\"content\":\"v=DMARC1; p=quarantine\",\"ttl\":300}";
        client.request("PUT", "/v1/dns/record/upsert", dmarc);
        String verification = client.request("POST", "/v1/email/domain/verify", "{\"domain\":\"" + domain + "\"}");
        if (!verification.contains("verification") || !verification.contains("status")) throw new IllegalStateException("verification response missing status");
        return verification;
    }
}
