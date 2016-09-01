/**
 * Treat all the dates in DB as UTC
 * This will applyConditions the TypeDef for all entities having Date attribute
 */
@TypeDefs(
        {
                @TypeDef(name = "UtcDateType", defaultForType = Date.class, typeClass = UtcDateType.class),
                @TypeDef(name = "UtcDateType", defaultForType = Timestamp.class, typeClass = UtcDateType.class)
        }
)
package com.montrosesoftware.types;

import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import java.sql.Timestamp;
import java.util.Date;