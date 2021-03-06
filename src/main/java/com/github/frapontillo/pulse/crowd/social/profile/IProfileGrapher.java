package com.github.frapontillo.pulse.crowd.social.profile;

import com.github.frapontillo.pulse.crowd.data.entity.Profile;
import com.github.frapontillo.pulse.rx.PulseSubscriber;
import com.github.frapontillo.pulse.spi.IPlugin;
import com.github.frapontillo.pulse.spi.VoidConfig;
import rx.Observable;

import java.util.List;

/**
 * Crowd Pulse plugin that enables fetching of a stream's profiles connections.
 *
 * @author Francesco Pontillo
 */
public abstract class IProfileGrapher extends IPlugin<Profile, Profile, VoidConfig> {

    /**
     * Retrieve the {@link Profile}s connected to a given {@link Profile}.
     * The stream will contain both the original profile and the connected ones.
     * This search is not recursive, which means it will only get one depth of level in the actual
     * graph.
     * <p/>
     * If you want to another level of connections, simply apply this plugin again.
     * At the end of the graph construction, you may want to apply the {@link Observable#distinct()}
     * operator, since profiles can have common connections that are repeated in the stream.
     *
     * @param profile    The {@link Profile} to fetch connections for.
     * @param parameters The {@link ProfileParameters} to use for the conversion.
     *
     * @return A {@link List<Profile>} containing all the connections, excluding the input profile.
     */
    public abstract List<Profile> getConnections(Profile profile, ProfileParameters parameters);

    @Override public Observable.Transformer<Profile, Profile> transform(VoidConfig params) {
        return profileObservable -> profileObservable.distinct(Profile::getUsername)
                .lift(getOperator(params));
    }

    @Override protected Observable.Operator<Profile, Profile> getOperator(VoidConfig parameters) {
        return subscriber -> new PulseSubscriber<Profile>(subscriber) {
            @Override public void onNext(Profile profile) {
                // do not graph profiles with existing connections
                if (profile.getConnections() == null || profile.getConnections().size() == 0) {
                    reportElementAsStarted(profile.getUsername());

                    // build the appropriate profile parameters
                    ProfileParameters params = new ProfileParameters();
                    params.setSource(getName());
                    params.setTags(profile.getCustomTags());
                    // get the profiles and emit them
                    List<Profile> connections = getConnections(profile, params);
                    connections.forEach(
                            connection -> profile.addConnections(connection.getUsername()));

                    reportElementAsEnded(profile.getUsername());
                    connections.forEach(subscriber::onNext);
                }
                subscriber.onNext(profile);
            }

            @Override public void onCompleted() {
                reportPluginAsCompleted();
                super.onCompleted();
            }

            @Override public void onError(Throwable e) {
                reportPluginAsErrored();
                super.onError(e);
            }
        };
    }

    @Override public VoidConfig getNewParameter() {
        return new VoidConfig();
    }
}
