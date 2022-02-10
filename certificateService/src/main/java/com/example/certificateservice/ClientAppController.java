package com.example.certificateservice;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.apache.http.client.HttpClient;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.security.KeyStore;

@RestController
@RequestMapping(value="/cert-app")
public class ClientAppController
{


	@RequestMapping(value = "/data", method = RequestMethod.GET)
	public String getData() throws URISyntaxException
	{

		return getRs().getForObject(new URI("https://localhost:9007/server-app/data"), String.class);
	}
	private RestTemplate getRs(){
		RestTemplate restTemplate = new RestTemplate();

		KeyStore keyStore;
		HttpComponentsClientHttpRequestFactory requestFactory = null;

		try {
			keyStore = KeyStore.getInstance("PKCS12");
			ClassPathResource classPathResource = new ClassPathResource("certificate-app.p12");
			InputStream inputStream = classPathResource.getInputStream();
			keyStore.load(inputStream, "cert-app".toCharArray());
			SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(new SSLContextBuilder()
					.loadTrustMaterial(null, new TrustSelfSignedStrategy())
					.loadKeyMaterial(keyStore, "cert-app".toCharArray()).build(),
					NoopHostnameVerifier.INSTANCE);

			HttpClient httpClient = HttpClients.custom().setSSLSocketFactory(socketFactory)
					.setMaxConnTotal(Integer.valueOf(5))
					.setMaxConnPerRoute(Integer.valueOf(5))
					.build();


			requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
			requestFactory.setReadTimeout(Integer.valueOf(10000));
			requestFactory.setConnectTimeout(Integer.valueOf(10000));


			restTemplate.setRequestFactory(requestFactory);
		}
		catch (Exception exception) {
			System.out.println("Exception Occured while creating restTemplate "+exception);
			exception.printStackTrace();
		}
		return restTemplate;
	}
	@RequestMapping(value = "/ms-data", method = RequestMethod.GET)
	public String getMsData() throws URISyntaxException
	{

		return getRs().getForObject(new URI("https://localhost:9007/server-app/data"), String.class);

	}


	private File getFIle()
	{

		URL res = getClass().getClassLoader().getResource("client-app.crt");
		File file = null;
		try
		{
			file = Paths.get(res.toURI()).toFile();
		}
		catch (URISyntaxException e)
		{
			e.printStackTrace();
		}

		return file;
	}


	@RequestMapping(value = "/upload", method = RequestMethod.GET)
	public ResponseEntity<?> uploadImages() throws IOException
	{
		LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
		String response;
		HttpStatus httpStatus = HttpStatus.CREATED;

		try {
			File file = getFIle();


			FileInputStream input = new FileInputStream(file);
			MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "text/plain", input);

			map.add("file", new MultipartInputStreamFileResource(multipartFile.getInputStream(), multipartFile.getOriginalFilename()));


			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.MULTIPART_FORM_DATA);

			String url = "https://localhost:9007/upload-app/uploadMyFile/";

			HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(map, headers);
			RestTemplate restTemplate = getRs();

			response = restTemplate.postForObject(url, requestEntity, String.class);

		} catch (HttpStatusCodeException e) {
			httpStatus = HttpStatus.valueOf(e.getStatusCode().value());
			response = e.getResponseBodyAsString();
		} catch (Exception e) {
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			response = e.getMessage();
		}

		return new ResponseEntity<>(response, httpStatus);
	}

}



class MultipartInputStreamFileResource extends InputStreamResource
{

	private final String filename;

	MultipartInputStreamFileResource(InputStream inputStream, String filename) {
		super(inputStream);
		this.filename = filename;
	}

	@Override
	public String getFilename() {
		return this.filename;
	}

	@Override
	public long contentLength() throws IOException {
		return -1; // we do not want to generally read the whole stream into memory ...
	}
}
