package de.tum.in.net.group17.onion.interfaces.authentication;

/**
 * Created by Marko Dorfhuber(PraMiD) on 11.08.17.
 */
public class AuthException extends Throwable {
    private String msg;

    /**
     * Create a new auth exception with the given message.
     *
     * @param message A message describing the error.
     */
    public AuthException(String message) {
        this.msg = message;
    }

    /**
     * @inheritDoc
     */
    @Override
    public String getMessage() {
        return this.msg;
    }
}
