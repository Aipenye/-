const express = require("express");
const app = express();
const PORT = 5000;

app.use(express.json()); // Parse JSON requests

// POST route to calculate the 3D model based on box data
app.post("/calculate", (req, res) => {
  const { boxes } = req.body;

  console.log("Boxes received:", boxes);

  // Generate 3D model data (for example, box positions, transformations, etc.)
  const modelData = boxes.map((box) => {
    return {
      id: box.id,
      position: [box.width * 2, box.height * 2, box.depth * 2], // Example transformation
      color: "lightblue",
      // Add any other data for your 3D model here
    };
  });

  // Respond with the generated model data
  res.status(200).json(modelData);
});

app.listen(PORT, () => {
  console.log(`Server running on http://localhost:${PORT}`);
});
