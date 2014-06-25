package com.thrivepregnancy.data;

import java.util.Date;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * A record in the Event table
 */
@DatabaseTable
public class Event {

	public static enum Type{
		TIP, 		
		DIARY_ENTRY,
		APPOINTMENT,
		TEST_RESULT, 
		QUESTION 
		}

	@DatabaseField(generatedId = true)
	private Integer		id;
	
	@DatabaseField
	private Type		type;

	/**
	 * Date semantics vary by Type:
	 * <p>
	 * <ul>TIP:         Calculated at first startup after install as f{tip date, due date}</ul
	 * <ul>DIARY_ENTRY: Entered date (default to current)</ul
	 * <ul>APPOINTMENT: Entered future date</ul
	 * <ul>TEST_RESULT: Entered date (default to current)</ul
	 * <ul>QUESTION:    Automatically generated (simply to order the questions)</ul>
	 */
	@DatabaseField
	private Date		date;

	/**
	 * Text semantics vary by Type:
	 * <p>
	 * <ul>TIP:         Text from the Tips file</ul
	 * <ul>DIARY_ENTRY: "Notes" field</ul
	 * <ul>APPOINTMENT: "Notes" field</ul
	 * <ul>TEST_RESULT: "Notes" field</ul
	 * <ul>QUESTION:    The question</ul>
	 */
	@DatabaseField
	private String		text;
	
	/**
	 * For appointment only
	 */
	@DatabaseField
	private String		purpose;
	
	/**
	 * For appointment only
	 */
	@DatabaseField
	private String		doctor;
	
	/**
	 * For appointment only
	 */
	@DatabaseField
	private String		address;
	
	/**
	 * Name of photo file. Semantics vary by Type
	 * <p>
	 * <ul>TIP:         Optional photo packaged with the tip</ul
	 * <ul>DIARY_ENTRY: Optional photo taken by user</ul
	 * <ul>APPOINTMENT: Optional photo taken by user</ul
	 * <ul>TEST_RESULT: Optional photo taken by user</ul
	 * <ul>QUESTION:    n/a</ul>
	 */
	@DatabaseField
	private String		photoFile;
	
	/**
	 * Name of audio file. Semantics vary by Type 
	 * <p>
	 * <ul>TIP:         Optional audio packaged with the tip</ul
	 * <ul>DIARY_ENTRY: Optional audio recorded by user</ul
	 * <ul>APPOINTMENT: n/a</ul
	 * <ul>TEST_RESULT: n/a</ul
	 * <ul>QUESTION:    n/a</ul>
	 */
	@DatabaseField
	private String		audioFile;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
	public String getPurpose() {
		return purpose;
	}

	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}
	
	public String getDoctor() {
		return doctor;
	}

	public void setDoctor(String doctor) {
		this.doctor = doctor;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPhotoFile() {
		return photoFile;
	}

	public void setPhotoFile(String photoFile) {
		this.photoFile = photoFile;
	}

	public String getAudioFile() {
		return audioFile;
	}

	public void setAudioFile(String audioFile) {
		this.audioFile = audioFile;
	}
}
