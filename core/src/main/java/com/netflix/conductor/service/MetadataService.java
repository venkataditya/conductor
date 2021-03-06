/**
 * Copyright 2016 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * 
 */
package com.netflix.conductor.service;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.netflix.conductor.annotations.Trace;
import com.netflix.conductor.common.metadata.tasks.TaskDef;
import com.netflix.conductor.common.metadata.workflow.WorkflowDef;
import com.netflix.conductor.core.WorkflowContext;
import com.netflix.conductor.core.execution.ApplicationException;
import com.netflix.conductor.core.execution.ApplicationException.Code;
import com.netflix.conductor.dao.MetadataDAO;

/**
 * @author Viren Workflow Manager
 */
@Singleton
@Trace
public class MetadataService {

	private MetadataDAO metadata;

	@Inject
	public MetadataService(MetadataDAO metadata) {
		this.metadata = metadata;
	}

	public void registerTaskDef(List<TaskDef> taskDefs) throws Exception {
		for (TaskDef taskDef : taskDefs) {
			taskDef.setCreatedBy(WorkflowContext.get().getClientApp());
	   		taskDef.setCreateTime(System.currentTimeMillis());
	   		taskDef.setUpdatedBy(null);
	   		taskDef.setUpdateTime(null);
			metadata.createTaskDef(taskDef);
		}
	}

	public void updateTaskDef(TaskDef taskDef) throws Exception {
		TaskDef existing = metadata.getTaskDef(taskDef.getName());
		if (existing == null) {
			throw new ApplicationException(Code.NOT_FOUND, "No such task by name " + taskDef.getName());
		}
   		taskDef.setUpdatedBy(WorkflowContext.get().getClientApp());
   		taskDef.setUpdateTime(System.currentTimeMillis());
		metadata.updateTaskDef(taskDef);
	}

	public void unregisterTaskDef(String taskType) {
		metadata.removeTaskDef(taskType);
	}

	public List<TaskDef> getTaskDefs() throws Exception {
		return metadata.getAllTaskDefs();
	}

	public TaskDef getTaskDef(String taskType) throws Exception {
		return metadata.getTaskDef(taskType);
	}

	public void updateWorkflowDef(WorkflowDef def) throws Exception {
		metadata.update(def);		
	}
	
	public void updateWorkflowDef(List<WorkflowDef> wfs) throws Exception {
		for (WorkflowDef wf : wfs) {
			metadata.update(wf);
		}
	}

	public WorkflowDef getWorkflowDef(String name, Integer version) throws Exception {
		if (version == null) {
			return metadata.getLatest(name);
		}
		return metadata.get(name, version);
	}

	public List<WorkflowDef> getWorkflowDefs() throws Exception {
		return metadata.getAll();
	}

	public void registerWorkflowDef(WorkflowDef def) throws Exception {
		if(def.getName().contains(":")) {
			throw new ApplicationException(Code.INVALID_INPUT, "Workflow name cannot contain the following set of characters: ':'");
		}
		metadata.create(def);
	}

	
	
}
