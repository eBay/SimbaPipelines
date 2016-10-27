package com.ebay.cip.framework.messages;

import java.util.List;
import java.util.Map;

import com.ebay.cip.framework.job.Job;
import com.ebay.cip.service.job.OrchestratorJob;

public class PipelineRequest {
	public Map<String, List<String>> headerListMap;
	public String path;
	public String payload;
	public Job job = OrchestratorJob.INSTANCE;
	
	
	public PipelineRequest(Map<String, List<String>> headerListMap, String path, String payload){
		this.headerListMap = headerListMap;
		this.path = path;
		this.payload = payload;
	}
}
