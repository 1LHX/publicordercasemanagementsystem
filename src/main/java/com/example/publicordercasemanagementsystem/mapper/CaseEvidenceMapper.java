package com.example.publicordercasemanagementsystem.mapper;

import com.example.publicordercasemanagementsystem.pojo.CaseEvidence;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CaseEvidenceMapper {

    List<CaseEvidence> findByCaseId(@Param("caseId") Long caseId);
}
