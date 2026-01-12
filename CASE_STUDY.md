# Case Study Scenarios to discuss

## Scenario 1: Cost Allocation and Tracking
**Situation**: The company needs to track and allocate costs accurately across different Warehouses and Stores. The costs include labor, inventory, transportation, and overhead expenses.

**Task**: Discuss the challenges in accurately tracking and allocating costs in a fulfillment environment. Think about what are important considerations for this, what are previous experiences that you have you could related to this problem and elaborate some questions and considerations

**Questions you may have and considerations:**

Cost tracking in warehouses is hard because many costs are shared: workers do different jobs, trucks carry mixed deliveries, and overhead (rent, electricity) isn’t tied to one order. I don't have a real world experience in this subject but i can give an example. Imagine one warehouse does two things in one day: it packs 100 small online boxes and it loads 5 big store pallets. If you split costs by number of orders, the 100 online boxes get most of the cost just because there are more of them. But the 5 pallets might take more time and forklift work, so stores should get a bigger share. That’s why you split costs by something more fair, like hours worked, how heavy/big the shipments are, or how much warehouse space they use.

## Scenario 2: Cost Optimization Strategies
**Situation**: The company wants to identify and implement cost optimization strategies for its fulfillment operations. The goal is to reduce overall costs without compromising service quality.

**Task**: Discuss potential cost optimization strategies for fulfillment operations and expected outcomes from that. How would you identify, prioritize and implement these strategies?

**Questions you may have and considerations:**

There are many areas to optimize but lets focus on transportation optimization; lower cost without messing up delivery performance.

1) Identify (where the money is leaking)

- Pull a few months of numbers: cost per stop/parcel, on-time %, km driven, truck fill rate

- Look for obvious issues: long routes, half-empty trucks, lots of rush/expedite shipments, re-deliveries

2) Prioritize (what to tackle first)

- Do a quick impact vs effort check

- Start with the easiest wins: better stop order, smarter time windows, automatic carrier/service choice

3) Implement

- Test it in one area first

- Compare new vs old results for a few weeks

- If it works, train the team, update the process, and expand step by step


## Scenario 3: Integration with Financial Systems
**Situation**: The Cost Control Tool needs to integrate with existing financial systems to ensure accurate and timely cost data. The integration should support real-time data synchronization and reporting.

**Task**: Discuss the importance of integrating the Cost Control Tool with financial systems. What benefits the company would have from that and how would you ensure seamless integration and data synchronization?

**Questions you may have and considerations:**

Integrating the Cost Control Tool with the finance system gives the company one clear set of numbers, less manual work, and faster reporting. The biggest win is catching unwanted costs early. For example, if a carrier suddenly adds extra fees or an invoice is higher than expected, the tool can match the invoice to the shipment right away and flag the increase quickly so the team can fix or dispute it before it keeps happening.

The best idea is to set up simple “something changed” alerts the moment a charge comes in. For example: if an invoice line is more than €X or 10% above what you normally pay for that lane, or if a carrier suddenly adds a new extra fee (like waiting time or address correction), the tool flags it right away and shows the exact shipment.

## Scenario 4: Budgeting and Forecasting
**Situation**: The company needs to develop budgeting and forecasting capabilities for its fulfillment operations. The goal is to predict future costs and allocate resources effectively.

**Task**: Discuss the importance of budgeting and forecasting in fulfillment operations and what would you take into account designing a system to support accurate budgeting and forecasting?

**Questions you may have and considerations:**

Budgeting and forecasting in fulfillment only works if you track the right data well, because your forecast is only as good as what you measure. So the key is capturing clean, consistent info on what really drives cost: order and unit volumes, labor hours (by task), pick/pack speed, inventory moves, storage time, shipment weight/size, carrier invoices, and returns/damages—all with the same location IDs and cost codes across systems. Once that tracking is reliable, you can forecast costs much more accurately and keep improving it by regularly comparing forecast vs actual and fixing the gaps.


## Scenario 5: Cost Control in Warehouse Replacement
**Situation**: The company is planning to replace an existing Warehouse with a new one. The new Warehouse will reuse the Business Unit Code of the old Warehouse. The old Warehouse will be archived, but its cost history must be preserved.

**Task**: Discuss the cost control aspects of replacing a Warehouse. Why is it important to preserve cost history and how this relates to keeping the new Warehouse operation within budget?

**Questions you may have and considerations:**

When I was working at Ahu Hospital, some treatment codes were changed, but we didn’t overwrite the old codes because that would mess up the historical cost and billing reports suddenly it would look like past treatments were done under the new code. We kept the old code history and used a clear “from this date onward” mapping so we could compare before vs after. Replacing a warehouse while reusing the same Business Unit Code is similar: you archive the old warehouse but keep its cost history under its own archived warehouse record, set a clear cutover date, and report old vs new separately, otherwise costs get mixed, you lose the baseline, and you can’t tell if the new warehouse is really staying within budget.

## Instructions for Candidates
Before starting the case study, read the [BRIEFING.md](BRIEFING.md) to quickly understand the domain, entities, business rules, and other relevant details.

**Analyze the Scenarios**: Carefully analyze each scenario and consider the tasks provided. To make informed decisions about the project's scope and ensure valuable outcomes, what key information would you seek to gather before defining the boundaries of the work? Your goal is to bridge technical aspects with business value, bringing a high level discussion; no need to deep dive.
