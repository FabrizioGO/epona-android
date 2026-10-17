// Constant-time comparison for the shared secrets the edge functions are gated on.
//
// A plain `===` returns as soon as the first byte differs, so response timing leaks
// how much of a guess was right. Hashing both sides first gives equal-length inputs
// (the length of the real secret leaks nothing either), and the XOR fold below
// touches every byte regardless of where they differ.

const encoder = new TextEncoder();

export async function secretsEqual(got: string, expected: string): Promise<boolean> {
  if (expected.length === 0) return false;
  const [a, b] = await Promise.all([
    crypto.subtle.digest("SHA-256", encoder.encode(got)),
    crypto.subtle.digest("SHA-256", encoder.encode(expected)),
  ]);
  const x = new Uint8Array(a);
  const y = new Uint8Array(b);
  let diff = 0;
  for (let i = 0; i < x.length; i++) diff |= x[i] ^ y[i];
  return diff === 0;
}
