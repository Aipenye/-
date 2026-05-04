    import React, { useState, useCallback } from "react";

    const InputBar = ({ onCreateBoxes, onSolve }) => {
    const [fields, setFields] = useState({
        width: "",
        height: "",
        depth: "",
        quantity: "",
    });
    const [error, setError] = useState("");

    const handleChange = useCallback((e) => {
        const { name, value } = e.target;
        setFields((prev) => ({ ...prev, [name]: value }));
    }, []);

    const handleSubmit = useCallback(
        (e) => {
        e.preventDefault();
        const { width, height, depth, quantity } = fields;

        // Validate that all inputs are positive numbers
        if (
            isNaN(width) ||
            isNaN(height) ||
            isNaN(depth) ||
            isNaN(quantity) ||
            Number(width) <= 0 ||
            Number(height) <= 0 ||
            Number(depth) <= 0 ||
            Number(quantity) <= 0
        ) {
            setError("All values must be positive numbers.");
            return;
        }
        setError("");
        // Pass the parameters to the parent component
        onCreateBoxes({
            width: Number(width),
            height: Number(height),
            depth: Number(depth),
            quantity: Number(quantity),
        });
        // Optionally clear the fields after submission
        setFields({ width: "", height: "", depth: "", quantity: "" });
        },
        [fields, onCreateBoxes]
    );

    return (
        <div style={styles.sidebar}>
        <h3>Warehouse Box Creator</h3>
        <form onSubmit={handleSubmit} style={styles.form}>
            <label style={styles.label}>
            Width:
            <input
                type="number"
                name="width"
                value={fields.width}
                onChange={handleChange}
                placeholder="Width"
                style={styles.input}
                required
            />
            </label>
            <label style={styles.label}>
            Height:
            <input
                type="number"
                name="height"
                value={fields.height}
                onChange={handleChange}
                placeholder="Height"
                style={styles.input}
                required
            />
            </label>
            <label style={styles.label}>
            Depth:
            <input
                type="number"
                name="depth"
                value={fields.depth}
                onChange={handleChange}
                placeholder="Depth"
                style={styles.input}
                required
            />
            </label>
            <label style={styles.label}>
            Quantity:
            <input
                type="number"
                name="quantity"
                value={fields.quantity}
                onChange={handleChange}
                placeholder="Number of Boxes"
                style={styles.input}
                required
            />
            </label>
            {error && <p style={styles.error}>{error}</p>}
            <button type="submit" style={styles.button}>
            Create Boxes
            </button>
        </form>
        <button onClick={onSolve} style={{ ...styles.button, marginTop: "10px" }}>
            Solve
        </button>
        </div>
    );
    };

    const styles = {
    sidebar: {
        width: "250px",
        backgroundColor: "#f8f8f8",
        padding: "15px",
        borderRight: "1px solid #ddd",
        boxSizing: "border-box",
    },
    form: {
        display: "flex",
        flexDirection: "column",
    },
    label: {
        marginBottom: "10px",
        fontSize: "14px",
    },
    input: {
        width: "100%",
        padding: "6px 8px",
        marginTop: "4px",
        boxSizing: "border-box",
    },
    button: {
        padding: "10px",
        backgroundColor: "#1976d2",
        color: "#fff",
        border: "none",
        cursor: "pointer",
    },
    error: {
        color: "red",
        fontSize: "12px",
    },
    };

    export default InputBar;
