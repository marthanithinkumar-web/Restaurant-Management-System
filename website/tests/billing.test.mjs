import test from "node:test";
import assert from "node:assert/strict";
import { calculateBill, roundCurrency } from "../lib/billing.js";

test("discount starts at exactly ₹500 and tax is calculated after discount", () => {
  assert.deepEqual(calculateBill([{ unitPrice: 499.99, quantity: 1 }]), {
    subtotal: 499.99, discount: 0, taxable: 499.99, tax: 25, total: 524.99
  });
  assert.deepEqual(calculateBill([{ unitPrice: 250, quantity: 2 }]), {
    subtotal: 500, discount: 50, taxable: 450, tax: 22.5, total: 472.5
  });
});

test("currency is rounded to two decimal places", () => {
  assert.equal(roundCurrency(0.1 * 3), 0.3);
  assert.equal(roundCurrency(10.005), 10.01);
});

test("invalid quantities and empty carts are rejected", () => {
  assert.throws(() => calculateBill([]), /At least one order line/);
  assert.throws(() => calculateBill([{ unitPrice: 12, quantity: 0 }]), /quantity/);
});
