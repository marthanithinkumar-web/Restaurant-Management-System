# Testing Plan

Run the application with the compile/run commands in README.md. Automated domain regression tests are in `tests/RestaurantManagementSystemTest.java`; GitHub Actions compiles the application and tests, executes the regression suite, and performs a startup/exit smoke test. The current automated checks passed in [GitHub Actions](https://github.com/marthanithinkumar-web/Restaurant-Management-System/actions). Manual console scenarios below remain a separate acceptance checklist. Record actual observed results before using this as a submitted lab report.

| Test | Input/action | Expected behaviour |
|---|---|---|
| T01 | Start application | Default menu is displayed |
| T02 | Display menu | All seeded items and prices appear |
| T03 | Add a valid food item with a new ID | Item is added |
| T04 | Add an item using an existing ID | Duplicate ID is rejected |
| T05 | Update an existing item price | New price is shown |
| T06 | Remove an existing item | Item no longer appears |
| T07 | Enter letters at an integer prompt | Error message; prompt repeats |
| T08 | Enter zero or negative quantity | Input is rejected |
| T09 | Create order with subtotal ₹400.00 | Discount ₹0.00; tax ₹20.00; total ₹420.00 |
| T10 | Create order with subtotal ₹500.00 | Discount ₹50.00; tax ₹22.50; total ₹472.50 |
| T11 | Create order with subtotal ₹600.00 | Discount ₹60.00; tax ₹27.00; total ₹567.00 |
| T12 | Select cash, UPI, or card | Corresponding simulated payment message appears |
| T13 | Finish an order without adding items | Order is cancelled; no bill generated |
| T14 | Remove an unknown item ID | “Item not found” message appears |
| T15 | Create a valid order and select a payment method | Unique order number and date/time appear; order is added to session history |
| T16 | View order history after completing an order | Order number, date/time, customer, total, and payment method are listed |
| T17 | View order history before completing any order | Informational message appears; application remains usable |

## Billing examples
- ₹400 subtotal: discount ₹0.00; tax 5% of ₹400 = ₹20.00; total ₹420.00.
- ₹500 subtotal: discount 10% = ₹50.00; tax 5% of ₹450 = ₹22.50; total ₹472.50.
- ₹600 subtotal: discount 10% = ₹60.00; tax 5% of ₹540 = ₹27.00; total ₹567.00.

The automated regression suite checks the three billing totals and key domain validation behaviours. The manual console scenarios above are not all automated. Order history is session-only and is cleared when the program exits.