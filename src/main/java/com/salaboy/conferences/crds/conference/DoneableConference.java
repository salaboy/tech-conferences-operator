package com.salaboy.conferences.crds.conference;

import com.salaboy.conferences.crds.conference.Conference;
import io.fabric8.kubernetes.api.builder.Function;
import io.fabric8.kubernetes.client.CustomResourceDoneable;

public class DoneableConference extends CustomResourceDoneable<Conference> {

    public DoneableConference(Conference resource, Function<Conference, Conference> function) {
        super(resource, function);
    }
}
