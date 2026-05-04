import React, { useState, useEffect, useRef } from "react";
import { Canvas } from "@react-three/fiber";
import { Physics } from "@react-three/cannon";
import { OrbitControls } from "@react-three/drei";
import "./App.css";
import Objects from "./Objects";
import InputManager from "./input-logic/input-manager";

import spaceSoundFile from "./audio/add_sound.mp3";
import controlSoundFile from "./audio/remove_sound.mp3";

const FBL_COORDINATE = [0, 0, 0];

const COLORS = [
  "lightblue","lightgreen","yellow","red","darkgray","orange","pink","white",
  "burlywood","coral","salmon","khaki","limegreen","orchid","turquoise","thistle",
  "tan","skyblue","gold","plum","greenyellow","tomato","mediumaquamarine",
  "mediumorchid","lightcoral","deepskyblue","peru","lightseagreen","steelblue",
  "mediumvioletred","lightgoldenrodyellow","dodgerblue","sandybrown",
  "mediumslateblue","palevioletred","darkseagreen","slateblue","powderblue"
];

const getColor = (length, height, width) => {
  const hash = (length * 73856093) ^ (height * 19349663) ^ (width * 83492791);
  return COLORS[Math.abs(hash) % COLORS.length];
};

function BoxesFromData({ data: boxData, fbl, visibleIndex }) {
  const [fbl_x, fbl_y, fbl_z] = fbl;
  const sortedData = [...boxData].sort((a, b) => a[2][1] - b[2][1]);
  const boxesToShow = sortedData.slice(0, visibleIndex);
  return (
    <>
      {boxesToShow.map(([id, dimensions, location, color]) => {
        const [width, height, length] = dimensions;
        return (
          <Objects.Box
            key={id}
            dimensions={dimensions}
            position={[
              location[0] + fbl_x + width / 2.0,
              location[1] + fbl_y + height / 2.0,
              location[2] + fbl_z + length / 2.0,
            ]}
            color={color}
          />
        );
      })}
    </>
  );
}

function WarehouseFromData({ data: warehouseData, fbl }) {
  const [width, height, length] = warehouseData;
  return (
    <Objects.Warehouse width={width} height={height} length={length} frontBottomLeft={fbl} />
  );
}

function Scene({ modelData }) {
  const [boxData, setBoxData] = useState([]);
  const [warehouseData, setWarehouseData] = useState([10, 10, 10]);
  const [currentBoxIndex, setCurrentBoxIndex] = useState(0);
  const [colorMap, setColorMap] = useState({});
  const spaceAudioRef = useRef(null);
  const controlAudioRef = useRef(null);

  useEffect(() => {
    spaceAudioRef.current = new Audio(spaceSoundFile);
    controlAudioRef.current = new Audio(controlSoundFile);
  }, []);

  const recordColor = (length, height, width) => {
    const color = getColor(length, height, width);
    setColorMap((prev) => {
      if (!(color in prev)) return { ...prev, [color]: [length, height, width] };
      return prev;
    });
    return color;
  };

  useEffect(() => {
    if (modelData) {
      const warehouse = modelData.warehouse;
      setWarehouseData([warehouse.length, warehouse.height, warehouse.width]);
      const parsedData = warehouse.containers.flatMap((container) =>
        container.items.map((item) => {
          const dimensions = [item.length, item.height, item.width];
          const location = [item.x, item.y, item.z];
          recordColor(item.length, item.height, item.width);
          return [item.id, dimensions, location, getColor(item.length, item.height, item.width)];
        })
      );
      setBoxData(parsedData);
      const total = parsedData.length;
      let cancelled = false;
      (async () => {
        let delay = 800;
        for (let i = 1; i < total + 1; i++) {
          if (cancelled) break;
          if (spaceAudioRef.current) {
            spaceAudioRef.current.currentTime = 0;
            spaceAudioRef.current.play().catch(() => {});
          }
          setCurrentBoxIndex(i);
          await new Promise((resolve) => setTimeout(resolve, delay));
          delay *= 0.75;
        }
        if (!cancelled) setCurrentBoxIndex(total);
      })();
      return () => { cancelled = true; };
    }
  }, [modelData]);

  useEffect(() => {
    const onKeyDown = (e) => {
      if (e.code === "ArrowRight" && boxData.length > 0) {
        e.preventDefault();
        if (spaceAudioRef.current) {
          spaceAudioRef.current.currentTime = 0;
          spaceAudioRef.current.play().catch(() => {});
        }
        setCurrentBoxIndex((idx) => (idx + 1) % (boxData.length + 1));
      } else if (e.code === "ArrowLeft" && boxData.length > 0) {
        e.preventDefault();
        if (controlAudioRef.current) {
          controlAudioRef.current.currentTime = 0;
          controlAudioRef.current.play().catch(() => {});
        }
        setCurrentBoxIndex((idx) => idx === 0 ? boxData.length : idx - 1);
      }
    };
    window.addEventListener("keydown", onKeyDown);
    return () => window.removeEventListener("keydown", onKeyDown);
  }, [boxData]);

  const displayIndex = boxData.length > 0 ? currentBoxIndex % (boxData.length + 1) : 0;

  return (
    <div className="app-container" style={{ display: "flex", flexDirection: "column" }}>
      <div className="info-panel" style={{
        position: 'absolute', top: 10, left: 1000, zIndex: 1,
        background: 'rgba(0,0,0,0.6)', color: '#fff',
        padding: '5px 10px', borderRadius: '4px', fontSize: '14px'
      }}>
        {`显示 ${displayIndex}/${boxData.length} 个货物`}
      </div>
      <Canvas camera={{ position: [20, 20, 20], fov: 50 }} dpr={window.devicePixelRatio}>
        <color attach="background" args={["white"]} />
        <ambientLight intensity={0.5} />
        <pointLight position={[10, 10, 10]} />
        <Physics gravity={[0, -9.8, 0]} key={JSON.stringify(warehouseData)}>
          <WarehouseFromData data={warehouseData} fbl={FBL_COORDINATE} />
          <BoxesFromData data={boxData} fbl={FBL_COORDINATE} visibleIndex={currentBoxIndex} />
        </Physics>
        <OrbitControls minPolarAngle={0} maxPolarAngle={Math.PI / 2.1} />
      </Canvas>
      <div style={{ clear: "both", marginTop: 100 }}>
        {Object.keys(colorMap).length > 0 && <h3>图例</h3>}
        <div className="legend-scroll-box">
          {Object.entries(colorMap).map(([color, dims]) => (
            <div className="legend-item" key={color}>
              <div className="legend-color" style={{ backgroundColor: color }} />
              <span>{`${dims[0]} × ${dims[1]} × ${dims[2]}`}</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

function App() {
  const [modelData, setModelData] = useState(null);
  const [warehouseInfo, setWarehouseInfo] = useState(null);
  const [goodsList, setGoodsList] = useState([]);

  // 从URL参数中读取仓库信息和货物列表
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
            // 设置仓库信息
            if (data.storageInfo) {
              setWarehouseInfo(data.storageInfo);
            } else {
              setWarehouseInfo({
                name: data.storageName || "仓库",
                length: wh.length,
                height: wh.height,
                width: wh.width,
              });
            }
            // 设置货物列表
            if (data.goodsList) {
              setGoodsList(data.goodsList);
            }
          }
        })
        .catch(err => console.error("加载仓库数据失败:", err));
    }
  }, []);

  const updateModelData = (data) => setModelData(data);

  return (
    <div className="app-container">
      <div className="left-panel">
        <InputManager
          updateModelData={updateModelData}
          warehouseInfo={warehouseInfo}
          goodsList={goodsList}
        />
      </div>
      <div className="right-panel">
        <Scene key={JSON.stringify(modelData)} modelData={modelData} />
      </div>
    </div>
  );
}

export { BoxesFromData, WarehouseFromData };
export default App;
