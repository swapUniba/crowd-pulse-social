package com.github.frapontillo.pulse.crowd.social.util;

import com.github.frapontillo.pulse.crowd.data.entity.Message;
import com.github.frapontillo.pulse.crowd.social.extraction.ExtractionParameters;
import com.github.frapontillo.pulse.util.StringUtil;
import rx.functions.Func1;

/**
 * @author Francesco Pontillo
 */
public class Checker {
    public static Func1<Message, Boolean> checkNonNullMessage() {
        return message -> (!StringUtil.isNullOrEmpty(message.getText()));
    }

    public static Func1<Message, Boolean> checkQuery(final ExtractionParameters parameters) {
        return message -> parameters.getQuery() == null || parameters.getQuery().size() == 0 ||
                StringUtil.containsAnyString(message.getText(), parameters.getQuery());
    }

    public static Func1<Message, Boolean> checkFromUser(final ExtractionParameters parameters) {
        return message -> (StringUtil.isNullOrEmpty(parameters.getFrom()) ||
                parameters.getFrom().equals(message.getFromUser()));
    }

    public static Func1<Message, Boolean> checkToUser(final ExtractionParameters parameters) {
        return message -> (StringUtil.isNullOrEmpty(parameters.getTo()) ||
                message.getToUsers().contains(parameters.getTo()));
    }

    public static Func1<Message, Boolean> checkReferencedUsers(
            final ExtractionParameters parameters) {
        return message -> {
            // if no referenced users are requested
            if (parameters.getReferences() == null || parameters.getReferences().size() <= 0) {
                return true;
            }
            // for each ref user to check
            for (String user : parameters.getReferences()) {
                // if the message does not contain it, return false
                if (!message.getRefUsers().contains(user)) {
                    return false;
                }
            }
            return true;
        };
    }

    public static Func1<Message, Boolean> checkUntilDate(final ExtractionParameters parameters) {
        return message -> parameters.getUntil() == null ||
                message.getDate().compareTo(parameters.getUntil()) <= 0;
    }

    public static Func1<Message, Boolean> checkSinceDate(final ExtractionParameters parameters) {
        return message -> parameters.getSince() == null ||
                message.getDate().compareTo(parameters.getSince()) >= 0;
    }

    public static Func1<Message, Boolean> checkLanguage(final ExtractionParameters parameters) {
        return message -> parameters.getLanguage() == null ||
                parameters.getLanguage().equals(message.getLanguage());
    }

    public static Func1<Message, Boolean> checkLocation(final ExtractionParameters parameters) {
        return message -> parameters.getGeoLocationBox() == null || parameters.getGeoLocationBox()
                .contains(message.getLongitude(), message.getLatitude());
    }
}
