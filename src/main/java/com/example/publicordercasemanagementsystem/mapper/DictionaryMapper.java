package com.example.publicordercasemanagementsystem.mapper;

import com.example.publicordercasemanagementsystem.pojo.DictCaseType;

import java.util.List;

public interface DictionaryMapper {

    List<DictCaseType> findActiveCaseTypes();
}

