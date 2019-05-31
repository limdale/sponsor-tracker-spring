package com.limdale.sponsorpls.repository;

import com.limdale.sponsorpls.model.SponsorshipWillingness;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class SponsorshipWillingnessConverter implements AttributeConverter<SponsorshipWillingness, String> {

    @Override
    public String convertToDatabaseColumn(SponsorshipWillingness sponsorshipWillingness) {
        return sponsorshipWillingness.getLabel();
    }

    @Override
    public SponsorshipWillingness convertToEntityAttribute(String s) {
        return SponsorshipWillingness.fromLabel(s);
    }
}
