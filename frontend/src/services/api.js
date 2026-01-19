const API_BASE = "http://localhost:8080/api";

export async function getAccount(accountId) {
  const res = await fetch(`${API_BASE}/account?accountId=${accountId}`);
  if (!res.ok) {
    throw new Error("Failed to fetch account");
  }
  return res.json();
}
export async function getPortfolio(accountId) {
  const res = await fetch(`${API_BASE}/portfolio?accountId=${accountId}`);
  if (!res.ok) throw new Error("Failed to fetch portfolio");
  return res.json();
}

export async function getTrades(accountId) {
  const res = await fetch(`${API_BASE}/trades?accountId=${accountId}`);
  if (!res.ok) throw new Error("Failed to fetch trades");
  return res.json();
}

export async function runStep(accountId, symbol) {
  const res = await fetch(`${API_BASE}/step?accountId=${accountId}&symbol=${symbol}`, {
    method: "POST",
  });
  if (!res.ok) throw new Error("Step failed");
  return res.text();
}

export async function runTrain(accountId, symbol, limit = 200) {
  const res = await fetch(`${API_BASE}/train?accountId=${accountId}&symbol=${symbol}&limit=${limit}`, {
    method: "POST",
  });
  if (!res.ok) throw new Error("Training failed");
  return res.text();
}

export async function resetAccount(accountId) {
  const res = await fetch(`${API_BASE}/reset?accountId=${accountId}`, {
    method: "POST",
  });
  if (!res.ok) throw new Error("Reset failed");
  return res.text();
}

export async function trainStep(accountId, symbol, limit, index) {
  const res = await fetch(
    `${API_BASE}/train/step?accountId=${accountId}&symbol=${symbol}&limit=${limit}&index=${index}`,
    { method: "POST" }
  );
  if (!res.ok) throw new Error("Training step failed");
  return res.json();
}
export async function createEquitySnapshot(accountId, mode) {
  const res = await fetch(`${API_BASE}/equity/snapshot?accountId=${accountId}&mode=${mode}`, {
    method: "POST",
  });
  if (!res.ok) throw new Error("Snapshot failed");
  return res.text();
}

export async function getEquitySnapshots(accountId, mode, limit = 200) {
  const res = await fetch(`${API_BASE}/equity/snapshots?accountId=${accountId}&mode=${mode}&limit=${limit}`);
  if (!res.ok) throw new Error("Failed to fetch equity snapshots");
  return res.json();
}

export async function getCandles(symbol, limit = 100, interval = "1m") {
  const res = await fetch(`${API_BASE}/market/candles?symbol=${symbol}&limit=${limit}&interval=${interval}`);
  if (!res.ok) throw new Error("Failed to fetch candles");
  return res.json();
}

