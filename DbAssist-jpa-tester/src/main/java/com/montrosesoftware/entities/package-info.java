/**
 * Treat all the dates in DB as UTC
 * This will apply the TypeDef for all entities having Date attribute
 */

@TypeDef(name = "UtcTimestampType", defaultForType = Date.class, typeClass = UtcTimestampType.class)
package com.montrosesoftware.entities;

import com.montrosesoftware.types.UtcTimestampType;
import org.hibernate.annotations.TypeDef;

import java.util.Date;

