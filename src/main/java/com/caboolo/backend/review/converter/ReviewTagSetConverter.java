package com.caboolo.backend.review.converter;

import com.caboolo.backend.core.converter.AbstractEnumSetConverter;
import com.caboolo.backend.review.enums.ReviewTag;
import jakarta.persistence.Converter;

@Converter
public class ReviewTagSetConverter extends AbstractEnumSetConverter<ReviewTag> {

    public ReviewTagSetConverter() {
        super(ReviewTag.class);
    }
}
