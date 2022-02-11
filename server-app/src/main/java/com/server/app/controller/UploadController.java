package com.server.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.app.SwappableSslService;
import nl.altindag.ssl.util.KeyManagerUtils;
import nl.altindag.ssl.util.TrustManagerUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509ExtendedTrustManager;
import java.io.*;
import java.net.URL;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.Iterator;

@RestController
@RequestMapping(value="/upload-app")
public class UploadController
{

	public UploadController(SwappableSslService sslService)
	{
		this.sslService = sslService;
	}

	@RequestMapping(value = "/data", method = RequestMethod.GET)
	public String getData() {
		System.out.println("Returning data from client-app own data method");
		return "Hello from client-app-data method";
	}

	private  Certificate getCertificate(File file)
			throws Exception {
		CertificateFactory certificateFactory = CertificateFactory
				.getInstance("X.509");

		FileInputStream in = new FileInputStream(file);

		Certificate certificate = certificateFactory
				.generateCertificate(in);
		in.close();

		return certificate;
	}


	private final SwappableSslService sslService;

	private void addCertificate(File file2, String certificatePath,String password,String certificateAlias){
		try
		{
			KeyStore keystore = KeyStore.getInstance("PKCS12");
			ClassPathResource classPathResource = new ClassPathResource(certificatePath);
			InputStream inputStream = classPathResource.getInputStream();
			keystore.load(inputStream, password.toCharArray());

			Certificate trustedCert = getCertificate(file2);
			keystore.setCertificateEntry(certificateAlias, trustedCert);

			URL res = getClass().getClassLoader().getResource(certificatePath);
			File file = Paths.get(res.toURI()).toFile();
			certificatePath = file.getAbsolutePath();


			try (FileOutputStream fos = new FileOutputStream(certificatePath)) {
				keystore.store(fos, password.toCharArray());
			}

			X509ExtendedKeyManager keyManager = KeyManagerUtils.createKeyManager(keystore,password.toCharArray());
			X509ExtendedTrustManager trustManager = TrustManagerUtils.createTrustManager(keystore);

			sslService.updateSslMaterials(keyManager,trustManager);

		}catch (Exception e){
			e.printStackTrace();

		}
	}

	private File convertMultiPartToFile(MultipartFile file ) throws IOException
	{
		File convFile = new File( file.getOriginalFilename() );
		FileOutputStream fos = new FileOutputStream( convFile );
		fos.write( file.getBytes() );
		fos.close();
		return convFile;
	}


	@RequestMapping(value = "/uploadMyFile", method = RequestMethod.POST)
	@ResponseBody
	public String handleFileUpload(MultipartHttpServletRequest request)
			throws Exception {

		Iterator<String> itrator = request.getFileNames();
		MultipartFile multiFile = request.getFile(itrator.next());
		try {

			System.out.println("File Length:" + multiFile.getBytes().length);
			System.out.println("File Type:" + multiFile.getContentType());
			String fileName=multiFile.getOriginalFilename();
			System.out.println("File Name:" +fileName);
			String path=request.getServletContext().getRealPath("/");

			File fl = convertMultiPartToFile(multiFile);

			String certificatePath = "server-app.p12";
			String password = "server-app";
			String certificateAlias = "client-app";
			addCertificate(fl,certificatePath,password,certificateAlias);


		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Error while loading the file");
		}
		return toJson("File Uploaded successfully.");
	}

	public String toJson(Object data)
	{
		ObjectMapper mapper=new ObjectMapper();
		StringBuilder builder=new StringBuilder();
		try {
			builder.append(mapper.writeValueAsString(data));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return builder.toString();
	}
}
