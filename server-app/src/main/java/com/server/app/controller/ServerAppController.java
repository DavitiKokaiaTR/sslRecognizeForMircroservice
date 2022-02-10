package com.server.app.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

@RestController
@RequestMapping(value = "/server-app")
public class ServerAppController {


    private void getDataFromAlias()
    {
        try
        {
            KeyStore keystore = KeyStore.getInstance("PKCS12");
            ClassPathResource classPathResource = new ClassPathResource("server-app.p12");
            InputStream inputStream = classPathResource.getInputStream();
            keystore.load(inputStream, "server-app".toCharArray());

            Enumeration<String> aliases = keystore.aliases();
            while (aliases.hasMoreElements())
            {
                String alias = aliases.nextElement();
                if (keystore.getCertificate(alias).getType().equals("X.509"))
                {
                    System.out.println(alias + " starts " + ((X509Certificate) keystore.getCertificate(alias)).getNotBefore());
                    System.out.println(alias + " expires " + ((X509Certificate) keystore.getCertificate(alias)).getNotAfter());
                    System.out.println(alias + " aboutInfo " + ((X509Certificate) keystore.getCertificate(alias)).getSubjectDN());

                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    @RequestMapping(value = "/data", method = RequestMethod.GET)
    public String getData() {
        getDataFromAlias();
        System.out.println("Returning data from server-app data method");
        return "Hello from Server-App-data method";
    }
}
