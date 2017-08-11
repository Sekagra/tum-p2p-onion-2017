package de.tum.in.net.group17.onion.interfaces.authentication;

/**
 * Created by Marko Dorfhuber(PraMiD) on 11.08.17.
 */
public class AuthException extends Throwable {
    private String msg;

    public AuthException(String message) {
        this.msg = message;
    }

    @Override
    public String getMessage() {
        return this.msg;
    }
}
