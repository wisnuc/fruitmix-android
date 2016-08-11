package com.winsun.fruitmix.model;

/**
 * Created by Administrator on 2016/7/8.
 */
public class OfflineTask {

    public enum OperationType {
        CREATE, UPDATE, DELETE
    }

    public enum HttpType {
        GET, POST, PATCH
    }

    private int id;
    private OperationType operationType;
    private HttpType httpType;
    private String request;
    private String data;
    private int operationCount;

    public OfflineTask(int id, OperationType operationType, HttpType httpType, String request, String data, int operationCount) {
        this.id = id;
        this.operationType = operationType;
        this.httpType = httpType;
        this.request = request;
        this.data = data;
        this.operationCount = operationCount;
    }

    public OfflineTask() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public HttpType getHttpType() {
        return httpType;
    }

    public void setHttpType(HttpType httpType) {
        this.httpType = httpType;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(OperationType operationType) {
        this.operationType = operationType;
    }

    public int getOperationCount() {
        return operationCount;
    }

    public void setOperationCount(int operationCount) {
        this.operationCount = operationCount;
    }
}
