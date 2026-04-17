package org.maldeclabs.spider.gateways.rest;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

public class ApiErrors {

    @Getter
    public List<String> errors;

    public ApiErrors(List<String> errors){
        this.errors = errors;
    }

    public ApiErrors(String messagemErro){
        this.errors = Arrays.asList(messagemErro);
    }
}
