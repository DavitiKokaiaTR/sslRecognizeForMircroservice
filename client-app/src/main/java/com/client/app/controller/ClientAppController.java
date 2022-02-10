package com.client.app.controller;

import java.io.*;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.sql.Time;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.logging.Logger;

import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping(value="/client-app")
public class ClientAppController {
    @Autowired
    RestTemplate restTemplate;
    

    
    @RequestMapping(value = "/data", method = RequestMethod.GET)
    public String getData() {
        System.out.println("Returning data from client-app own data method");
        return "Hello from client-app-data method";
    }
    
    @RequestMapping(value = "/ms-data", method = RequestMethod.GET)
    public String getMsData() {

        getDataFromAlias();


        System.out.println("Got inside client-app-server-data method");
        try {
            System.out.println("MS Endpoint name : [" + "https://localhost:9007/server-app/data" + "]");
           if( 1   ==1){

               return restTemplate.getForObject(new URI("https://localhost:9007/server-app/data"), String.class);
           }

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer ");
            headers.set("MicroServiceUniqueID", "isheetversiondato.1641471995826");

            HttpEntity<String> entity = new HttpEntity<>("body", headers);


            ResponseEntity<String> response = restTemplate.exchange(
                    new URI("https://use2devp6125.hqdev.highq.com/isheetversiondato/data"), HttpMethod.GET, entity, String.class);

            return response.getBody();


        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "Exception occurred.. so, returning default data";
    }


    private  Certificate getCertificate(String certificatePath)
            throws Exception {
        CertificateFactory certificateFactory = CertificateFactory
                .getInstance("X.509");
        URL res = getClass().getClassLoader().getResource(certificatePath);
        File file = Paths.get(res.toURI()).toFile();
        certificatePath = file.getAbsolutePath();


        FileInputStream in = new FileInputStream(certificatePath);

        Certificate certificate = certificateFactory
                .generateCertificate(in);
        in.close();

        return certificate;
    }

    private void addCertificate(){
        try
        {
            KeyStore keystore = KeyStore.getInstance("PKCS12");
            ClassPathResource classPathResource = new ClassPathResource("client-app.p12");
            InputStream inputStream = classPathResource.getInputStream();
            keystore.load(inputStream, "client-app".toCharArray());

            Certificate trustedCert = getCertificate("server_app12.cer");

            keystore.setCertificateEntry("server-app123", trustedCert);

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

    private void getDataFromAlias(){
        try {
            KeyStore keystore = KeyStore.getInstance("PKCS12");
            ClassPathResource classPathResource = new ClassPathResource("client-app.p12");
            InputStream inputStream = classPathResource.getInputStream();
            keystore.load(inputStream, "client-app".toCharArray());

            Enumeration<String> aliases = keystore.aliases();
            while(aliases.hasMoreElements()){
                String alias = aliases.nextElement();
                if(keystore.getCertificate(alias).getType().equals("X.509")){
                    System.out.println(alias + " starts " + ((X509Certificate) keystore.getCertificate(alias)).getNotBefore());
                    System.out.println(alias + " expires " + ((X509Certificate) keystore.getCertificate(alias)).getNotAfter());
                    System.out.println(alias + " aboutInfo " + ((X509Certificate) keystore.getCertificate(alias)).getSubjectDN());
                    Certificate[] chain = keystore.getCertificateChain("client-app");

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
