package com.taskadapter.redmineapi;

public class NotFoundException extends RedMineException {

    private static final long serialVersionUID = 1L;

    public NotFoundException(String msg) {
        super(msg);
    }
}