package com.salaboy.conferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@SpringBootApplication
@EnableScheduling
public class ConferencesApplication {
    private Logger logger = LoggerFactory.getLogger(ConferencesApplication.class);

    @Autowired
    private ConferencesOperator conferencesOperator;


    public static void main(String[] args) {
        SpringApplication.run(ConferencesApplication.class,
                args);
    }


    @Scheduled(fixedDelay = 10000)
    public void reconcileLoop() {
        if (conferencesOperator.isOn()) {
            if (conferencesOperator.isInitDone()) {
                logger.info("+ --------------------- RECONCILE LOOP -------------------- + ");
                conferencesOperator.reconcile();
                logger.info("+ --------------------- END RECONCILE  -------------------- +\n\n\n ");
            } else {
                // Bootstrap
                logger.info("> Conferences Operator Bootstrapping ... ");
                conferencesOperator.bootstrap();
            }
        }
    }

}
