export const DISCOUNT_THRESHOLD = 500;
export const DISCOUNT_RATE = 0.10;
export const TAX_RATE = 0.05;

export function roundCurrency(value) {
  if (!Number.isFinite(Number(value))) throw new TypeError("Amount must be a finite number");
  return Math.round((Number(value) + Number.EPSILON) * 100) / 100;
}

export function calculateBill(lines) {
  if (!Array.isArray(lines) || lines.length === 0) {
    throw new TypeError("At least one order line is required");
  }

  const subtotal = roundCurrency(lines.reduce((sum, line) => {
    const price = Number(line.unitPrice);
    const quantity = Number(line.quantity);
    if (!Number.isFinite(price) || price < 0 || !Number.isInteger(quantity) || quantity < 1 || quantity > 99) {
      throw new TypeError("Each line needs a non-negative price and quantity from 1 to 99");
    }
    return sum + price * quantity;
  }, 0));

  const discount = subtotal >= DISCOUNT_THRESHOLD ? roundCurrency(subtotal * DISCOUNT_RATE) : 0;
  const taxable = roundCurrency(subtotal - discount);
  const tax = roundCurrency(taxable * TAX_RATE);
  const total = roundCurrency(taxable + tax);
  return { subtotal, discount, taxable, tax, total };
}
