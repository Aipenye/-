// __mocks__/reactThreeFiberMock.js
module.exports = {
  // stub out the call that spring/drei expects:
  addEffect: () => {},

  // if you use useFrame/useThree in your components under test:
  useFrame: () => {},
  useThree: () => ({ camera: {}, gl: {} }),
};
