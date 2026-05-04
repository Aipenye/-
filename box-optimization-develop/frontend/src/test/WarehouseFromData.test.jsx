// This test file is for the BoxesFromData component in the App.jsx file.
/*
What does it test?
- Ensures your WarehouseFromData helper doesn’t reorder or mis-map any of its inputs.
*/
import React from 'react';
import { render, screen } from '@testing-library/react';
import { WarehouseFromData } from '../App';

// Mock Objects.Warehouse so we can inspect props via data-attributes
jest.mock('../Objects', () => ({
  Warehouse: ({ width, height, length, frontBottomLeft }) => (
    <div
      data-testid="warehouse"
      data-width={width}
      data-height={height}
      data-length={length}
      data-fbl={frontBottomLeft.join(',')}
    />
  )
}));

describe('WarehouseFromData', () => {
  // Test that the helper renders a single Warehouse with correct prop mapping
  it('renders a single Warehouse with correct props', () => {
    const dims = [5, 6, 7];     // width, height, length
    const fbl  = [1, 2, 3];     // front-bottom-left coordinates

    // Render the helper with given dimensions and FBL
    render(<WarehouseFromData data={dims} fbl={fbl} />);

    // Grab the mocked Warehouse <div>
    const wh = screen.getByTestId('warehouse');

    // Verify width, height, length props map correctly
    expect(wh).toHaveAttribute('data-width', '5');
    expect(wh).toHaveAttribute('data-height', '6');
    expect(wh).toHaveAttribute('data-length', '7');
    // Verify frontBottomLeft prop maps correctly
    expect(wh).toHaveAttribute('data-fbl', '1,2,3');
  });
});
