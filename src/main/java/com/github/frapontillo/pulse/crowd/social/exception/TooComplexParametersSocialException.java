package com.github.frapontillo.pulse.crowd.social.exception;

/**
 * @author Francesco Pontillo
 */
public class TooComplexParametersSocialException extends InvalidParametersSocialException {
    public TooComplexParametersSocialException(String message) {
        super(message);
    }

    public TooComplexParametersSocialException(long maximumQuantity, long actualQuantity) {
        this("The maximum parameter number for this extractor is " + maximumQuantity +
                ", while the number of actual parameters was " + actualQuantity + ".");
    }
}
