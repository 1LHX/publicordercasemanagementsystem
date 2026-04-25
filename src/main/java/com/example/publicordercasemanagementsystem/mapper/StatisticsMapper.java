package com.example.publicordercasemanagementsystem.mapper;

import com.example.publicordercasemanagementsystem.dto.OfficerEfficiencyItem;
import com.example.publicordercasemanagementsystem.dto.RegionHotspotItem;
import com.example.publicordercasemanagementsystem.dto.ReviewPassRateItem;
import com.example.publicordercasemanagementsystem.dto.StatisticsCountItem;
import com.example.publicordercasemanagementsystem.dto.TimeCountItem;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface StatisticsMapper {

    long countCasesTotal(@Param("startTime") LocalDateTime startTime,
                         @Param("endTime") LocalDateTime endTime);

    List<StatisticsCountItem> countCasesByStatus(@Param("startTime") LocalDateTime startTime,
                                                 @Param("endTime") LocalDateTime endTime);

    List<StatisticsCountItem> countCasesByType(@Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime);

    List<TimeCountItem> countCasesByPeriod(@Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime,
                                           @Param("periodFormat") String periodFormat);


    List<RegionHotspotItem> findRegionHotspots(@Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime,
                                               @Param("topN") int topN);

    List<OfficerEfficiencyItem> findOfficerEfficiency(@Param("startTime") LocalDateTime startTime,
                                                      @Param("endTime") LocalDateTime endTime,
                                                      @Param("topN") int topN);

    List<ReviewPassRateItem> findReviewPassRate(@Param("startTime") LocalDateTime startTime,
                                                @Param("endTime") LocalDateTime endTime);

    long countCurrentOnlineUsers();

    long countTotalUsers();

    long countPoliceOfficers();

    long countTotalCasesAll();

    long countOpenCases();

    long countClosedCases();

    long countOverdueCases();

    long countNearDeadlineCases(@Param("deadlineBefore") LocalDateTime deadlineBefore);
}

