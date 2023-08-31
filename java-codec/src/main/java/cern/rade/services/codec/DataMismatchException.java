package cern.rade.services.codec;

import java.io.IOException;

public class DataMismatchException extends Exception {
    public DataMismatchException(String message, Throwable error){
        super(message, error);
    }
    public DataMismatchException(String message){
        super(message);
    }
}
