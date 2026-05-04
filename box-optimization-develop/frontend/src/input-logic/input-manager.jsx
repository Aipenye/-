import React, { useRef, useState, useEffect } from "react";
import "../App.css";

const API_BASE = "http://localhost:8083";

function InputManager({ updateModelData, warehouseInfo, goodsList }) {
  const autoCalculateRef = useRef(false);
  const [boxes, setBoxes] = useState([]);
  const [warehouseInput, setWarehouseInput] = useState("");
  const [modelData, setModelData] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [showUnplaced, setShowUnplaced] = useState(false);
  const [showToast, setShowToast] = useState(false);
  const [strategy, setStrategy] = useState("skjolber");

  // 接收来自WMS的postMessage数据（装箱优化模式）
  useEffect(() => {
    const handler = (event) => {
      if (event.data && event.data.type === "WMS_OPTIMIZE") {
        const { warehouse, solvingStrategy } = event.data.payload;
        if (!warehouse) return;
        const wh = warehouse;
        setWarehouseInput(`${wh.length},${wh.height},${wh.width}`);
        setStrategy(solvingStrategy || "skjolber");
        const items = wh.containers && wh.containers[0] && wh.containers[0].items
          ? wh.containers[0].items
          : [];
        autoCalculateRef.current = true;
        setBoxes(items.map((item) => ({
          id: item.id,
          length: item.length,
          height: item.height,
          width: item.width,
          x: item.x != null ? item.x : 0,
          y: item.y != null ? item.y : 0,
          z: item.z != null ? item.z : 0,
          locked: false,
        })));
      }
    };
    window.addEventListener("message", handler);
    return () => window.removeEventListener("message", handler);
  }, []);

  // 接收来自URL参数的可视化数据
  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const dataUrl = params.get("dataUrl");
    if (dataUrl) {
      fetch(decodeURIComponent(dataUrl))
        .then(res => res.json())
        .then(res => {
          if (res.code === 200 && res.data) {
            const data = res.data;
            const wh = data.warehouse;
            setWarehouseInput(`${wh.length},${wh.height},${wh.width}`);
            setStrategy(data.solvingStrategy || "skjolber");
            const items = wh.containers && wh.containers[0] && wh.containers[0].items
              ? wh.containers[0].items : [];
            autoCalculateRef.current = true;
            setBoxes(items.map((item) => ({
              id: item.id,
              length: item.length,
              height: item.height,
              width: item.width,
              placed: false,
              locked: false,
            })));
          }
        })
        .catch(err => console.error("加载数据失败:", err));
    }
  }, []);

  useEffect(() => {
    if (autoCalculateRef.current && boxes.length > 0) {
      autoCalculateRef.current = false;
      setTimeout(() => {
        document.getElementById("wms-auto-calculate")?.click();
      }, 50);
    }
  }, [boxes]);

  const parseWarehouseDimensions = (input) => {
    const parts = input.split(",").map((p) => parseFloat(p.trim()));
    if (parts.length !== 3 || parts.some((v) => isNaN(v) || v <= 0)) {
      return null;
    }
    return { length: parts[0], height: parts[1], width: parts[2] };
  };

  const updateBoxesWithModelData = (data) => {
    boxes.forEach((box) => {
      const boxinfo = data.warehouse.containers[0].items.find(
        (d) => String(d.id).trim() === String(box.id).trim()
      );
      if (boxinfo != null) {
        box.x = boxinfo.x;
        box.y = boxinfo.y;
        box.z = boxinfo.z;
      }
    });
  };

  const handleCalculate = async () => {
    setIsLoading(true);
    setError(null);
    setSuccess(null);

    const parsedWarehouse = parseWarehouseDimensions(warehouseInput);
    if (!parsedWarehouse) {
      setError("警告！仓库尺寸无效，格式应为：长,高,宽（均大于0）");
      setIsLoading(false);
      return;
    }
    if (boxes.length === 0) {
      setError("该仓库没有货物数据");
      setIsLoading(false);
      return;
    }

    const { length, height, width } = parsedWarehouse;
    const payload = {
      solvingStrategy: strategy,
      warehouse: {
        length, height, width,
        containers: [{
          id: "1",
          length, height, width,
          items: boxes.map((box) => ({
            id: box.id,
            length: parseFloat(box.length),
            height: parseFloat(box.height),
            width: parseFloat(box.width),
            placed: false,
          })),
        }],
      },
    };

    try {
      const response = await fetch(`${API_BASE}/api/algorithm/run`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });
      if (!response.ok) throw new Error("请求服务器失败");
      const data = await response.json();
      setModelData(data);
      updateBoxesWithModelData(data);
      updateModelData(data);
      setSuccess("装箱方案计算成功！");
      if (data?.warehouse?.unplacedItems?.length > 0) {
        setShowToast(true);
        setTimeout(() => setShowToast(false), 4000);
      }
    } catch (err) {
      setError("计算失败：" + err.message);
    } finally {
      setIsLoading(false);
    }
  };

  const handleDownload = () => {
    if (!modelData) { setError("没有可下载的方案"); return; }
    const blob = new Blob([JSON.stringify(modelData, null, 2)], { type: "application/json" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = "装箱方案.json";
    a.click();
    URL.revokeObjectURL(url);
  };

  return (
    <div className="input-manager-wrapper">
      <div className="input-manager">
        <h1 className="title">仓库空间优化系统</h1>

        {/* 仓库属性 */}
        {warehouseInfo && (
          <div className="form-section info-card">
            <h2>🏭 仓库属性</h2>
            <div className="info-row"><span className="info-label">仓库名称：</span><span className="info-value">{warehouseInfo.name}</span></div>
            <div className="info-row"><span className="info-label">长度：</span><span className="info-value">{warehouseInfo.length} cm</span></div>
            <div className="info-row"><span className="info-label">高度：</span><span className="info-value">{warehouseInfo.height} cm</span></div>
            <div className="info-row"><span className="info-label">宽度：</span><span className="info-value">{warehouseInfo.width} cm</span></div>
          </div>
        )}

        {/* 仓库使用情况 */}
        {goodsList && goodsList.length > 0 && (
          <div className="form-section info-card">
            <h2>📦 仓库使用情况</h2>
            <table className="goods-table">
              <thead>
                <tr>
                  <th>货物名称</th>
                  <th>长(cm)</th>
                  <th>宽(cm)</th>
                  <th>高(cm)</th>
                  <th>数量</th>
                </tr>
              </thead>
              <tbody>
                {goodsList.map((goods) => (
                  <tr key={goods.id}>
                    <td>{goods.name}</td>
                    <td>{goods.length ?? '-'}</td>
                    <td>{goods.width ?? '-'}</td>
                    <td>{goods.height ?? '-'}</td>
                    <td>{goods.count ?? 1}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {/* 选择算法 */}
        <div className="form-section">
          <h2>选择优化算法</h2>
          <select
            id="model-select"
            className="input-field"
            value={strategy}
            onChange={(e) => setStrategy(e.target.value)}
          >
            <option value="timefold">Timefold（精确模式）</option>
            <option value="skjolber">Skjolber（快速模式）</option>
          </select>
        </div>

        {/* 操作按钮 */}
        <div className="actions">
          <button
            id="wms-auto-calculate"
            className="calculate-button"
            onClick={handleCalculate}
            disabled={isLoading}
          >
            {isLoading ? "计算中..." : "开始计算"}
          </button>

          <button
            className="unplaced-button"
            onClick={() => setShowUnplaced(!showUnplaced)}
            disabled={!modelData}
          >
            {showUnplaced ? "隐藏未放置货物" : "查看未放置货物"}
          </button>

          <button className="download-button" onClick={handleDownload} disabled={!modelData}>
            下载方案
          </button>

          {error && <p className="error">{error}</p>}
          {success && <p className="success">{success}</p>}
        </div>

        {/* 重置按钮 */}
        <div className="window-buttons" style={{ marginTop: "1rem" }}>
          <button className="reset-button" onClick={() => window.location.reload()}>
            重置
          </button>
        </div>
      </div>

      {/* 未放置货物列表 */}
      {showUnplaced && (
        <div className="unplaced-window">
          <h3>以下货物无法放置</h3>
          {modelData?.warehouse?.unplacedItems?.length > 0 ? (
            <ul>
              {modelData.warehouse.unplacedItems.map((item) => (
                <li key={item.id}>
                  货物 {item.id}：{item.length} × {item.height} × {item.width}
                </li>
              ))}
            </ul>
          ) : (
            <p>所有货物均已成功放置。</p>
          )}
        </div>
      )}

      {showToast && (
        <div className="toast-notification">
          警告！部分货物无法放入仓库，请查看未放置货物列表。
        </div>
      )}
    </div>
  );
}

export default InputManager;
