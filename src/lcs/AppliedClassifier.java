package com.clawsoftware.agentsimulator.lcs;

/**
 *
 * The correctly rotated classifier with the absolute direction
 * Only used to determine the action
 * 
 * @author Clemens Lode, clemens at lode.de, University Karlsruhe (TH)
 */
public class AppliedClassifier {
    
    /**
     * Pointer to the original classifier
     */
    private final Classifier originalClassifier;
    
    /**
     * The absolute direction that was executed with this classifier
     */
    private final int absoluteDirection;
    
    // TODO evtl numerosity hier auch, bei mehreren Directions... ??
    
    /**
     * @param original_classifier The original classifier that was used to determine the action
     * @param absolute_direction The absolute direction that was taken in connection with the original classifier
     */
    public AppliedClassifier(final Classifier original_classifier, int absolute_direction) {
        originalClassifier = original_classifier;
        absoluteDirection = absolute_direction;
    }

    /**
     * @return The classifier that was used to construct this applied classifier and action
     */
    public final Classifier getOriginalClassifier() {
        return originalClassifier;
    }

    /**
     * @return the real absolute direction of this applied classifer, i.e. the actual action
     */
    public final int getAbsoluteDirection() {
        return absoluteDirection;
    }
    
    @Override
    public String toString() {
        return originalClassifier.toString() + " ==> " + absoluteDirection;
    }
    
}
