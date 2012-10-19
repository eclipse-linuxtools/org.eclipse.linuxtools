package org.eclipse.linuxtools.systemtap.ui.consolelog.structures;

/**
 * Turn error codes into text. Taken from include/staperror.hpp
 * from the data manager's source.
 * 
 * @author patrickm
 *
 */
public final class DMError {
	
	private String errtext;
	
	public DMError (int errcode) {
		
		if (errcode >= 0) {
			errtext = "Success";
			return;
		}

		switch (errcode) {
			case 0x82100001:
				errtext = "Invalid/Unknown request type";
				break;
			case 0x81100002:
				errtext = "Error running script, or script unexpectedly died";
				break;
			case 0x82100003:
				errtext = "Unknown / unused !?";
				break;
			case 0x81100004:
				errtext = "Unused?";
				break;
			case 0x81200005:
				errtext = "Invalid mailbox number requested";
				break;
			case 0x82200006:
				errtext = "Duplicate/invalid un- or subscription request";
				break;
			case 0x82200007:
				errtext = "Failed to release or aquire a mutex lock";
				break;
			case 0x82200009:
				errtext = "Malformed or unrecognizable packet";
				break;
			case 0x8220000A:
				errtext = "Invalid client ID";
				break;
			case 0x8220000B:
				errtext = "Attemting to use unopen or bad channel";
				break;
			default:
				errtext = "Unknown Error: " + errcode;
		}
		return;
	}
	
	@Override
	public String toString() {
		return errtext;
	}
	
}
