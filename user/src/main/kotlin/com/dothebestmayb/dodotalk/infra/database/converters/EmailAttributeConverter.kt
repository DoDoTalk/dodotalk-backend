package com.dothebestmayb.dodotalk.infra.database.converters

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

/**
 * JPA AttributeConverter that automatically normalizes email addresses.
 * Ensures all emails are stored in lowercase and trimmed in the database.
 */
@Converter
class EmailAttributeConverter: AttributeConverter<String, String> {

    override fun convertToDatabaseColumn(attribute: String?): String? {
        return attribute?.trim()?.lowercase()
    }

    override fun convertToEntityAttribute(dbData: String?): String? {
        return dbData?.trim()?.lowercase()
    }
}