package com.github.frapontillo.pulse.crowd.social.extraction;

import com.github.frapontillo.pulse.crowd.data.entity.Message;
import com.github.frapontillo.pulse.crowd.social.exception.InvalidParametersSocialException;
import com.github.frapontillo.pulse.crowd.social.exception.MissingParametersSocialException;
import com.github.frapontillo.pulse.crowd.social.exception.SocialException;
import com.github.frapontillo.pulse.spi.IPlugin;
import com.github.frapontillo.pulse.util.PulseLogger;
import com.github.frapontillo.pulse.util.StringUtil;
import org.apache.logging.log4j.Logger;
import rx.Observable;
import rx.Subscriber;
import rx.observers.SafeSubscriber;

import java.util.List;

/**
 * @author Francesco
 */
public abstract class IExtractor extends IPlugin<Void, Message, ExtractionParameters> {

    private final Logger logger = PulseLogger.getLogger(IExtractor.class);

    /**
     * Returns the maximum number of parameters that this extractor supports per each query.
     *
     * @return {@link long} the maximum number of parameters per query.
     */
    public abstract long getMaximumQueryParameters();

    /**
     * Check if the extractor supports regular text queries.
     *
     * @return {@link boolean} true if there is support for text queries.
     */
    public abstract boolean getSupportQuery();

    /**
     * Check if the extractor supports searching with geolocation parameters
     * (usually latitude, longitude, radius).
     *
     * @return {@link boolean} true if there is support for geolocation.
     */
    public abstract boolean getSupportGeoLocation();

    /**
     * Check if the extractor supports searching for messages sent from a specific social network
     * user (implementation-dependent).
     *
     * @return {@link boolean} true if there is support for origin searching.
     */
    public abstract boolean getSupportFrom();

    /**
     * Check if the extractor supports searching for messages sent to a specific social network
     * user
     * (implementation-dependent).
     *
     * @return {@link boolean} true if there is support for destination searching.
     */
    public abstract boolean getSupportTo();

    /**
     * Check if the extractor supports searching for messages that reference a specific social
     * network user (implementation-dependent).
     *
     * @return {@link boolean} true if there is support for reference searching.
     */
    public abstract boolean getSupportReference();

    /**
     * Check if the extractor supports searching for messages sent since a specific date.
     *
     * @return {@link boolean} true if there is support for starting date searching.
     */
    public abstract boolean getSupportSince();

    /**
     * Check if the extractor supports searching for messages sent until a specific date.
     *
     * @return {@link boolean} true if there is support for ending date searching.
     */
    public abstract boolean getSupportUntil();

    /**
     * Check if the extractor supports searching for messages written in a specific
     * language.
     *
     * @return {@link boolean} true if there is support for language searching.
     */
    public abstract boolean getSupportLanguage();

    /**
     * Check if the extractor supports searching for messages written in a specific language
     * locale.
     *
     * @return {@link boolean} true if there is support for language locale searching.
     */
    public abstract boolean getSupportLocale();

    /**
     * Check if the extractor needs the author user OR the recipient user to be specified.
     * Please note that this is an inclusive OR.
     *
     * @return {@link boolean} true if the extractor needs the author or the recipient user.
     */
    public abstract boolean mustSpecifyToOrFrom();

    /**
     * Validate some extraction parameters, returning true if they are valid for the current
     * implementation of {@link IExtractor}, or throwing a {@link SocialException}.
     * IMPORTANT: this is a pre-validation technique, it may rely on obsolete information.
     *
     * @param parameters Some {@link ExtractionParameters} set.
     *
     * @return true if the parameters are valid.
     * @throws SocialException if a parameter is invalid.
     */
    public boolean validateParameters(ExtractionParameters parameters) throws SocialException {
        validateParameter("query", parameters.getQuery(), getSupportQuery());
        validateParameter("geolocation", parameters.getGeoLocationBox(), getSupportGeoLocation());
        validateParameter("from", parameters.getFrom(), getSupportFrom());
        validateParameter("to", parameters.getTo(), getSupportTo());
        validateParameter("reference", parameters.getReferences(), getSupportReference());
        validateParameter("since", parameters.getSince(), getSupportSince());
        validateParameter("until", parameters.getUntil(), getSupportUntil());
        validateParameter("language", parameters.getLanguage(), getSupportLanguage());
        validateParameter("locale", parameters.getLocale(), getSupportLocale());
        if (mustSpecifyToOrFrom() &&
                StringUtil.isNullOrEmpty(parameters.getFrom()) &&
                StringUtil.isNullOrEmpty(parameters.getTo())) {
            throw new MissingParametersSocialException(
                    "You must specify at least one among \"from\" and \"to\".");
        }
        return true;
    }

    private boolean validateParameter(String parameterName, String parameter, boolean isSupported)
            throws InvalidParametersSocialException {
        if (!StringUtil.isNullOrEmpty(parameter) && !isSupported) {
            throwErrorForInvalidParameter(parameterName);
        }
        return true;
    }

    private boolean validateParameter(String parameterName, Object parameter, boolean isSupported)
            throws InvalidParametersSocialException {
        if (parameter != null && !isSupported) {
            throwErrorForInvalidParameter(parameterName);
        }
        return true;
    }

    private boolean validateParameter(String parameterName, List parameter, boolean isSupported)
            throws InvalidParametersSocialException {
        if (parameter != null && parameter.size() > 0 && !isSupported) {
            throwErrorForInvalidParameter(parameterName);
        }
        return true;
    }

    private void throwErrorForInvalidParameter(String parameterName)
            throws InvalidParametersSocialException {
        throw new InvalidParametersSocialException(
                String.format("You cannot specify the \"%s\" parameter.", parameterName));
    }

    /**
     * Starts an asynchronous search loading an {@link rx.Observable} of {@link Message} that will
     * be populated as results come in.
     *
     * @param parameters {@link ExtractionParameters} to search for.
     *
     * @return {@link rx.Observable<Message>}
     */
    protected abstract Observable<Message> getMessages(ExtractionParameters parameters);

    @Override
    protected Observable.Operator<Message, Void> getOperator(ExtractionParameters parameters) {
        return subscriber -> new SafeSubscriber<>(new Subscriber<Object>() {
            @Override public void onCompleted() {
                parameters.setSource(getName());
                getMessages(parameters).subscribe(subscriber);
            }

            @Override public void onError(Throwable e) {
                logger.error("Error before extracting messages.", e);
                subscriber.onError(e);
            }

            @Override public void onNext(Object o) {
                // ignore this, we're going to send only generated Messages
            }
        });
    }

    @Override public ExtractionParameters getNewParameter() {
        return new ExtractionParameters();
    }

}
