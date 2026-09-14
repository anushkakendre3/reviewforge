package com.reviewforge.backend.dto;


public class AiReviewRequest {


    private String repoUrl;


    public AiReviewRequest() {
    }


    public String getRepoUrl() {

        return repoUrl;

    }


    public void setRepoUrl(
            String repoUrl
    ) {

        this.repoUrl = repoUrl;

    }

}