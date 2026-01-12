# Questions

Here we have 3 questions related to the code base for you to answer. It is not about right or wrong, but more about what's the reasoning behind your decisions.

1. In this code base, we have some different implementation strategies when it comes to database access layer and manipulation. If you would maintain this code base, would you refactor any of those? Why?

**Answer:**
```txt
I’d refactor the data layer for consistency, because right now Store uses Panache active‑record (Store.findById/persist),
Product uses a Panache repository, Warehouse uses a repository + domain model/use cases, and fulfilment directly uses
EntityManager; that mix makes testing and maintenance harder, so I’d standardize on the repository/use‑case style to
keep persistence behind clear interfaces and make business rules easier to test.
```
----
2. When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded directly everything. What would be your thoughts about what are the pros and cons of each approach and what would be your choice?

**Answer:**
```txt
OpenAPI‑first (used by Warehouse) gives a clear, shared contract and enables client generation, while code‑first
(Store/Product) is faster to build but can drift from documentation; I’d pick one approach for consistency and likely
standardize on OpenAPI‑first if these APIs are consumed by multiple teams or external clients.
```
----
3. Given the need to balance thorough testing with time and resource constraints, how would you prioritize and implement tests for this project? Which types of tests would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
```txt
I’d prioritize tests that protect business rules and key flows: unit tests for rule logic plus a small set of REST
integration tests for critical paths like fulfilment constraints and the legacy‑sync commit behavior, which already 
exist and show the right balance of value vs. cost; then I’d require new business logic to include tests so coverage 
stays meaningful over time.
```