package de.tum.in.net.group17.onion.parser;

/**
 * Thrown if an exception occured during parsing of messages.
 * We do not use the ParseException of the Java library because we want to use RuntimeExceptions.
 *
 * Created by Marko Dorfhuber(PraMiD) on 27.05.17.
 */
public class ParsingException extends Exception {
    private final String message;

    /**
     * Create a new ParsingException with the given message.
     *
     * @param msg A message describing the error.
     */
    public ParsingException(String msg) {
        this.message = msg;
    }

    /**
     * @inheritDoc
     */
    @Override
    public String getMessage() {
        return message;
    }
}
