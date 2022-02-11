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

    @RequestMapping(value = "/data", method = RequestMethod.GET)
    public String getData() {
        System.out.println("Returning data from server-app data method");
        return "Hello from Server-App-data method";
    }
}
