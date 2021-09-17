package com.foxconn.utils;

public class HttpGet extends org.apache.http.client.methods.HttpGet
{
	public HttpGet(String URL)
	{
		super(URL);
		setHeader("Content-Type", "application/x-www-form-urlencoded");
		setHeader("User-Agent", 
				"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1) ; .NET CLR 2.0.50727; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729; .NET4.0C; .NET4.0E)");
	}
}