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

        System.out.println("Got inside client-app-server-data method");
        try {
            System.out.println("MS Endpoint name : [" + "https://localhost:9007/server-app/data" + "]");
            return restTemplate.getForObject(new URI("https://localhost:9007/server-app/data"), String.class);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "Exception occurred.. so, returning default data";
    }

}
