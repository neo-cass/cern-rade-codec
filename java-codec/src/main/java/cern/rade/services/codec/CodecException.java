package cern.rade.services.codec;

public class CodecException extends Exception {
    public CodecException(String message, Throwable error){
        super(message, error);
    }
    public CodecException(String message){
        super(message);
    }
}
