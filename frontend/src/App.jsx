import { useEffect, useRef, useState } from "react";
import {
  getAccount,
  getPortfolio,
  getTrades,
  runStep,
  runTrain,
  resetAccount,
  trainStep,
  createEquitySnapshot,
  getEquitySnapshots,
  getCandles,
} from "./services/api";

import EquityChart from "./components/EquityChart";
import PriceChart from "./components/PriceChart";
import "./dashboard.css";

function formatTs(ts) {
  if (!ts) return "-";
  const d = new Date(ts);
  if (Number.isNaN(d.getTime())) return ts;
  return d.toLocaleString(undefined, {
    month: "short",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  });
}
function formatTsShort(ts) {
  if (!ts) return "-";
  const d = new Date(ts);
  if (Number.isNaN(d.getTime())) return ts;
  return d.toLocaleString(undefined, {
    day: "2-digit",
    month: "short",
    hour: "2-digit",
    minute: "2-digit",
  });
}

function formatNum(n, decimals = 2) {
  if (n === null || n === undefined || n === "") return "-";
  const x = Number(n);
  if (Number.isNaN(x)) return String(n);
  return x.toFixed(decimals);
}

function HoldingsMiniBar({ portfolio, latestPrice }) {
  const items = (portfolio || [])
    .map((p) => ({
      symbol: p.symbol,
      value: Number(p.quantity) * Number(latestPrice || 0),
    }))
    .filter((x) => x.value > 0)
    .sort((a, b) => b.value - a.value);

  if (items.length === 0) {
    return (
      <div className="miniBarWrap">
        <div className="miniBarTitle">Holdings</div>
        <div className="miniBarEmpty">None</div>
      </div>
    );
  }

  const max = Math.max(...items.map((i) => i.value)) || 1;

  return (
    <div className="miniBarWrap">
      <div className="miniBarTitle">Holdings</div>
      <div className="miniBars">
        {items.slice(0, 3).map((it) => (
          <div key={it.symbol} className="miniBarRow">
            <div className="miniBarLabel">{it.symbol}</div>
            <div className="miniBarTrack">
              <div
                className="miniBarFill"
                style={{ width: `${(it.value / max) * 100}%` }}
                title={`${it.symbol}: $${it.value.toFixed(2)}`}
              />
            </div>
            <div className="miniBarValue">${it.value.toFixed(0)}</div>
          </div>
        ))}
      </div>
    </div>
  );
}

export default function App() {
  const [mode, setMode] = useState("TRADING");
  const [symbol, setSymbol] = useState("BTCUSDT");
  const accountId = mode === "TRADING" ? 1 : 2;

  const [account, setAccount] = useState(null);
  const [portfolio, setPortfolio] = useState([]);
  const [trades, setTrades] = useState([]);

  const [status, setStatus] = useState("");
  const [error, setError] = useState("");

  const [isRunning, setIsRunning] = useState(false);
  const [intervalSec, setIntervalSec] = useState(5);
  const SYMBOLS = ["BTCUSDT", "ETHUSDT", "BNBUSDT", "SOLUSDT"];
  const CANDLE_LIMIT = 100;
  const CANDLE_INTERVAL = "1m";

  // Training
  const [trainLimit, setTrainLimit] = useState(200);
  const [trainIndex, setTrainIndex] = useState(21);
  const [isTrainingRunning, setIsTrainingRunning] = useState(false);
  const trainIndexRef = useRef(trainIndex);

  const [snapshots, setSnapshots] = useState([]);
  const [candles, setCandles] = useState([]);

  const [showSettings, setShowSettings] = useState(false);

  const isActive = mode === "TRADING" ? isRunning : isTrainingRunning;

  const primaryLabel =
    mode === "TRADING"
      ? isRunning
        ? "Pause"
        : "Start Trading"
      : isTrainingRunning
      ? "Pause"
      : "Start Training";

  useEffect(() => {
    trainIndexRef.current = trainIndex;
  }, [trainIndex]);

  async function loadCandles() {
    const data = await getCandles(symbol, CANDLE_LIMIT, CANDLE_INTERVAL);
    setCandles(data);
  }

  async function refresh() {
    await loadCandles();

    setError("");
    const [a, p, t] = await Promise.all([
      getAccount(accountId),
      getPortfolio(accountId),
      getTrades(accountId),
    ]);

    setAccount(a);
    setPortfolio(p);
    setTrades(t);
  }

  async function loadSnapshots() {
    const data = await getEquitySnapshots(accountId, mode, 200);
    setSnapshots(data);
  }

  async function takeSnapshot() {
    setError("");
    await createEquitySnapshot(accountId, mode);
    await loadSnapshots();
    setStatus("Snapshot saved");
  }

  useEffect(() => {
    loadSnapshots().catch(() => {});
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [accountId, mode]);

  useEffect(() => {
    refresh().catch((e) => setError(e.message));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [accountId]);

  async function handleStep() {
    try {
      setStatus("Running step...");
      const result = await runStep(accountId, symbol);
      setStatus(result);
      await refresh();
      await createEquitySnapshot(accountId, mode);
      await loadSnapshots();
    } catch (e) {
      setError(e.message);
    }
  }

  async function handleTrainOnce() {
    try {
      setStatus("Training...");
      const result = await runTrain(accountId, symbol, trainLimit);
      setStatus(result);
      await refresh();
      await createEquitySnapshot(accountId, mode);
      await loadSnapshots();
    } catch (e) {
      setError(e.message);
    }
  }

  async function handleReset() {
    try {
      const ok = window.confirm("Reset will clear portfolio & trades. Continue?");
      if (!ok) return;

      setStatus("Resetting...");
      setError("");

      await resetAccount(accountId);

      setTrades([]);
      setPortfolio([]);
      setSnapshots([]);

      setTrainIndex(21);
      trainIndexRef.current = 21;

      await refresh(); // IMPORTANT: get fresh account/cash before baseline snapshot

      await createEquitySnapshot(accountId, mode); // new baseline
      await loadSnapshots();

      setStatus("Reset: OK");
    } catch (e) {
      setError(e.message);
    }
  }

  function startAuto() {
    setError("");
    setStatus("Auto running...");
    setIsRunning(true);
  }

  function pauseAuto() {
    setStatus("Paused");
    setIsRunning(false);
  }

  function startTrainingAuto() {
    setError("");
    setStatus("Training running...");
    setIsTrainingRunning(true);
  }

  function pauseTrainingAuto() {
    setStatus("Training paused");
    setIsTrainingRunning(false);
  }

  function togglePrimary() {
    if (mode === "TRADING") {
      if (isRunning) pauseAuto();
      else startAuto();
    } else {
      if (isTrainingRunning) pauseTrainingAuto();
      else startTrainingAuto();
    }
  }

  // TRAINING AUTO LOOP
  useEffect(() => {
    if (mode !== "TRAINING") {
      setIsTrainingRunning(false);
      return;
    }
    if (!isTrainingRunning) return;

    const id = setInterval(async () => {
      try {
        const currentIndex = trainIndexRef.current;
        const result = await trainStep(accountId, symbol, trainLimit, currentIndex);

        setStatus(
          `Train: ${result.signal} | trades +${result.tradesExecuted} | next=${result.nextIndex}`
        );

        trainIndexRef.current = result.nextIndex;
        setTrainIndex(result.nextIndex);

        await refresh();
        await createEquitySnapshot(accountId, mode);
        await loadSnapshots();

        if (result.done) {
          setIsTrainingRunning(false);
          setStatus("Training finished");
        }
      } catch (e) {
        setIsTrainingRunning(false);
        setError(e.message);
      }
    }, 500);

    return () => clearInterval(id);
  }, [isTrainingRunning, mode, accountId, symbol, trainLimit]);

  // TRADING AUTO LOOP
  useEffect(() => {
    if (mode !== "TRADING") {
      setIsRunning(false);
      return;
    }
    if (!isRunning) return;

    const id = setInterval(() => {
      handleStep();
    }, intervalSec * 1000);

    return () => clearInterval(id);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isRunning, intervalSec, mode]);

  useEffect(() => {
    loadCandles().catch(() => {});
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [symbol, mode]);

  const latestPrice =
    candles && candles.length > 0 ? Number(candles[candles.length - 1].close) : 0;

  const cryptoValue = portfolio.reduce(
    (sum, p) => sum + Number(p.quantity) * latestPrice,
    0
  );

  const cash = account ? Number(account.cash_balance) : 0;
  const totalEquity = cash + cryptoValue;

  const baselineEquity =
    snapshots.length > 0 ? Number(snapshots[snapshots.length - 1].total_equity) : totalEquity;

  const pnl = totalEquity - baselineEquity;

  const botStatus = isActive ? "Active" : "Paused";
  const lastTradeTs = trades.length > 0 ? trades[0]?.timestamp : null;

  const safeLimit = Math.max(1, Number(trainLimit || 1));
  const progressNow = Math.min(Number(trainIndex || 0), safeLimit);
  const progressPct = Math.min(100, Math.round((progressNow / safeLimit) * 100));

  return (
    <div className="container">
      <div className="header">
        <h1 className="title">Trading Bot Dashboard</h1>
        <div className="badge">
          {error ? `❌ ${error}` : status ? `✅ ${status}` : "Ready"}
        </div>
      </div>

      {/* SUMMARY BAR */}
      <div className="summaryBar">
        <div className="summaryItem">
          <span className="label">Total Equity</span>
          <span className="value">${formatNum(totalEquity, 2)}</span>
        </div>

        <div className={`summaryItem ${pnl >= 0 ? "pos" : "neg"}`}>
          <span className="label">Total PnL</span>
          <span className="value">
            {pnl >= 0 ? "+" : ""}${formatNum(pnl, 2)}
          </span>
        </div>

        <div className="summaryItem">
          <span className="label">Cash</span>
          <span className="value">${formatNum(cash, 2)}</span>
        </div>

        <HoldingsMiniBar portfolio={portfolio} latestPrice={latestPrice} />

        <div className="summaryRight">
          <div className="summaryMeta">
            <span className="label">Trades</span>
            <span className="valueSmall">{trades.length}</span>
          </div>
          <div className="summaryMeta">
            <span className="label">Last trade</span>
            <span className="valueSmall">
              {lastTradeTs ? formatTs(lastTradeTs) : "-"}
            </span>
          </div>
          <span className={`statusBadge ${botStatus.toLowerCase()}`}>
            {botStatus}
          </span>
        </div>
      </div>

      {/* CONTROLS CARD */}
      <div className="card controlsCard" style={{ marginBottom: 16 }}>
        <div className="controlsLayout">
          {/* LEFT */}
          <div className="controlsLeft">
            <h2 className="controlsTitle">Controls</h2>

            <div className="modeTabs underTitle">
              <button
                className={`tab ${mode === "TRADING" ? "active" : ""}`}
                onClick={() => setMode("TRADING")}
              >
                Trading
              </button>
              <button
                className={`tab ${mode === "TRAINING" ? "active" : ""}`}
                onClick={() => setMode("TRAINING")}
              >
                Training
              </button>
            </div>
          </div>

          {/* RIGHT */}
          <div className="controlsRight">
            <div className="controlsActionsRow">
              {/* CENTER */}
              <div className="controlsRowLeft">
                <label className="controlsSymbol">
                  <span className="controlsSymbolLabel">Choose your cryptocurrency</span>
                  <select value={symbol} onChange={(e) => setSymbol(e.target.value)}>
                    {SYMBOLS.map((s) => (
                      <option key={s} value={s}>
                        {s}
                      </option>
                    ))}
                  </select>
                </label>
              </div>

              {/* RIGHT */}
              <div className="controlsRowRight">
                <button className="primaryBtn" onClick={togglePrimary}>
                  {primaryLabel}
                </button>

                <button className="dangerBtn" onClick={handleReset}>
                  Reset
                </button>

                <button
                  className="iconBtn"
                  onClick={() => setShowSettings((v) => !v)}
                  title="Settings"
                >
                  ⚙️
                </button>
              </div>
            </div>

            {mode === "TRAINING" && (
              <div className="trainingRow">
                <div className="trainingLeft">
                  <div className="trainingProgressLabel">
                    Progress: <strong>{progressNow}</strong> / {safeLimit} ({progressPct}%)
                  </div>
                  <div className="trainingProgressTrack">
                    <div
                      className="trainingProgressFill"
                      style={{ width: `${progressPct}%` }}
                    />
                  </div>
                </div>

                <div className="trainingRight">
                  <label className="small">
                    Training data size (candles):&nbsp;
                    <input
                      type="number"
                      min="50"
                      value={trainLimit}
                      onChange={(e) => setTrainLimit(Number(e.target.value))}
                    />
                  </label>

                  <button onClick={handleTrainOnce}>Run Backtest (once)</button>
                </div>
              </div>
            )}

            {showSettings && (
              <div className="settingsPanel">
                {mode === "TRADING" && (
                  <label className="small">
                    Auto interval (sec):&nbsp;
                    <input
                      type="number"
                      min="1"
                      value={intervalSec}
                      onChange={(e) => setIntervalSec(Number(e.target.value))}
                    />
                  </label>
                )}

                {mode === "TRAINING" && (
                  <div className="small" style={{ color: "var(--muted)" }}>
                    Advanced: the training pointer is managed automatically.
                  </div>
                )}
              </div>
            )}
          </div>
        </div>
      </div>


      {/* MAIN GRID */}
      <div className="grid">
        <div className="stack">
          <div className="card">
            <div className="cardTopRow">
              <h2>Price</h2>
              <span className="cardMeta">{`Last ${CANDLE_LIMIT} x ${CANDLE_INTERVAL} candles`}</span>
            </div>
            <div className="chartHeader">
              <div>
                <div className="chartTitleRow">
                  <span className="chartSymbol">{symbol}</span>
                  <span className="chartMetaDot"></span>
                  <span className="chartMeta">{CANDLE_INTERVAL} candles</span>
                  <span className="chartMetaDot"></span>
                  <span className="chartMeta">Last {CANDLE_LIMIT} {CANDLE_INTERVAL === "1m" ? "minutes" : "candles"}</span>
                </div>
                <div className="chartSub">
                  Latest Price: {formatNum(latestPrice, 2)}
                </div>
              </div>

              <div className="chartLegend">
                <span className="legendPill buy">Buy</span>
                <span className="legendPill sell">Sell</span>
              </div>
            </div>
            <PriceChart candles={candles} trades={trades} />
          </div>

          <div className="card">
            <div className="cardTopRow">
              <h2>Equity</h2>
              <span className="cardMeta">{`Last ${snapshots.length} snapshots ~ Captured on each step`}</span>
            </div>

            <div className="equityMetaRow">
              <div className="equityMeta">
                <span className="small">Start</span>
                <strong>${snapshots.length ? formatNum(Number(snapshots[snapshots.length - 1].total_equity), 2) : "—"}</strong>
              </div>
              <div className="equityMeta">
                <span className="small">Now</span>
                <strong>${formatNum(totalEquity, 2)}</strong>
              </div>
              <div className={`equityPnl ${pnl >= 0 ? "pos" : "neg"}`}>
                {pnl >= 0 ? "+" : ""}${formatNum(pnl, 2)}
              </div>
            </div>

            {showSettings && (
              <div style={{ marginBottom: 10 }}>
                <button onClick={takeSnapshot} style={{ marginRight: 10 }}>
                  Take Snapshot
                </button>
                <button onClick={loadSnapshots}>Reload</button>
              </div>
            )}
            <EquityChart snapshots={snapshots} />
          </div>
        </div>

        <div className="stack">
          <div className="card">
            <h2>Portfolio</h2>
            <table className="table">
              <thead>
                <tr>
                  <th>Symbol</th>
                  <th>Quantity</th>
                  <th>Avg Entry</th>
                  <th>Updated</th>
                </tr>
              </thead>
              <tbody>
                {portfolio.length === 0 ? (
                  <tr>
                    <td colSpan="4">No holdings yet. Start trading to buy assets.</td>
                  </tr>
                ) : (
                  portfolio.map((row, idx) => (
                    <tr key={idx}>
                      <td>{row.symbol}</td>
                      <td>{formatNum(row.quantity, 8)}</td>
                      <td>{formatNum(row.avg_entry_price, 2)}</td>
                      <td>{formatTs(row.updated_at)}</td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>

          <div className="card">
            <h2>Trade history</h2>

            <div className="tableScroll tradeHistoryScroll">
              <table className="table">
                <thead>
                  <tr>
                    <th className="colDate">Date</th>
                    <th>Action</th>
                    <th>Symbol</th>
                    <th>Qty</th>
                    <th>Price</th>
                    <th>PNL</th>
                  </tr>
                </thead>
              <tbody>
                {trades.length === 0 ? (
                  <tr>
                    <td colSpan="6">No trades yet. Click “Start Trading” to begin!</td>
                  </tr>
                ) : (
                  trades.map((row, idx) => {
                    const pnlNum =
                      row.pnl === null || row.pnl === undefined ? null : Number(row.pnl);

                    const pnlClass =
                      pnlNum === null || Number.isNaN(pnlNum)
                        ? "pnlMuted"
                        : pnlNum >= 0
                        ? "pnlPos"
                        : "pnlNeg";

                      return (
                        <tr key={idx}>
                          <td className="tdDate" title={row.timestamp}>
                            {formatTsShort(row.timestamp)}
                          </td>
                          <td>{row.side}</td>
                          <td>{row.symbol}</td>
                          <td>{formatNum(row.quantity, 8)}</td>
                          <td>{formatNum(row.price, 2)}</td>
                        <td className={pnlClass}>
                          {pnlNum === null || Number.isNaN(pnlNum)
                            ? "—"
                            : formatNum(pnlNum, 2)}
                        </td>
                      </tr>
                    );
                  })
                )}
              </tbody>
            </table>
            </div>

          </div>
        </div>
      </div>
    </div>
  );
}
