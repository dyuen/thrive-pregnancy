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
	
	@DatabaseField
	private String title;
	
	@DatabaseField
	private Boolean needit;
	
	@DatabaseField
	private Boolean gotit;
	
	@DatabaseField
	private String resources;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Boolean getNeedit() {
		return needit;
	}

	public void setNeedit(Boolean needit) {
		this.needit = needit;
	}

	public Boolean getGotit() {
		return gotit;
	}

	public void setGotit(Boolean gotit) {
		this.gotit = gotit;
	}

	public String getResources() {
		return resources;
	}

	public void setResources(String resources) {
		this.resources = resources;
	}

}
