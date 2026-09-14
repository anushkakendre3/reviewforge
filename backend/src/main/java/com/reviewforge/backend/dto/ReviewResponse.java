package com.reviewforge.backend.dto;

import java.util.List;

public class ReviewResponse {

    private String status;

    private String repoName;

    private List<String> files;

    private int filesAnalyzed;

    private int chunkCount;

    private int retrievedChunks;

    private String review;

    private boolean aiUsed;

    private String reviewEngine;

    private String analysisType;


    public ReviewResponse() {
    }


    public String getStatus() {
        return status;
    }


    public void setStatus(String status) {
        this.status = status;
    }


    public String getRepoName() {
        return repoName;
    }


    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }


    public List<String> getFiles() {
        return files;
    }


    public void setFiles(List<String> files) {
        this.files = files;
    }


    public int getFilesAnalyzed() {
        return filesAnalyzed;
    }


    public void setFilesAnalyzed(int filesAnalyzed) {
        this.filesAnalyzed = filesAnalyzed;
    }


    public int getChunkCount() {
        return chunkCount;
    }


    public void setChunkCount(int chunkCount) {
        this.chunkCount = chunkCount;
    }


    public int getRetrievedChunks() {
        return retrievedChunks;
    }


    public void setRetrievedChunks(int retrievedChunks) {
        this.retrievedChunks = retrievedChunks;
    }


    public String getReview() {
        return review;
    }


    public void setReview(String review) {
        this.review = review;
    }


    public boolean isAiUsed() {
        return aiUsed;
    }


    public void setAiUsed(boolean aiUsed) {
        this.aiUsed = aiUsed;
    }


    public String getReviewEngine() {
        return reviewEngine;
    }


    public void setReviewEngine(String reviewEngine) {
        this.reviewEngine = reviewEngine;
    }


    public String getAnalysisType() {
        return analysisType;
    }


    public void setAnalysisType(String analysisType) {
        this.analysisType = analysisType;
    }

}