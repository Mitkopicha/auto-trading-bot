import React from "react";
// PriceChart component renders an SVG line chart of price candles over time
// it displays the closing prices, grid lines, and time labels on the x-axis
// it marks trade events (buy/sell) on the chart with dots
// it requires at least 2 candles to render the chart
// each candle should have a timestamp and close price
function formatTimeHM(tsMs) {
  if (!tsMs) return "";
  const d = new Date(tsMs);
  if (Number.isNaN(d.getTime())) return "";
  return d.toLocaleTimeString(undefined, { hour: "2-digit", minute: "2-digit" });
}

// Convert various timestamp formats to milliseconds since epoch
function toMs(rawTs) {
  if (rawTs == null) return null;

  if (rawTs instanceof Date) return rawTs.getTime();

  if (typeof rawTs === "string") {
    const t = new Date(rawTs).getTime();
    return Number.isNaN(t) ? null : t;
  }

  const n = Number(rawTs);
  if (Number.isNaN(n)) return null;

  // seconds -> ms
  if (n > 0 && n < 1e12) return n * 1000;

  return n;
}

// Find the candle index that contains the trade timestamp
function findCandleIndex(candles, tradeMs) {
  if (!candles || candles.length === 0 || tradeMs == null) return -1;

  const first = candles[0].ts;
  const last = candles[candles.length - 1].ts;

  if (first == null || last == null) return -1;
  if (tradeMs < first) return -1;
  if (tradeMs > last) return candles.length - 1;

  // Linear scan is fine for ~100 candles
  for (let i = 0; i < candles.length - 1; i++) {
    const a = candles[i].ts;
    const b = candles[i + 1].ts;
    if (a == null || b == null) continue;
    if (tradeMs >= a && tradeMs < b) return i;
  }

  return candles.length - 1;
}

export default function PriceChart({ candles, trades }) {
  if (!candles || candles.length < 2) return <div>No candle data yet.</div>;

  const normCandles = candles
    .map((c) => ({
      ts: toMs(c.ts ?? c.timestamp ?? c.time),
      close: Number(c.close),
    }))
    .filter((c) => c.ts != null && Number.isFinite(c.close));

  if (normCandles.length < 2) return <div>No valid candle data.</div>;

  // ✅ DEBUG LOGS (INSIDE component so candles/trades exist)
  // Comment these out after you verify timestamps look normal.
  console.log("first candle", candles?.[0]?.timestamp, toMs(candles?.[0]?.timestamp));
  console.log("first trade", trades?.[0]?.timestamp, toMs(trades?.[0]?.timestamp));

  const width = 700;
  const height = 220;
  const pad = 40;
  const svgPad = 12;

  const prices = normCandles.map((c) => c.close);
  const min = Math.min(...prices);
  const max = Math.max(...prices);
  const range = max - min || 1;

  const xFor = (i) => pad + (i * (width - pad * 2)) / (normCandles.length - 1);
  const yFor = (p) => pad + (height - pad * 2) * (1 - (p - min) / range);

  const points = prices.map((p, i) => `${xFor(i)},${yFor(p)}`).join(" ");

  // X-axis ticks
  const step = Math.ceil(normCandles.length / 5);
  const timeLabels = [];
  for (let i = 0; i < normCandles.length; i += step) {
    timeLabels.push({ idx: i, label: formatTimeHM(normCandles[i].ts) });
  }

  const minTs = normCandles[0].ts;
  const maxTs = normCandles[normCandles.length - 1].ts;

  // ✅ FIX: place each trade dot on the candle that contains the trade timestamp
  const markers = (trades || [])
    .map((t) => {
      const tradeMs = toMs(t.timestamp);
      if (!tradeMs) return null;

      const idx = findCandleIndex(normCandles, tradeMs);
      if (idx < 0) return null;

      return {
        side: t.side,
        tradeMs,
        x: xFor(idx),
        y: yFor(normCandles[idx].close),
        price: Number(t.price ?? normCandles[idx].close),
        quantity: Number(t.quantity ?? 0),
        pnl: t.pnl,
        iso: t.timestamp,
      };
    })
    .filter((m) => m && m.tradeMs >= minTs && m.tradeMs <= maxTs);

  const [hoveredIdx, setHoveredIdx] = React.useState(null);

  return (
    <div>
      <svg
        width={width + svgPad * 2}
        height={height + svgPad * 2}
        style={{
          border: "1px solid var(--border)",
          background: "#0f131b",
          marginBottom: 8,
        }}
      >
        {/* grid */}
        {[0, 0.25, 0.5, 0.75, 1].map((pct, i) => {
          const yVal = pad + (height - pad * 2) * (1 - pct);
          return (
            <line
              key={`grid-${i}`}
              x1={pad - 5}
              y1={yVal}
              x2={width - pad}
              y2={yVal}
              stroke="var(--border)"
              strokeWidth="0.5"
              opacity="0.3"
            />
          );
        })}

        {/* y labels */}
        {[0, 0.5, 1].map((pct, i) => {
          const yVal = pad + (height - pad * 2) * (1 - pct);
          const price = min + range * pct;
          return (
            <text key={`label-${i}`} x="5" y={yVal + 4} fontSize="11" fill="var(--muted)">
              {price.toFixed(0)}
            </text>
          );
        })}

        {/* price line */}
        <polyline fill="none" stroke="#3ddc97" strokeWidth="2.5" points={points} />

        {/* trade dots */}
        {markers.map((m, i) => (
          <g
            key={i}
            onMouseEnter={() => setHoveredIdx(i)}
            onMouseLeave={() => setHoveredIdx(null)}
          >
            <circle
              cx={m.x}
              cy={m.y}
              r="5"
              fill={m.side === "BUY" ? "#3ddc97" : "#ff6b6b"}
              stroke="white"
              strokeWidth="1.5"
              style={{ cursor: "pointer" }}
            />

            {hoveredIdx === i && (
              <g>
                <rect
                  x={Math.max(pad, m.x - 95)}
                  y={m.y - 85}
                  width="190"
                  height="120"
                  fill="#0d1117"
                  stroke="#3ddc97"
                  strokeWidth="2"
                  rx="4"
                  opacity="0.95"
                />
                <text
                  x={Math.max(pad + 8, m.x - 87)}
                  y={m.y - 62}
                  fontSize="12"
                  fontWeight="800"
                  fill={m.side === "BUY" ? "#3ddc97" : "#ff6b6b"}
                >
                  {m.side}
                </text>
                <text x={Math.max(pad + 8, m.x - 87)} y={m.y - 42} fontSize="11" fill="#888">
                  {formatTimeHM(m.tradeMs)}
                </text>
                <text x={Math.max(pad + 8, m.x - 87)} y={m.y - 22} fontSize="11" fill="#fff">
                  ${m.price.toFixed(2)}
                </text>
                <text x={Math.max(pad + 8, m.x - 87)} y={m.y - 2} fontSize="11" fill="#888">
                  Qty: {m.quantity.toFixed(6)}
                </text>
                <text x={Math.max(pad + 8, m.x - 87)} y={m.y + 18} fontSize="11" fill="#888">
                  PnL: {m.pnl == null ? "—" : Number(m.pnl).toFixed(2)}
                </text>
              </g>
            )}
          </g>
        ))}

        {/* x labels */}
        {timeLabels.map((tl, i) => {
          const x = xFor(tl.idx);
          return (
            <text
              key={`time-${i}`}
              x={x}
              y={height - 5}
              fontSize="11"
              fill="var(--muted)"
              textAnchor="middle"
            >
              {tl.label}
            </text>
          );
        })}

        <line
          x1={pad}
          y1={height - pad}
          x2={width - pad}
          y2={height - pad}
          stroke="var(--border)"
          strokeWidth="1"
        />
      </svg>

      <div style={{ fontSize: 12, color: "var(--muted)" }}>
        Min: {min.toFixed(2)} | Max: {max.toFixed(2)} | Hover trade dots for details
      </div>
    </div>
  );
}
