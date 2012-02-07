package uk.ac.ebi.ep.adapter.reactome;

public class ReactomeConfig implements ReactomeConfigMBean {

	/**
	 * Use a proxy for requests to Reactome?
	 */
	private boolean useProxy = true;
	
	/**
	 * Timeout for requests to Reactome, in milliseconds.
	 */
	private int timeout = 30000;
	
	private String wsBaseUrl = 
			"http://www.reactome.org:8080/ReactomeRESTfulAPI/RESTfulWS/queryById/";
	
	public void setUseProxy(boolean useProxy) {
		this.useProxy = useProxy;
	}

	public boolean getUseProxy() {
		return useProxy;
	}

	public void setTimeout(int msec) {
		this.timeout = msec;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setWsBaseUrl(String wsBaseUrl) {
		this.wsBaseUrl = wsBaseUrl;
	}

	public String getWsBaseUrl() {
		return wsBaseUrl;
	}

}
