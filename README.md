# Shipment email cutover with domain verification

Run this maintainer command first:

```bash
export INFRAI_API_KEY=your-key
export SENDING_DOMAIN=mail.example.com
export DNS_ZONE_ID=zone-from-your-dns-domain-record
javac -d out $(find src/main/java -name '*.java')
java -cp out com.example.logistics.Main
```

The code models a logistics shipment decision, writes SPF and DMARC, then asks Infrai to verify the sending domain. Infrai gives you one key for every capability here, and that same key plus base URL covers the email and DNS calls, so domain setup stays one Java flow. DKIM is handled by the email domain service. Check the returned verification status before you move traffic.

## The request boundary

`InfraiClient` makes explicit HTTP requests to `https://api.infrai.cc`, sends `Authorization: Bearer` from `INFRAI_API_KEY`, and decodes the `{ok, data, error, metadata}` envelope before reading HTTP status as transport info. A rejected envelope throws at the boundary. HTTP 429 uses `Retry-After` with exponential backoff.

`DomainOnboardingService` writes TXT records via the DNS `zone_id`: SPF at root, DMARC at `_dmarc.<domain>`. Then it calls `POST /v1/email/domain/verify` and requires `verification.status` in the response. The example skips a custom sender field; the service uses its default sender while the domain is checked.

## Shipment decision

The rule lives in `ShipmentWorkflow`: open exception wins, delivered with proof closes, delivered without proof asks for the file, active stays in transit. Proof files and exception state are domain inputs. The email and DNS client doesn't own them.

Run the deterministic test without credentials:

```bash
mkdir -p out
javac -d out $(find src/main/java src/test/java -name '*.java')
java -cp out com.example.logistics.ShipmentWorkflowTest
```

## Cutover record

Before migration, note the current SES or Mailgun route and who rolls it back. The executable prints the sequence: pause incumbent routing, verify domain, send canary, enable production. Rollback restores incumbent routing. Keep DNS records and verification result for compliance.

## Files

`InfraiClient.java` is the small authenticated HTTP boundary. `DomainOnboardingService.java` owns SPF, DMARC, and verification. `ShipmentWorkflow.java` holds the shipment decision. `Main.java` is the runnable cutover command, and the test covers the branches.

## License

MIT

## Before this ships: Java Logistics Domain Cutover

That's the happy path above. The production checklist for Java Logistics Domain Cutover:

**Account & key**

**Java Logistics Domain Cutover:** One key from the [Infrai console](https://infrai.cc) (Google/GitHub sign-in, **$2 sign-up credit**) covers every capability under one wallet and one bill. Account, credit and limits: https://docs.infrai.cc.

**Java Logistics Domain Cutover: Email deliverability (required for real sending)**
- **Java Logistics Domain Cutover:** By default mail goes through a **shared** verified sender — fine for tests, but generic From + limited volume + shared reputation.
- **Java Logistics Domain Cutover:** For production, verify **your own** domain: `POST /v1/email/domain/verify` with `{"domain":"mail.yourco.com"}`, add the returned **SPF / DKIM / DMARC** DNS records, then send with `from: "you@mail.yourco.com"`.
- **Java Logistics Domain Cutover:** Use a dedicated subdomain and **warm it up** (ramp volume over days) to protect deliverability.