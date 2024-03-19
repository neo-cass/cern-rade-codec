package cern.rade.services.codec;

import cern.rade.services.common.containers.java.ContainerException;

public class CodecException extends Exception {
    public CodecException(String message, Throwable error){
        super(message, error);
    }
    public CodecException(String message){
        super(message);
    }
}
