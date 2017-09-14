package com.github.frapontillo.pulse.crowd.social.converter;

import com.beust.jcommander.IStringConverter;
import com.github.frapontillo.pulse.crowd.social.extraction.GeoLocationBox;

/**
 * @author Francesco Pontillo
 */
public class GeoLocationBoxConverter implements IStringConverter<GeoLocationBox> {
    @Override public GeoLocationBox convert(String value) {
        if (value == null) {
            return null;
        }
        try {
            String[] components = value.split(",");
            return new GeoLocationBox(Double.parseDouble(components[1]),
                    Double.parseDouble(components[0]), Double.parseDouble(components[2]));
        } catch (Exception ignored) {
        }
        return null;
    }
}
