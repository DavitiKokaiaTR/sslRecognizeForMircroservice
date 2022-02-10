package com.client.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.*;
import java.net.URL;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Iterator;

@RestController
@RequestMapping(value="/upload-app")
public class UploadController
{

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

	private void addCertificate(File file2){
		try
		{
			KeyStore keystore = KeyStore.getInstance("PKCS12");
			ClassPathResource classPathResource = new ClassPathResource("client-app.p12");
			InputStream inputStream = classPathResource.getInputStream();
			keystore.load(inputStream, "client-app".toCharArray());

			Certificate trustedCert = getCertificate(file2);

			keystore.setCertificateEntry("server-app", trustedCert);

			//TODO save in resource directory
			String certificatePath = "client-app.p12";
			URL res = getClass().getClassLoader().getResource(certificatePath);
			File file = Paths.get(res.toURI()).toFile();
			certificatePath = file.getAbsolutePath();

			System.out.println("path: " + certificatePath);


			try (FileOutputStream fos = new FileOutputStream(certificatePath)) {
				keystore.store(fos, "client-app".toCharArray());
			}



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
			// just to show that we have actually received the file
			System.out.println("File Length:" + multiFile.getBytes().length);
			System.out.println("File Type:" + multiFile.getContentType());
			String fileName=multiFile.getOriginalFilename();
			System.out.println("File Name:" +fileName);
			String path=request.getServletContext().getRealPath("/");

			File fl = convertMultiPartToFile(multiFile);
			addCertificate(fl);

			//making directories for our required path.
			byte[] bytes = multiFile.getBytes();
			File directory=    new File(path+ "/uploads");
			directory.mkdirs();
			// saving the file
			String dir = "C:\\Users\\C283921\\AppData\\Local\\Temp\\1\\tomcat-docbase.9008.683872255700651084\\uploads" + fileName;
			File file=new File(dir);
			BufferedOutputStream stream = new BufferedOutputStream(
					new FileOutputStream(file));
			stream.write(bytes);
			stream.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return builder.toString();
	}
}
