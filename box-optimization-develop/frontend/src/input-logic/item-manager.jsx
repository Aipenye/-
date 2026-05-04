import React, { useState } from "react";
import "../App.css";

function BoxManager({ boxes, setBoxes }) {
  const [boxInput, setBoxInput] = useState("");
  const [pathwaysInput, setPathwaysInput] = useState("");
  const [nextBoxId, setNextBoxId] = useState(1);
  const [error, setError] = useState(null);

const parseBoxInput = (input) => {
  const segments = input.split(";").map(seg => seg.trim()).filter(Boolean);
  const boxes = [];
  let currentId = nextBoxId;

  for (const segment of segments) {
    const parts = segment.split(",").map(p => p.trim());
    if (parts.length !== 4 && parts.length !== 6) {
      return { error: "Error! Each box must have 4 values: length, height, width, quantity or 6 values: length, height, width, x, y, z" };
    }
    if (parts.length === 4) {
      const [length, height, width, quantity] = parts.map(Number);

      if ([length, height, width, quantity].some(isNaN)) {
        return { error: "Error! All values must be numbers." };
      }

      if (length <= 0 || height <= 0 || width <= 0) {
        return { error: "Error! Box dimensions must be greater than 0." };
      }

      if (!Number.isInteger(quantity) || quantity <= 0) {
        return { error: "Error! Quantity must be an integer greater than 0." };
      }
      for (let i = 0; i < quantity; i++) {
        boxes.push({
          id: currentId++,
          length,
          height,
          width,
          placed:false,
        });
      }
    }
    if (parts.length === 6) {
      const [length, height, width, x, y, z] = parts.map(Number);

      if ([length, height, width, x, y, z].some(isNaN)) {
        return { error: "Error! All values must be numbers." };
      }

      if (length <= 0 || height <= 0 || width <= 0) {
        return { error: "Error! Box dimensions must be greater than 0." };
      }
      if ( x < 0 || y < 0 || z < 0) {
        return { error: "Error! Box position is outside the warehouse. "}
      }
        boxes.push({
          id: currentId++,
          length,
          height,
          width,
          placed:true,
          x,
          y,
          z,
        });
      
    }


  }

  return { boxes };
};

  const addBoxes = () => {
    const result = parseBoxInput(boxInput);
    if (result.error) {
      setError(result.error);
      return;
    }

    setBoxes([...boxes, ...result.boxes]);
    setNextBoxId(nextBoxId + result.boxes.length);
    setBoxInput("");
    setError(null);
  };

  const deleteBox = (id) => {
    setBoxes(boxes.filter((b) => b.id !== id));
  };

  const lockBox = (id) => {
    boxes.filter((b) => b.id == id)[0].locked=true;
  };

  const unlockBox = (id) => {
    boxes.filter((b) => b.id == id)[0].locked=false;
  };

  const isLockedBox = (id) => {
    return boxes.filter((b) => b.id == id)[0].locked;
  }

  const toggleLockBox = (id) => {
    if (isLockedBox(id)) {
      unlockBox(id);
      document.getElementById("boxitem" + id).innerHTML = "&#x1F513";
    } else {
      lockBox(id);
      document.getElementById("boxitem" + id).innerHTML = "&#x1F512";
    }
  }

  return (
    <div className="form-section">
      <h2>Box Dimensions</h2>
      <p class="field-instruction">Input Format: length1, height1, width1, quantity1; length2, height2, ...<br /> Or: length1, height1, width1, x1, y1, z1; ... (only supported by timefold)</p>
      <input
        className="input-field"
        type="text"
        placeholder="e.g. 5,5,5,10; 5,1,1,0,0,0"
        value={boxInput}
        onChange={(e) => setBoxInput(e.target.value)}
      />
      <button className="add-button" onClick={addBoxes}>
        Add Box
      </button>

      {error && <p style={{ color: "red"}}>{error}</p>}

      {boxes.length > 0 && (
      <>
        <h5 style={{ marginTop: "20px" }}>Added Boxes</h5>
        <ul className="box-list">
          {boxes.map((box, index) => (
            <li key={box.id} className="box-item">
              <span>
                Box {index + 1}: {box.length}×{box.height}×{box.width}
                {box.x != null && box.y != null && box.z != null && (
                  <> &nbsp;x:{box.x} y:{box.y} z:{box.z}</>
                )}
              </span>
              <button 
                className="lock-button" 
                id={"boxitem" + box.id}
                onClick={() => toggleLockBox(box.id)}>
                &#x1F513;
              </button>
              <button className="delete-button" onClick={() => deleteBox(box.id)}>
                Delete
              </button>
            </li>
          ))}
        </ul>
      </>
    )}

    </div>
  );
}

export default BoxManager;