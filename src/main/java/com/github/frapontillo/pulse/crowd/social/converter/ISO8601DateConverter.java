package com.github.frapontillo.pulse.crowd.social.converter;

import com.beust.jcommander.IStringConverter;
import org.joda.time.DateTime;

import java.util.Date;

/**
 * @author Francesco Pontillo
 */
public class ISO8601DateConverter implements IStringConverter<Date> {
    @Override public Date convert(String value) {
        if (value == null) {
            return null;
        }
        return new DateTime(value).toDate();
    }
}