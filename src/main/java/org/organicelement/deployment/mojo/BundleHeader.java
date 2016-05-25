package org.organicelement.deployment.mojo;

/**
 * Represents a Bundle header added during packaging of deployment package.
 * 
 * @author Thomas Leveque
 *
 */
public class BundleHeader implements Header {

	private String value;
	
	private String headerName;
	
	public BundleHeader() {
		// do nothing
	}
	
	public BundleHeader(String headerName, String value) {
		headerName = headerName;
		value = value;
	}

	public String getName() {
		return headerName;
	}
	
	public void setName(String headerName) {
		headerName = headerName;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		value = value;
	}
	
	public Header clone() {
		return new BundleHeader(headerName, value);
	}
}
