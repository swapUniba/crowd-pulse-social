package com.github.frapontillo.pulse.crowd.social.extraction;

import com.github.frapontillo.pulse.crowd.data.entity.Message;
import com.github.frapontillo.pulse.rx.PulseSubscriber;
import com.github.frapontillo.pulse.spi.IPlugin;
import com.github.frapontillo.pulse.spi.VoidConfig;
import rx.Observable;

import java.util.List;

/**
 * Crowd Pulse plugin to fetch message replies.
 *
 * @author Francesco Pontillo
 */
public abstract class IReplyExtractor extends IPlugin<Message, Message, VoidConfig> {

    /**
     * Retrieve the replies for the given {@link Message}.
     * The replies should not include the input message, as it will be automatically emitted before
     * the replies.
     *
     * @param message    The {@link Message} to fetch replies for.
     * @param parameters The parameters that will be needed to properly convert the replies.
     *
     * @return A {@link List<Message>} containing all the replies.
     */
    public abstract List<Message> getReplies(Message message, ExtractionParameters parameters);

    @Override protected Observable.Operator<Message, Message> getOperator(VoidConfig parameters) {
        return subscriber -> new PulseSubscriber<Message>(subscriber) {
            @Override public void onNext(Message message) {
                reportElementAsStarted(message.getId());
                ExtractionParameters newParams = new ExtractionParameters();
                newParams.setSource(getName());
                newParams.setTags(message.getCustomTags());
                List<Message> replies = getReplies(message, newParams);
                reportElementAsEnded(message.getId());
                subscriber.onNext(message);
                replies.forEach(subscriber::onNext);
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
