package com.github.frapontillo.pulse.crowd.social.profile;

import com.github.frapontillo.pulse.crowd.data.entity.Message;
import com.github.frapontillo.pulse.crowd.data.entity.Profile;
import com.github.frapontillo.pulse.rx.RxUtil;
import com.github.frapontillo.pulse.spi.IPlugin;
import rx.Observable;
import rx.Subscriber;
import rx.observers.SafeSubscriber;

import java.util.List;

/**
 * Crowd Pulse plugin interface to retrieve a stream of {@link Profile}s starting from a stream of
 * {@link Message}s.
 *
 * @author Francesco Pontillo
 */
public abstract class IProfiler extends IPlugin<Message, Profile, ProfileParameters> {

    /**
     * Gets a {@link List} of {@link Profile}s from the given parameters.
     *
     * @param parameters The input {@link ProfileParameters} containing the information to retrieve
     *                   the profiles.
     *
     * @return A {@link List} of {@link Profile}s retrieved from the current implementation.
     */
    public abstract List<Profile> getProfiles(ProfileParameters parameters)
            throws ProfilerException;

    @Override public Observable.Transformer<Message, Profile> transform(ProfileParameters params) {
        return messageObservable -> messageObservable.map(Message::getFromUser).distinct()
                .buffer(100).lift(new Observable.Operator<List<Profile>, List<String>>() {
                    @Override public Subscriber<? super List<String>> call(
                            Subscriber<? super List<Profile>> subscriber) {
                        return new SafeSubscriber<>(new Subscriber<List<String>>() {
                            @Override public void onCompleted() {
                                subscriber.onCompleted();
                            }

                            @Override public void onError(Throwable e) {
                                subscriber.onError(e);
                            }

                            @Override public void onNext(List<String> profileNames) {
                                ProfileParameters parameters = new ProfileParameters();
                                parameters.setSource(getName());
                                parameters.setProfiles(profileNames);
                                if (params != null) {
                                    parameters.setTags(params.getTags());
                                }
                                profileNames.forEach(IProfiler.this::reportElementAsStarted);
                                List<Profile> profiles = null;
                                try {
                                    profiles = getProfiles(parameters);
                                } catch (ProfilerException e) {
                                    subscriber.onError(e);
                                }
                                profileNames.forEach(IProfiler.this::reportElementAsEnded);
                                subscriber.onNext(profiles);
                            }
                        });
                    }
                }).filter(profile -> (profile != null)).compose(RxUtil.flatten())
                .doOnCompleted(this::reportPluginAsCompleted)
                .doOnError((err) -> reportPluginAsErrored());
    }

    @Override
    protected Observable.Operator<Profile, Message> getOperator(ProfileParameters parameters) {
        // we don't need no operator
        // we don't need no thought control...
        return null;
    }

    @Override public ProfileParameters getNewParameter() {
        return new ProfileParameters();
    }
}
