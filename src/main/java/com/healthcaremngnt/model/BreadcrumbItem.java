package com.healthcaremngnt.model;

public class BreadcrumbItem {

	public BreadcrumbItem(String label, String url) {
		super();
		this.label = label;
		this.url = url;
	}

	private String label;
	private String url;

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public String toString() {
		return "BreadcrumbItem [label=" + label + ", url=" + url + "]";
	}

}