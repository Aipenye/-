// jest.config.js
module.exports = {
  testEnvironment: "jsdom",
  setupFilesAfterEnv: ["<rootDir>/jest.setup.js"],
  moduleNameMapper: {
    "\\.(css|sass|scss)$": "<rootDir>/__mocks__/styleMock.js",
    "^@react-three/fiber$": "<rootDir>/__mocks__/reactThreeFiberMock.js",
    "^@react-three/cannon$": "<rootDir>/__mocks__/reactThreeCannonMock.js",
    "\\.(mp3|wav|ogg|png|svg)$": "<rootDir>/__mocks__/fileMock.js",
  },
};
