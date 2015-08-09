package com.jaimegag.demo.springcloud;

import org.joda.time.Instant;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class VisitLog {
	
	private Instant timestamp = null;
	private String ip = null;
	private String ua = null;
	
	public VisitLog (String ip, String ua) {
		this.ip = ip;
		this.ua = ua;
		this.timestamp = new Instant();
	}

	public String getIp() {
		return this.ip;
	}
	
	public String getUa() {
		return this.ua;
	}
	
	public String getTimestamp() {
		DateTimeFormatter dtfOut = DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss:SSS");
		return dtfOut.print(this.timestamp.toDateTime());
	}
	
	@Override
	public String toString() {
		return this.timestamp.toDateTime().toString() + " - " + this.ua + " - " + this.ip;
	}

}
