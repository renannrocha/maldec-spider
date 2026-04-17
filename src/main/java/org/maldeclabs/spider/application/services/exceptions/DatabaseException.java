package org.maldeclabs.spider.application.services.exceptions;

public class DatabaseException extends RuntimeException{
    private static final Long serialVersionUID = 1L;

    public DatabaseException(String msg){
        super(msg);
    }
}
