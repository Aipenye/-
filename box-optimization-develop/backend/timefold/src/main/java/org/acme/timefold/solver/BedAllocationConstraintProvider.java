package org.acme.timefold.solver;

import org.acme.timefold.domain.Box;
import ai.timefold.solver.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintCollectors;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.Joiners;

/*
 Defines all the constraints (rules) for Timefold to evaluate solutions.
 This guides the solver in finding legal and optimal box placements.
 */
public class BedAllocationConstraintProvider implements ConstraintProvider {

    // This method collects all constraints the solver should apply.
    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                noOverlap(constraintFactory), // Prevent boxes from overlapping
                notOutOfBoundsX(constraintFactory), // Ensure boxes stay within X dimension
                notOutOfBoundsY(constraintFactory), // Ensure boxes stay within Y dimension
                notOutOfBoundsZ(constraintFactory), // Ensure boxes stay within Z dimension
                notInPathwaysXdir(constraintFactory), // Ensure boxes do not enter the paths that span an x-range
                notInPathwaysZdir(constraintFactory), // Ensure boxes do not enter the paths that span a z-range

                // Optional stability constraint to keep boxes supported
                stability(constraintFactory),

                // Soft constraint: encourage packing the container fully
                maximizeFilledVolume(constraintFactory),

                // restrict moving of locked boxes constraint
                lockedBoxes(constraintFactory),
        };
    }

    /**
     * Hard Constraint: Prevents any two boxes from overlapping in 3D space.
     * Any hard constraint should never be broken by timefold.
     */

    public Constraint noOverlap(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachUniquePair(Box.class) // All unique box pairs
                .filter((box1, box2) -> box1.getAirCube() != null && box2.getAirCube() != null)
                // Check for X-axis overlap using AABB logic
                .filter((box1,
                        box2) -> Math.max(box1.getX(), box2.getX()) < Math.min(box1.getX() + box1.getLength(),
                                box2.getX() + box2.getLength()))
                // Check for Y-axis overlap
                .filter((box1,
                        box2) -> Math.max(box1.getY(), box2.getY()) < Math.min(box1.getY() + box1.getHeight(),
                                box2.getY() + box2.getHeight()))
                // Check for Z-axis overlap
                .filter((box1,
                        box2) -> Math.max(box1.getZ(), box2.getZ()) < Math.min(box1.getZ() + box1.getWidth(),
                                box2.getZ() + box2.getWidth()))
                // Penalize overlap violations with a hard score
                .penalize(HardMediumSoftScore.ONE_HARD)
                .asConstraint("noOverlap");
    }

    /*
     * Hard Constraint: Prevents box from going outside the container's X-axis,
     * Y-axis, and Z-axis
     */

    public Constraint notOutOfBoundsX(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Box.class)
                .filter(box -> box.getAirCube() != null)
                .filter(box -> box.getAirCube().getX() + box.getLength() > box.getContainer().getXbound())
                .penalize(HardMediumSoftScore.ONE_HARD)
                .asConstraint("notOutOfBoundsX");
    }

    public Constraint notOutOfBoundsY(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Box.class)
                .filter(box -> box.getAirCube() != null)
                .filter(box -> box.getAirCube().getY() + box.getHeight() > box.getContainer().getYbound())
                .penalize(HardMediumSoftScore.ONE_HARD)
                .asConstraint("notOutOfBoundsY");
    }

    public Constraint notOutOfBoundsZ(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Box.class)
                .filter(box -> box.getAirCube() != null)
                .filter(box -> box.getAirCube().getZ() + box.getWidth() > box.getContainer().getZbound())
                .penalize(HardMediumSoftScore.ONE_HARD)
                .asConstraint("notOutOfBoundsZ");
    }
    /*
     * Hard Constraint: prevent boxes from being placed in pathways
     */
    public Constraint notInPathwaysXdir(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Box.class)
                .filter(box -> box.getAirCube() != null)
                .filter(box -> box.inPathX(box.getAirCube().getContainer().getX_paths()))
                .penalize(HardMediumSoftScore.ONE_HARD)
                .asConstraint("notInPathwaysXdir");
    }
    public Constraint notInPathwaysZdir(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Box.class)
                .filter(box -> box.getAirCube() != null)
                .filter(box -> box.inPathZ(box.getAirCube().getContainer().getZ_paths()))
                .penalize(HardMediumSoftScore.ONE_HARD)
                .asConstraint("notInPathwaysZdir");
    }

    /*
     * Soft Constraint: Reward placing boxes to maximize used volume.
     * A soft constraint is the lowest priority, is what should be optimized over as long
     * as no hard and medium constraints are broken.
     */
    public Constraint maximizeFilledVolume(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Box.class)
                .filter(box -> box.getAirCube() != null)
                // Reward based on total volume of all boxes that have been assigned in the space
                .reward(HardMediumSoftScore.ONE_SOFT, box -> box.getLength() * box.getHeight() * box.getWidth() * (box.getContainer().getYbound() - box.getY()))
                .asConstraint("maximizeFilledVolume");

    }

    /*
     * Medium Constraint: Penalizes boxes that are not stable (not supported from
     * below).
     * A medium constraint should only be broken when absolutely necessary. 
     */
    public Constraint stability(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Box.class)
                .join(constraintFactory.forEach(Box.class),
                        // Ensure we're considering pairs of distinct boxes with non-null AirCubes.
                        // non-null aircube means the box has not been assigned, i.e., there was no space for it.
                        Joiners.filtering(
                                (box1, box2) -> box1 != box2 && box1.getAirCube() != null && box2.getAirCube() != null))
                .filter((box1, box2) -> box1.getY() > box2.getY())

                // Group by the first box and sum up the "standing on" values from the joined
                // pair. Standing on value is the amount of squares of the surface area of a box
                // that is supported by other boxes.
                .groupBy((box1, box2) -> box1, ConstraintCollectors.sum((box1, box2) -> Box.isStandingOn(box1, box2)))
                
                // Filter to only those boxes that are unstable and also not on the ground.
                .filter((box, standingOnCount) -> box.getY() > 0 && !box.isStable(standingOnCount))
                .penalize(HardMediumSoftScore.ONE_MEDIUM,
                        (box, standingOnCount) -> box.getLength() * box.getWidth() - standingOnCount)
                .asConstraint("stability");

    }

    public Constraint lockedBoxes(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Box.class)
                .filter(box -> box.isLocked())
                .filter(box -> !box.isInLockedPosition())
                .penalize(HardMediumSoftScore.ONE_MEDIUM)
                .asConstraint("lockedBoxes");
    }
}