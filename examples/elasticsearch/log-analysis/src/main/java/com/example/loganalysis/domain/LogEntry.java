package com.example.loganalysis.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;

@Document(indexName = "logs-#{T(java.time.LocalDate).now().toString()}")
public class LogEntry {

    @Id
    private String id;

    @Field(name = "@timestamp", type = FieldType.Date)
    private Instant timestamp;

    @Field(type = FieldType.Keyword)
    private String level;

    @Field(type = FieldType.Keyword)
    private String logger;

    @Field(type = FieldType.Keyword)
    private String thread;

    @Field(type = FieldType.Text)
    private String message;

    @Field(type = FieldType.Text)
    private String exception;

    @Field(type = FieldType.Text, index = false)
    private String stackTrace;

    @Field(type = FieldType.Keyword)
    private String application;

    @Field(type = FieldType.Keyword)
    private String environment;

    @Field(type = FieldType.Keyword)
    private String host;

    @Field(type = FieldType.Keyword)
    private String requestId;

    @Field(type = FieldType.Keyword)
    private String userId;

    @Field(type = FieldType.Long)
    private Long durationMs;

    @Field(type = FieldType.Keyword)
    private String httpMethod;

    @Field(type = FieldType.Keyword)
    private String httpPath;

    @Field(type = FieldType.Integer)
    private Integer httpStatus;

    public LogEntry() {}

    // Getters
    public String getId() { return id; }
    public Instant getTimestamp() { return timestamp; }
    public String getLevel() { return level; }
    public String getLogger() { return logger; }
    public String getThread() { return thread; }
    public String getMessage() { return message; }
    public String getException() { return exception; }
    public String getStackTrace() { return stackTrace; }
    public String getApplication() { return application; }
    public String getEnvironment() { return environment; }
    public String getHost() { return host; }
    public String getRequestId() { return requestId; }
    public String getUserId() { return userId; }
    public Long getDurationMs() { return durationMs; }
    public String getHttpMethod() { return httpMethod; }
    public String getHttpPath() { return httpPath; }
    public Integer getHttpStatus() { return httpStatus; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    public void setLevel(String level) { this.level = level; }
    public void setLogger(String logger) { this.logger = logger; }
    public void setThread(String thread) { this.thread = thread; }
    public void setMessage(String message) { this.message = message; }
    public void setException(String exception) { this.exception = exception; }
    public void setStackTrace(String stackTrace) { this.stackTrace = stackTrace; }
    public void setApplication(String application) { this.application = application; }
    public void setEnvironment(String environment) { this.environment = environment; }
    public void setHost(String host) { this.host = host; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }
    public void setHttpMethod(String httpMethod) { this.httpMethod = httpMethod; }
    public void setHttpPath(String httpPath) { this.httpPath = httpPath; }
    public void setHttpStatus(Integer httpStatus) { this.httpStatus = httpStatus; }
}
