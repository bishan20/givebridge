# GiveBridge - Issues & Solutions Log

## Issue #1 — ByteBuddyInterceptor serialization error
**Date:** 2026-04-12
**Endpoint:** GET /api/donations, GET /api/donations/{id}
**Error:**
```
org.springframework.http.converter.HttpMessageConversionException: 
Type definition error: [simple type, class org.hibernate.proxy.pojo.bytebuddy.ByteBuddyInterceptor]
```

**Root Cause:**
Returning the Donation entity directly from the controller. The entity
has a @ManyToOne(fetch = FetchType.LAZY) Campaign field. When Jackson
tries to serialize the Donation, Hibernate hasn't loaded the Campaign
yet — instead it placed a ByteBuddy proxy object as a placeholder.
Jackson doesn't know how to serialize that proxy → error.

**Fix:**
Created DonationResponseDTO and mapped Donation → DonationResponseDTO
in the service layer before returning to the controller. Jackson then
serializes a plain DTO with no proxy objects.

**Lesson:**
Never return Entity objects directly from Controllers.
Always map to a Response DTO first.

---

## Issue #2 — N+1 Query Problem on getAllDonations
**Date:** 2026-04-12
**Endpoint:** GET /api/donations
**Symptom:**
getAllDonations fired multiple SELECT queries against the campaigns
table — one per unique campaign referenced by donations.

**Root Cause:**
FetchType.LAZY on @ManyToOne means Hibernate only loads the Campaign
when explicitly accessed. When mapToResponseDTO calls
donation.getCampaign(), Hibernate fires a separate query per unique
campaign to load it.

**Current Behavior:**
1 query for donations + 1 query per unique campaign referenced.
Hibernate first level cache helps — same campaign is not fetched twice
within the same request. So formula is:
1 + number of UNIQUE campaigns referenced (not total donations)

**Fix (not yet implemented):**
Use JOIN FETCH query in repository:
```java
@Query("SELECT d FROM Donation d JOIN FETCH d.campaign ORDER BY d.donatedAt DESC")
List<Donation> findAllWithCampaignOrderByDonatedAtDesc();
```
This fetches donations and campaigns together in a single query.

**Priority:** Low — acceptable for current scale, fix before production.