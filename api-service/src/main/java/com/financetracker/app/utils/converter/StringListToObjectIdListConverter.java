package com.financetracker.app.utils.converter;

import org.bson.types.ObjectId;
import org.springframework.core.convert.converter.Converter;

import java.util.List;

public class StringListToObjectIdListConverter implements Converter<List<String>, List<ObjectId>> {

    @Override
    public List<ObjectId> convert(List<String> source) {
        return source.stream()
                .map(ObjectId::new)
                .toList();
    }
}
