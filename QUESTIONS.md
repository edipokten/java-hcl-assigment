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
Advantages of Open API approach

1. Single source of truth (clear API contract) – everyone uses the same definition of endpoints, inputs, outputs, and auth.
2. Auto docs + easier onboarding – interactive documentation makes it faster to understand and use the API.
3. Validation/testing support – schema-based validation and contract tests catch breaking changes early.

Disadvantages of Open API approach

1. Maintenance burden – if you don’t keep it updated, it becomes misleading.
2. Verbose/complex specs – large APIs create big files that are hard to manage manually.
3. Doesn’t capture everything / awkward cases – some behaviors (streaming/events, deep unions, business rules) don’t fit neatly, and generated SDKs may still need manual improvement.

Advantages of traditional approach
1. Fast to start (minimal setup)
2. Flexible (easy to describe special behaviors in plain text)
3. Less tooling overhead (no spec file to maintain)

Disadvantages of Open API approach
1. Docs drift easily (implementation changes, docs don’t)
2. Harder integrations (less precise contract, more back-and-forth)
3. Less automation (no guaranteed validation/contract tests, limited code generation)

I  choose the traditional approach when the API is small, changes often, or is only used by one team. Because it’s faster and more flexible.

I  choose OpenAPI when multiple teams or partners depend on the API because a shared contract, auto-docs, and validation reduce confusion and breaking changes. 


```
----
3. Given the need to balance thorough testing with time and resource constraints, how would you prioritize and implement tests for this project? Which types of tests would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
```txt
I’d use TDD as the main approach: backend developers write a small failing test first for each business rule (validation, permissions, errors), then implement the code to pass and refactor safely.

Pros: fewer bugs and regressions, clearer requirements (tests act like specs), safer refactoring, and more reliable APIs over time.
Cons / costs: it usually takes more time upfront (writing tests first), there’s a learning curve, and tests still need maintenance.
Overall, you trade slower initial development for code that’s more error-proof and cheaper to fix later.
```