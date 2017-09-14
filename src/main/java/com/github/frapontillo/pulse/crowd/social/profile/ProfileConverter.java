package com.github.frapontillo.pulse.crowd.social.profile;

import com.github.frapontillo.pulse.crowd.data.entity.Profile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Francesco Pontillo
 */
public abstract class ProfileConverter<T> {
    private final ProfileParameters parameters;

    public ProfileConverter(ProfileParameters parameters) {
        this.parameters = parameters;
    }

    protected abstract Profile fromSpecificExtractor(T original,
            HashMap<String, Object> additionalData);

    public Profile fromExtractor(T original, HashMap<String, Object> additionalData) {
        Profile converted = fromSpecificExtractor(original, additionalData);
        if (parameters != null) {
            converted.setSource(parameters.getSource());
            converted.setCustomTags(parameters.getTags());
        }
        return converted;
    }

    public List<Profile> fromExtractor(List<T> originalList) {
        List<Profile> profileList = new ArrayList<>(originalList.size());
        return addFromExtractor(originalList, profileList);
    }

    public <L extends List> List<Profile> addFromExtractor(L originalList, List<Profile> addToList,
            HashMap<String, Object> additionalData) {
        for (Object original : originalList) {
            addToList.add(fromExtractor((T) original, additionalData));
        }
        return addToList;
    }

    public <L extends List> List<Profile> addFromExtractor(L originalList,
            List<Profile> addToList) {
        return addFromExtractor(originalList, addToList, null);
    }
}
