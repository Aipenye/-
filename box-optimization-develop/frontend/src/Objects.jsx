import React from 'react'
import { useBox, usePlane } from '@react-three/cannon'

//constructs Box
function Box({ color, dimensions, position }) {
  const [ref] = useBox(() => ({ type: "static", mass: 1, args: dimensions, position }));

  return (
    <mesh ref={ref}>
      <boxGeometry args={dimensions} />
      <meshStandardMaterial color={color} transparent opacity={0.7} />
      {/* <meshBasicMaterial color="gray" wireframe /> */}
    </mesh>
  )
}
// function Box({ color, dimensions, position }) {
//   const [ref] = useBox(() => ({ mass: 1, args: dimensions, position }));

//   return (
//     <group ref={ref} position={position}>
//       {/* Transparent box body */}
//       <mesh>
//         <boxGeometry args={dimensions} />
//         <meshStandardMaterial color="skyblue" transparent opacity={0.3} />
//       </mesh>

//       {/* Bold outline using wireframe */}
//       <mesh>
//         <boxGeometry args={dimensions} />
//         <meshBasicMaterial color="black" wireframe />
//       </mesh>
//     </group>
//   );
// }

//construct floor
function Floor({ position, args }) {
  const [ref] = usePlane(() => ({ rotation: [-Math.PI / 2, 0, 0], position }));
  return (
    <mesh ref={ref}>
      <planeGeometry args={args} />
      <meshStandardMaterial color="lightgray" transparent opacity={0.6} />
    </mesh>
  );
}

//construct ceiling
function Ceiling({ position, size, color = "white", opacity = 0.5 }) {
  const [ref] = usePlane(() => ({ position, rotation: [Math.PI / 2, 0, 0] }));
  return (
    <mesh ref={ref}>
      <planeGeometry args={size} />
      <meshStandardMaterial color={color} transparent opacity={opacity} />
    </mesh>
  );
}

//construct wall
function Wall({ position, rotation, args }) {
  const [ref] = usePlane(() => ({ position, rotation }));
  return (
    <mesh ref={ref}>
      <planeGeometry args={args} />
      <meshStandardMaterial color="white" side={2} transparent opacity={0.1} />
    </mesh>
  );
}

/**
 * Warehouse component that builds a room based on the passed parameters.
 *
 * @param {Object} props
 * @param {number} props.width  - The overall width of the warehouse.
 * @param {number} props.height - The overall height of the warehouse.
 * @param {number} props.length - The overall length (depth) of the warehouse.
 */
function Warehouse({ width, height, length, frontBottomLeft }) {
  const [fbl_x, fbl_y, fbl_z] = frontBottomLeft; // Unpack FBL coordinates

  return (
    <>
      {/* Floor at the base */}
      <Floor position={[fbl_x + width / 2, fbl_y, fbl_z + length / 2]} args={[width, length]} />
      
      {/* Back Wall at z = fbl_z */}
      <Wall position={[fbl_x + width / 2, fbl_y + height / 2, fbl_z]} rotation={[0, 0, 0]} args={[width, height]} />
      
      {/* Front Wall at z = fbl_z + length */}
      <Wall position={[fbl_x + width / 2, fbl_y + height / 2, fbl_z + length]} rotation={[0, Math.PI, 0]} args={[width, height]} />
      
      {/* Left Wall at x = fbl_x */}
      <Wall position={[fbl_x, fbl_y + height / 2, fbl_z + length / 2]} rotation={[0, Math.PI / 2, 0]} args={[length, height]} />
      
      {/* Right Wall at x = fbl_x + width */}
      <Wall position={[fbl_x + width, fbl_y + height / 2, fbl_z + length / 2]} rotation={[0, -Math.PI / 2, 0]} args={[length, height]} />
      
      {/* Ceiling at the top */}
      <Ceiling position={[fbl_x + width / 2, fbl_y + height, fbl_z + length / 2]} size={[width, length]} />
    </>
  );
}

const Objects = { Box, Warehouse }
export default Objects
