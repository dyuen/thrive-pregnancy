package com.thrivepregnancy.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * A record in the Event table
 */
@DatabaseTable
public class Need {

	@DatabaseField(generatedId = true)
	private Integer		id;

}
