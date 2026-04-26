package com.example.publicordercasemanagementsystem.service;

import com.example.publicordercasemanagementsystem.dto.ChatCompletionRequest;
import com.example.publicordercasemanagementsystem.dto.ChatCompletionResponse;
import com.example.publicordercasemanagementsystem.dto.ChatMessage;
import com.example.publicordercasemanagementsystem.dto.PromptRequest;
import com.example.publicordercasemanagementsystem.dto.CaseDocumentRequest;
import com.example.publicordercasemanagementsystem.dto.CaseDetailResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
public class DashScopeService {

    private final WebClient webClient;
    private final CaseService caseService;

    public DashScopeService(@Qualifier("dashScopeWebClient") WebClient webClient, CaseService caseService) {
        this.webClient = webClient;
        this.caseService = caseService;
    }

    public ChatCompletionResponse chat(ChatCompletionRequest request) {
        return webClient.post()
                .uri("/chat/completions")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ChatCompletionResponse.class)
                .block();
    }

    public ChatCompletionResponse chat(PromptRequest request) {
        List<ChatMessage> messages = new ArrayList<>();
        // 添加系统提示，要求生成符合常规文书格式的内容，基于系统数据库数据类型
        messages.add(new ChatMessage("system", "请基于该公安案件管理系统数据库中的数据类型生成文书。数据类型包括：\n" +
                "- 案件信息：案件编号(case_number)、标题(title)、类型(type_code，如PUBLIC_DISTURBANCE)、状态(status，如REGISTERED/ACCEPTED/INVESTIGATING/CLOSED)、报告人(reporter_name)、联系方式(reporter_contact)、发生时间(incident_time)、地点(incident_location)、简要描述(brief_description)、创建者(creator_id)、处理人员(handling_officer_id)、部门(department_id)、截止时间(deadline_time)、是否逾期(is_overdue)。\n" +
                "- 用户信息：姓名(name)、角色(role，如admin/supervisor/legal_officer/police_officer)、部门(department_id)。\n" +
                "- 部门信息：名称(name)、上级部门(parent_id)。\n" +
                "- 案件流程：状态变更(from_status/to_status)、操作者(operator_id)、操作时间(operation_time)、备注(comment)。\n" +
                "- 证据：文件名(file_name)、描述(description)、上传者(upload_user_id)。\n" +
                "- 法律审查：审查状态(review_status)、审查意见(review_comment)、审查者(reviewer_id)。\n" +
                "- 决定：决定结果(decision_result)、决定内容(decision_content)、强制措施(coercive_measure_code)、决定者(decided_by)。\n" +
                "- 执行：执行结果(execution_result)、执行备注(execution_note)、执行者(executed_by)。\n" +
                "请生成一份符合中国行政公文格式的文书，包括标题、文号、发文机关、发文日期、正文和落款。确保内容基于上述数据类型，并以纯文本格式输出，便于阅读和打印。"));
        messages.add(new ChatMessage("user", request.getPrompt()));

        ChatCompletionRequest chatRequest = new ChatCompletionRequest();
        if (request.getModel() != null && !request.getModel().isBlank()) {
            chatRequest.setModel(request.getModel());
        }
        chatRequest.setMessages(messages);

        return chat(chatRequest);
    }

    public Mono<ChatCompletionResponse> chatAsync(ChatCompletionRequest request) {
        return webClient.post()
                .uri("/chat/completions")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ChatCompletionResponse.class);
    }

    public ChatCompletionResponse generateCaseDocument(CaseDocumentRequest request) {
        // 获取案件详情
        CaseDetailResponse caseDetail = caseService.getCaseById(request.getCaseId());

        // 构建用户prompt，包含案件数据
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("请基于以下案件数据生成").append(request.getDocumentType() != null ? request.getDocumentType() : "处理").append("文书：\n");
        promptBuilder.append("案件编号: ").append(caseDetail.getCaseNumber()).append("\n");
        promptBuilder.append("标题: ").append(caseDetail.getTitle()).append("\n");
        promptBuilder.append("类型: ").append(caseDetail.getTypeName()).append("\n");
        promptBuilder.append("状态: ").append(caseDetail.getStatus()).append("\n");
        promptBuilder.append("报告人: ").append(caseDetail.getReporterName()).append("\n");
        if (caseDetail.getReporterContact() != null) {
            promptBuilder.append("联系方式: ").append(caseDetail.getReporterContact()).append("\n");
        }
        if (caseDetail.getIncidentTime() != null) {
            promptBuilder.append("发生时间: ").append(caseDetail.getIncidentTime()).append("\n");
        }
        if (caseDetail.getIncidentLocation() != null) {
            promptBuilder.append("地点: ").append(caseDetail.getIncidentLocation()).append("\n");
        }
        if (caseDetail.getBriefDescription() != null) {
            promptBuilder.append("描述: ").append(caseDetail.getBriefDescription()).append("\n");
        }
        if (caseDetail.getCreatorName() != null) {
            promptBuilder.append("创建者: ").append(caseDetail.getCreatorName()).append("\n");
        }
        if (caseDetail.getHandlingOfficerName() != null) {
            promptBuilder.append("处理人员: ").append(caseDetail.getHandlingOfficerName()).append("\n");
        }
        if (caseDetail.getDepartmentName() != null) {
            promptBuilder.append("部门: ").append(caseDetail.getDepartmentName()).append("\n");
        }
        if (caseDetail.getDeadlineTime() != null) {
            promptBuilder.append("截止时间: ").append(caseDetail.getDeadlineTime()).append("\n");
        }
        promptBuilder.append("是否逾期: ").append(caseDetail.getIsOverdue() ? "是" : "否").append("\n");

        // 添加其他相关数据，如证据、流程等（如果CaseDetailResponse包含）
        // 这里假设CaseDetailResponse有相关字段，如果没有，需要扩展

        List<ChatMessage> messages = new ArrayList<>();
        // 使用相同的系统提示
        messages.add(new ChatMessage("system", "请基于该公安案件管理系统数据库中的数据类型生成文书。数据类型包括：\n" +
                "- 案件信息：案件编号(case_number)、标题(title)、类型(type_code，如PUBLIC_DISTURBANCE)、状态(status，如REGISTERED/ACCEPTED/INVESTIGATING/CLOSED)、报告人(reporter_name)、联系方式(reporter_contact)、发生时间(incident_time)、地点(incident_location)、简要描述(brief_description)、创建者(creator_id)、处理人员(handling_officer_id)、部门(department_id)、截止时间(deadline_time)、是否逾期(is_overdue)。\n" +
                "- 用户信息：姓名(name)、角色(role，如admin/supervisor/legal_officer/police_officer)、部门(department_id)。\n" +
                "- 部门信息：名称(name)、上级部门(parent_id)。\n" +
                "- 案件流程：状态变更(from_status/to_status)、操作者(operator_id)、操作时间(operation_time)、备注(comment)。\n" +
                "- 证据：文件名(file_name)、描述(description)、上传者(upload_user_id)。\n" +
                "- 法律审查：审查状态(review_status)、审查意见(review_comment)、审查者(reviewer_id)。\n" +
                "- 决定：决定结果(decision_result)、决定内容(decision_content)、强制措施(coercive_measure_code)、决定者(decided_by)。\n" +
                "- 执行：执行结果(execution_result)、执行备注(execution_note)、执行者(executed_by)。\n" +
                "请生成一份符合中国行政公文格式的文书，包括标题、文号、发文机关、发文日期、正文和落款。确保内容基于上述数据类型，并以纯文本格式输出，便于阅读和打印。"));
        messages.add(new ChatMessage("user", promptBuilder.toString()));

        ChatCompletionRequest chatRequest = new ChatCompletionRequest();
        if (request.getModel() != null && !request.getModel().isBlank()) {
            chatRequest.setModel(request.getModel());
        }
        chatRequest.setMessages(messages);

        return chat(chatRequest);
    }
}
