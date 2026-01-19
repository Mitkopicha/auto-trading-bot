function nearestIndex(candles, tradeTimeMs) {
  
  let best = 0;
  let bestDiff = Infinity;
  for (let i = 0; i < candles.length; i++) {
    const diff = Math.abs(candles[i].time - tradeTimeMs);
    if (diff < bestDiff) {
      bestDiff = diff;
      best = i;
    }
  }
  return best;
}

export default function PriceChart({ candles, trades }) {
  if (!candles || candles.length < 2) return <div>No candle data yet.</div>;

  const width = 700;
  const height = 220;
  const pad = 12;

  const prices = candles.map(c => Number(c.close));
  const min = Math.min(...prices);
  const max = Math.max(...prices);
  const range = max - min || 1;

  const xFor = (i) => pad + (i * (width - pad * 2)) / (candles.length - 1);
  const yFor = (p) => pad + (height - pad * 2) * (1 - (p - min) / range);

  const points = prices.map((p, i) => `${xFor(i)},${yFor(p)}`).join(" ");

  
  const markers = (trades || [])
    .filter(t => t.symbol && t.timestamp)
    .map(t => {
      const tradeTimeMs = Date.parse(t.timestamp);
      const idx = nearestIndex(candles, tradeTimeMs);
      const p = prices[idx];
      return {
        side: t.side,
        x: xFor(idx),
        y: yFor(p),
      };
    });

  const last = prices[prices.length - 1].toFixed(2);

  return (
    <div>
      <div style={{ marginBottom: 6 }}>
        <strong>Latest Price:</strong> {last}
      </div>

      <svg width={width} height={height} style={{ border: "1px solid #ccc" }}>
        <polyline fill="none" stroke="black" strokeWidth="2" points={points} />

        {markers.map((m, i) => (
          <circle
            key={i}
            cx={m.x}
            cy={m.y}
            r="4"
            fill={m.side === "BUY" ? "lime" : "red"}
            stroke="black"
          />
        ))}
      </svg>

      <div style={{ fontSize: 12, marginTop: 6 }}>
        Min: {min.toFixed(2)} | Max: {max.toFixed(2)} | Markers: BUY=green, SELL=red
      </div>
    </div>
  );
}
