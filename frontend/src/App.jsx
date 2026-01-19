import { useEffect, useState } from "react";
import { getAccount, getPortfolio, getTrades, runStep, runTrain, resetAccount, trainStep } from "./services/api";
import EquityChart from "./components/EquityChart";
import { createEquitySnapshot, getEquitySnapshots } from "./services/api";
import PriceChart from "./components/PriceChart";
import { getCandles } from "./services/api";



function App() {
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

  const [trainIndex, setTrainIndex] = useState(21);
  const [trainLimit, setTrainLimit] = useState(200);
  const [isTrainingRunning, setIsTrainingRunning] = useState(false);
  const [snapshots, setSnapshots] = useState([]);
  const [candles, setCandles] = useState([]);



  async function loadCandles() {
  const data = await getCandles(symbol, 100, "1m");
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
    } catch (e) {
      setError(e.message);
    }
  }

  async function handleTrain() {
    try {
      setStatus("Training...");
      // Tip: use a different accountId for training later if you want.
      const result = await runTrain(accountId, symbol, 200);
      setStatus(result);
      await refresh();
    } catch (e) {
      setError(e.message);
    }
  }

  async function handleReset() {
  try {
    setStatus("Resetting...");
    const result = await resetAccount(accountId);
    setStatus(`Reset: ${result}`);
    await refresh();
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

useEffect(() => {
  if (mode !== "TRAINING") {
    setIsTrainingRunning(false);
    return;
  }
  if (!isTrainingRunning) return;

  const id = setInterval(async () => {
    try {
      const result = await trainStep(accountId, symbol, trainLimit, trainIndex);

      setStatus(`Train: ${result.signal} | trades +${result.tradesExecuted} | next=${result.nextIndex}`);
      setTrainIndex(result.nextIndex);

      await refresh();

      if (result.done) {
        setIsTrainingRunning(false);
        setStatus("Training finished");
      }
    } catch (e) {
      setIsTrainingRunning(false);
      setError(e.message);
    }
  }, 500); // 0.5 sec per step (fast but visible)

  return () => clearInterval(id);
  // eslint-disable-next-line react-hooks/exhaustive-deps
}, [isTrainingRunning, trainIndex, trainLimit, mode, accountId, symbol]);

useEffect(() => {
  // Only allow auto-run in TRADING mode
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

  return (
    <div style={{ padding: 20, fontFamily: "Arial" }}>
      <h1>Trading Bot Dashboard</h1>

      {error && <div style={{ marginBottom: 10 }}>❌ {error}</div>}
      {status && <div style={{ marginBottom: 10 }}>✅ {status}</div>}

          <div style={{ marginBottom: 10 }}>
      <label style={{ marginRight: 10 }}>
        Mode:&nbsp;
        <select value={mode} onChange={(e) => setMode(e.target.value)}>
          <option value="TRADING">TRADING (Account 1)</option>
          <option value="TRAINING">TRAINING (Account 2)</option>
        </select>
      </label>

      <label>
        Symbol:&nbsp;
        <input
          value={symbol}
          onChange={(e) => setSymbol(e.target.value)}
          style={{ width: 120 }}
        />
      </label>
    </div>
{mode === "TRAINING" && (
  <div style={{ marginBottom: 10 }}>
    <label style={{ marginRight: 10 }}>
      Limit:&nbsp;
      <input
        type="number"
        min="50"
        value={trainLimit}
        onChange={(e) => setTrainLimit(Number(e.target.value))}
        style={{ width: 80 }}
      />
    </label>

    <label style={{ marginRight: 10 }}>
      Index:&nbsp;
      <input
        type="number"
        min="21"
        value={trainIndex}
        onChange={(e) => setTrainIndex(Number(e.target.value))}
        style={{ width: 80 }}
      />
    </label>

    {!isTrainingRunning ? (
      <button onClick={startTrainingAuto}>Start Training</button>
    ) : (
      <button onClick={pauseTrainingAuto}>Pause Training</button>
    )}
  </div>
)}

    
{mode === "TRADING" && (
  <div style={{ marginBottom: 10 }}>
    <label style={{ marginRight: 10 }}>
      Auto interval (sec):&nbsp;
      <input
        type="number"
        min="1"
        value={intervalSec}
        onChange={(e) => setIntervalSec(Number(e.target.value))}
        style={{ width: 70 }}
      />
    </label>

    {!isRunning ? (
      <button onClick={startAuto}>Start Live</button>
    ) : (
      <button onClick={pauseAuto}>Pause</button>
    )}
  </div>
)}

      <div style={{ marginBottom: 20 }}>
        <button
          onClick={handleStep}
          disabled={mode !== "TRADING" || isRunning}
          style={{ marginRight: 10 }}
        >
          Step (Live)
      </button>
        <button onClick={handleTrain} disabled={mode !== "TRAINING"}>
          Train (Backtest)
        </button>
        <button onClick={handleReset} style={{ marginLeft: 10 }}>
        Reset
      </button>

      </div>
      <h2 style={{ marginTop: 20 }}>Equity Over Time</h2>

<div style={{ marginBottom: 10 }}>
  <button onClick={takeSnapshot} style={{ marginRight: 10 }}>Take Snapshot</button>
  <button onClick={loadSnapshots}>Reload</button>
</div>
<h2 style={{ marginTop: 20 }}>Price Chart (with trades)</h2>
<PriceChart candles={candles} trades={trades} />

<EquityChart snapshots={snapshots} />


      <h2>Account</h2>
      {!account ? (
        <div>Loading account...</div>
      ) : (
        <div>
          <div><strong>ID:</strong> {account.id}</div>
          <div><strong>Cash:</strong> {account.cash_balance}</div>
        </div>
      )}

      <h2 style={{ marginTop: 20 }}>Portfolio</h2>
      <table border="1" cellPadding="8" style={{ borderCollapse: "collapse", width: "100%" }}>
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
            <tr><td colSpan="4">No holdings</td></tr>
          ) : (
            portfolio.map((row, idx) => (
              <tr key={idx}>
                <td>{row.symbol}</td>
                <td>{row.quantity}</td>
                <td>{row.avg_entry_price ?? "-"}</td>
                <td>{row.updated_at}</td>
              </tr>
            ))
          )}
        </tbody>
      </table>

      <h2 style={{ marginTop: 20 }}>Trade History</h2>
      <table border="1" cellPadding="8" style={{ borderCollapse: "collapse", width: "100%" }}>
        <thead>
          <tr>
            <th>Time</th>
            <th>Side</th>
            <th>Symbol</th>
            <th>Qty</th>
            <th>Price</th>
            <th>PNL</th>
          </tr>
        </thead>
        <tbody>
          {trades.length === 0 ? (
            <tr><td colSpan="6">No trades yet</td></tr>
          ) : (
            trades.map((row, idx) => (
              <tr key={idx}>
                <td>{row.timestamp}</td>
                <td>{row.side}</td>
                <td>{row.symbol}</td>
                <td>{row.quantity}</td>
                <td>{row.price}</td>
                <td>{row.pnl ?? "-"}</td>
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
}

export default App;
