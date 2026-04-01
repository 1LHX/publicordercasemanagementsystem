package com.example.publicordercasemanagementsystem.service.impl;

import com.example.publicordercasemanagementsystem.dto.CaseTypeItem;
import com.example.publicordercasemanagementsystem.mapper.DictionaryMapper;
import com.example.publicordercasemanagementsystem.pojo.DictCaseType;
import com.example.publicordercasemanagementsystem.service.DictionaryService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DictionaryServiceImpl implements DictionaryService {

    private final DictionaryMapper dictionaryMapper;

    public DictionaryServiceImpl(DictionaryMapper dictionaryMapper) {
        this.dictionaryMapper = dictionaryMapper;
    }

    @Override
    public List<CaseTypeItem> listCaseTypes() {
        List<DictCaseType> list = dictionaryMapper.findActiveCaseTypes();
        List<CaseTypeItem> items = new ArrayList<>(list.size());
        for (DictCaseType type : list) {
            CaseTypeItem item = new CaseTypeItem();
            item.setCode(type.getCode());
            item.setName(type.getName());
            item.setSortOrder(type.getSortOrder());
            item.setIsActive(type.getIsActive());
            items.add(item);
        }
        return items;
    }
}

