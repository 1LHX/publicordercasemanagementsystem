package com.example.publicordercasemanagementsystem.mapper;

import com.example.publicordercasemanagementsystem.pojo.CaseWorkflowActionLog;
import com.example.publicordercasemanagementsystem.pojo.CaseWorkflowInstance;
import com.example.publicordercasemanagementsystem.pojo.CaseWorkflowTask;
import com.example.publicordercasemanagementsystem.pojo.WorkflowDefinition;
import com.example.publicordercasemanagementsystem.pojo.WorkflowNode;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CaseWorkflowMapper {

    WorkflowDefinition findActiveDefinitionByFlowType(@Param("flowType") String flowType);

    List<WorkflowNode> findNodesByDefinitionId(@Param("definitionId") Long definitionId);

    int insertWorkflowInstance(CaseWorkflowInstance instance);

    CaseWorkflowInstance findByInstanceId(@Param("instanceId") Long instanceId);

    List<CaseWorkflowInstance> findByCaseId(@Param("caseId") Long caseId);

    CaseWorkflowInstance findActiveInstanceByCaseIdAndFlowType(@Param("caseId") Long caseId,
                                                               @Param("flowType") String flowType);

    int updateInstanceProgress(@Param("id") Long id,
                               @Param("status") String status,
                               @Param("currentNodeKey") String currentNodeKey,
                               @Param("finishedBy") Long finishedBy,
                               @Param("finishedAt") LocalDateTime finishedAt);

    int insertWorkflowTask(CaseWorkflowTask task);

    CaseWorkflowTask findTodoTaskById(@Param("taskId") Long taskId);

    List<CaseWorkflowTask> findTasksByInstanceId(@Param("instanceId") Long instanceId);

    List<CaseWorkflowTask> findPendingTasksByRole(@Param("roleCode") String roleCode);

    int updateTaskDecision(@Param("taskId") Long taskId,
                           @Param("status") String status,
                           @Param("comment") String comment,
                           @Param("actedBy") Long actedBy,
                           @Param("actedAt") LocalDateTime actedAt);

    int closeTodoTasksByInstanceId(@Param("instanceId") Long instanceId,
                                   @Param("status") String status,
                                   @Param("comment") String comment,
                                   @Param("actedBy") Long actedBy,
                                   @Param("actedAt") LocalDateTime actedAt);

    int insertActionLog(CaseWorkflowActionLog actionLog);

    CaseWorkflowActionLog findActionLogByRequestId(@Param("requestId") String requestId);
}

