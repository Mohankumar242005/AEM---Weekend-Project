
package com.weekend.core.services.impl;

import org.osgi.service.component.annotations.Component;

import com.weekend.core.services.Hellojava;

@Component(service = Hellojava.class)
public class Hellojavaimpl implements Hellojava{

    @Override
    public String getMessage() {
        return "This is my first service model";
    }
}
