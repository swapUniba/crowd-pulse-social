package com.github.frapontillo.pulse.crowd.social.profile;

/**
 * @author Francesco Pontillo
 */
public class ProfileKey {
    private String source;
    private String id;

    public ProfileKey(String source, String id) {
        this.source = source;
        this.id = id;
    }

    @Override public boolean equals(Object obj) {
        if (!(obj instanceof ProfileKey)) {
            return false;
        }
        ProfileKey ref = (ProfileKey) obj;
        return this.source.equals(ref.source) && this.id.equals(ref.id);
    }

    @Override public int hashCode() {
        return source.hashCode() ^ id.hashCode();
    }
}
