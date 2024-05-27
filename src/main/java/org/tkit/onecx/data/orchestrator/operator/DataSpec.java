package org.tkit.onecx.data.orchestrator.operator;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataSpec {

    @JsonProperty("orgId")
    private String orgId;

    @JsonProperty("key")
    private String key;

    @JsonProperty("description")
    private String description;

    @JsonProperty("productName")
    private String productName;

    @JsonProperty("appId")
    private String appId;

    @JsonProperty("data")
    private String data;

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "DataSpec{" +
                ", key='" + key + '\'' +
                ", productName='" + productName + '\'' +
                ", appId='" + appId + '\'' +
                '}';
    }
}
