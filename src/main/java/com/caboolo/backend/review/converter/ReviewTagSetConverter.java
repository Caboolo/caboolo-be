package com.caboolo.backend.review.converter;

import com.caboolo.backend.core.converter.AbstractEnumSetConverter;
import com.caboolo.backend.review.enums.ReviewTagType;
import jakarta.persistence.Converter;

@Converter
public class ReviewTagSetConverter extends AbstractEnumSetConverter<ReviewTagType> {

    public ReviewTagSetConverter() {
        super(ReviewTagType.class);
    }
}
