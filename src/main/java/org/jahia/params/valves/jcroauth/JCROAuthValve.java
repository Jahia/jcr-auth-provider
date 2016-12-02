package org.jahia.params.valves.jcroauth;

import org.jahia.params.valves.AutoRegisteredBaseAuthValve;
import org.jahia.pipelines.PipelineException;
import org.jahia.pipelines.valves.ValveContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dgaillard
 */
public class JCROAuthValve extends AutoRegisteredBaseAuthValve {
    private static final Logger logger = LoggerFactory.getLogger(JCROAuthValve.class);

    @Override
    public void invoke(Object context, ValveContext valveContext) throws PipelineException {
        logger.info("YOYOYO");
    }
}
