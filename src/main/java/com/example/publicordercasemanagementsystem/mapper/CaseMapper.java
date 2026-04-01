package com.example.publicordercasemanagementsystem.mapper;

import com.example.publicordercasemanagementsystem.pojo.CaseDecision;
import com.example.publicordercasemanagementsystem.pojo.CaseEvidence;
import com.example.publicordercasemanagementsystem.pojo.CaseExecution;
import com.example.publicordercasemanagementsystem.pojo.CaseLegalReview;
import com.example.publicordercasemanagementsystem.pojo.CaseProcess;
import com.example.publicordercasemanagementsystem.pojo.CaseRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CaseMapper {

    int insertCase(CaseRecord record);

    CaseRecord findById(Long id);

    CaseRecord findByCaseNumber(String caseNumber);

    long countCases(@Param("caseNumber") String caseNumber,
                    @Param("title") String title,
                    @Param("typeCode") String typeCode,
                    @Param("status") String status,
                    @Param("departmentId") Long departmentId,
                    @Param("handlingOfficerId") Long handlingOfficerId,
                    @Param("isOverdue") Boolean isOverdue);

    List<CaseRecord> findCasePage(@Param("caseNumber") String caseNumber,
                                  @Param("title") String title,
                                  @Param("typeCode") String typeCode,
                                  @Param("status") String status,
                                  @Param("departmentId") Long departmentId,
                                  @Param("handlingOfficerId") Long handlingOfficerId,
                                  @Param("isOverdue") Boolean isOverdue,
                                  @Param("offset") int offset,
                                  @Param("size") int size);

    int updateCase(CaseRecord record);

    int assignOfficer(@Param("id") Long id,
                      @Param("handlingOfficerId") Long handlingOfficerId,
                      @Param("departmentId") Long departmentId);

    int updateCaseStatus(@Param("id") Long id,
                         @Param("status") String status,
                         @Param("acceptanceTime") java.time.LocalDateTime acceptanceTime);

    int insertProcess(CaseProcess process);

    List<CaseProcess> findProcessesByCaseId(Long caseId);

    int insertEvidence(CaseEvidence evidence);

    List<CaseEvidence> findEvidencesByCaseId(Long caseId);

    int upsertLegalReview(CaseLegalReview legalReview);

    CaseLegalReview findLegalReviewByCaseId(Long caseId);

    int upsertDecision(CaseDecision decision);

    int upsertExecution(CaseExecution execution);
}
