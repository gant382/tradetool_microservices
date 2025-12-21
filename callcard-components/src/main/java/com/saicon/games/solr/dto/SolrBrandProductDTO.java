package com.saicon.games.solr.dto;

import java.io.Serializable;

public class SolrBrandProductDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String productId;
    private String productName;
    private String brandId;
    private String brandName;
    private int ordering;

    public SolrBrandProductDTO() {
    }

    public SolrBrandProductDTO(String productId, String productName, String brandId, String brandName, int ordering) {
        this.productId = productId;
        this.productName = productName;
        this.brandId = brandId;
        this.brandName = brandName;
        this.ordering = ordering;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getBrandId() {
        return brandId;
    }

    public void setBrandId(String brandId) {
        this.brandId = brandId;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public int getOrdering() {
        return ordering;
    }

    public void setOrdering(int ordering) {
        this.ordering = ordering;
    }

    @Override
    public String toString() {
        return "SolrBrandProductDTO{" +
                "productId='" + productId + '\'' +
                ", productName='" + productName + '\'' +
                ", brandId='" + brandId + '\'' +
                ", brandName='" + brandName + '\'' +
                ", ordering=" + ordering +
                '}';
    }
}
