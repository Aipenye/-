# React WebApp – Warehouse and Box Visualization Tool

This project is a React-based frontend developed using Vite for fast bundling and Jest for testing. The app visualizes warehouse and box data, supports user-defined inputs, and includes a clean modular architecture that makes it easy to contribute and extend.

# React + Vite

This template provides a minimal setup to get React working in Vite with HMR and some ESLint rules.

Currently, two official plugins are available:

- [@vitejs/plugin-react](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react/README.md) uses [Babel](https://babeljs.io/) for Fast Refresh
- [@vitejs/plugin-react-swc](https://github.com/vitejs/vite-plugin-react-swc) uses [SWC](https://swc.rs/) for Fast Refresh


# System Prerequisites for LInux

Make sure you have NodeJS and npm:

```
sudo apt install nodejs
sudo apt install npm
```

Then run the command from the directory `~/XLRIT-optimization-2025/webapp`:
`npm install react`
`npm run build`
`npm install --save-dev jest @testing-library/react @testing-library/jest-dom @react-three test-renderer `
`npm install --save-dev cypress`


Launch the website with command:

`npm run dev`


- `index.html`**: Static page that exposes a `<div id="root">` mount-point for the bundled React app.
- `main.jsx`**: Boots the app by calling `createRoot(...).render(<App/>)` and brings in global styles.
- `App.jsx`**: Orchestrates everything; left panel collects user input, right panel renders a Three.js scene that updates whenever `modelData` changes.
- `Objects.jsx`**: Houses low-level 3-D primitives (`Box`, `Warehouse`, floor/walls) built with @react-three/fiber and cannon physics.
- `input-logic/input-manager.jsx`**: Form wizard that parses warehouse/box fields, posts them to the backend, handles success/error UI, and hands parsed `modelData` up to `<App/>`.
- `input-logic/item-manager.jsx`**: Subcomponent that lets users add/delete multiple boxes (width × length × height × quantity) and validates each entry.
- `server.js`**: Tiny Express mock API (`/calculate`) that echoes back trivial 3-D positions so the frontend has something to visualise during local development.
- `src/test/BoxesFromData.test.jsx`**: Verifies each data row spawns exactly one mocked `<Box>` and checks edge-cases (zero-size, negative dimensions, oversized boxes).
- `src/test/WarehouseFromData.test.jsx`**: Ensures `WarehouseFromData` passes width, height, length, and front-bottom-left props unchanged to the mocked `<Warehouse>`.
- `vite.config.js`**: Tells Vite how to bundle the React app with hot-module reload.
- `jest.config.js` / `jest.setup.js`**: Configure Jest and React Testing Library globals for all tests.
- CSS files (`App.css`, `styles.css`, etc.)**: Provide layout (split-pane UI) and light design tweaks for inputs, buttons, and 3-D canvas wrapper.


Windows Install Node.js and NPM on Windows
https://phoenixnap.com/kb/install-node-js-npm-on-windows 

# Testing with Jest

Install 



This project uses Jest for unit testing.
To run `npm test`

`~/src/test/`:
This folder contains all the unit tests for validating key React components using Jest.

BoxesFromData.test.jsx
- Renders the correct number of <Box> components
- Correctly computes their 3D position values
- Passes through all expected props (dimensions, position, color)
- Gracefully handles edge cases like 0-dimension, negative sizes, or oversized boxes
WarehouseFromData.test.jsx
- Ensures your WarehouseFromData helper doesn’t reorder or mis-map any of its inputs.

