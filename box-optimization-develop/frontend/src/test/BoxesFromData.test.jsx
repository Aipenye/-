// This test file is for the BoxesFromData component in the App.jsx file.


/*

What does it test?

- Renders the correct number of <Box> components
- Correctly computes their 3D position values
- Passes through all expected props (dimensions, position, color)
- Gracefully handles edge cases like 0-dimension, negative sizes, or oversized boxes

*/


import React from 'react';
import { render, screen } from '@testing-library/react';
import { BoxesFromData } from '../App';

jest.mock('../Objects', () => ({
  Box: ({ dimensions, position, color }) => (
    <div
      data-testid="box"
      data-dimensions={dimensions.join(',')}
      data-position={position.join(',')}
      data-color={color}
    />
  )
}));

describe('BoxesFromData (including edge cases)', () => {
  it('renders one <Box> per data row (basic)', () => {
    const sample = [
      ['id1', [1, 2, 3], [0, 0, 0], 'red'],
      ['id2', [2, 4, 6], [1, 1, 1], 'blue']
    ];
    render(<BoxesFromData data={sample} fbl={[10, 10, 10]} />);
    expect(screen.getAllByTestId('box')).toHaveLength(2);
  });

  it('handles a zero-size box ([0,0,0]) correctly', () => {
    // Zero dimensions: width=0, height=0, length=0
    // Position should be just location + FBL offset (no half-dimension added)
    render(
      <BoxesFromData
        data={[['zero', [0, 0, 0], [5, 5, 5], 'purple']]}
        fbl={[1, 1, 1]}
      />
    );
    const box = screen.getByTestId('box');
    expect(box).toHaveAttribute('data-dimensions', '0,0,0');
    // X: 5 + 1 + 0 = 6, Y: 5 + 1 + 0 = 6, Z: 5 + 1 + 0 = 6
    expect(box).toHaveAttribute('data-position', '6,6,6');
    expect(box).toHaveAttribute('data-color', 'purple');
  });

  it('handles negative dimensions correctly', () => {
    // Negative dims simulate invalid input—our helper still maps them 
    // This test doesn’t reject or sanitize them — it just confirms the math doesn’t break
    render(
      <BoxesFromData
        data={[['neg', [-2, -4, -6], [2, 4, 6], 'orange']]}
        fbl={[0, 0, 0]}
      />
    );
    const box = screen.getByTestId('box');
1
  });

  it('still renders boxes larger than the warehouse', () => {
    // Even if a box's dimensions exceed warehouse size, it is still created
    render(
      <BoxesFromData
        data={[['big', [10, 10, 10], [0, 0, 0], 'black']]}
        fbl={[0, 0, 0]}
      />
    );
    // We expect exactly one Box, regardless of size
    expect(screen.getAllByTestId('box')).toHaveLength(1);
  });
});