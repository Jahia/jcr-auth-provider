package org.jahia.modules.jcroauthdatamapper.impl;

import org.jahia.modules.jahiaoauth.service.Mapper;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dgaillard
 */
public class JCROAuthDataMapperImpl implements Mapper {
    private static final Logger logger = LoggerFactory.getLogger(JCROAuthDataMapperImpl.class);

    private JahiaUserManagerService jahiaUserManagerService;

    @Override
    public void executeMapper() {

//        JCRUserNode userNode = jahiaUserManagerService.lookupUser(token, session);
//        if (userNode == null) {
//            userNode = jahiaUserManagerService.createUser()
//        }

        logger.info("Hello world!");
    }

    public void setJahiaUserManagerService(JahiaUserManagerService jahiaUserManagerService) {
        this.jahiaUserManagerService = jahiaUserManagerService;
    }
}
